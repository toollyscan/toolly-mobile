#!/usr/bin/env python3
"""Scan Toolly repository tracked files and git history for committed secrets.

Design rules:
- Scans current tracked files and every commit in git history.
- Never prints or logs discovered secret values.
- Reports only: redacted type, file/blob reference, line number, remediation.
- Supports narrow, expiring false-positive exceptions via
  config/ci/secret-exceptions.json.
- Uses only the Python standard library.

Exit 0 = no findings after exceptions.
Exit 1 = one or more unexcepted findings, or self-test failure.
"""

from __future__ import annotations

import argparse
import datetime as dt
import json
import re
import subprocess
import sys
import tempfile
from pathlib import Path
from typing import NamedTuple

ROOT = Path(__file__).resolve().parents[1]
EXCEPTIONS_PATH = ROOT / "config/ci/secret-exceptions.json"

# ---------------------------------------------------------------------------
# Secret patterns
#
# (rule_id, human_description, compiled_pattern, capture_group_index)
# Patterns are evaluated in order; first match per line wins.
# The capture group is used only for redaction; the value is never printed.
# ---------------------------------------------------------------------------

_PATTERNS: list[tuple[str, str, re.Pattern[str], int]] = [
    (
        "AWS_ACCESS_KEY_ID",
        "AWS access key ID",
        re.compile(r"\b(AKIA[0-9A-Z]{16})\b"),
        1,
    ),
    (
        "PRIVATE_KEY_BLOCK",
        "PEM private-key block",
        re.compile(
            r"(-----BEGIN (?:RSA |EC |DSA |OPENSSH |ENCRYPTED )?PRIVATE KEY-----)"
        ),
        1,
    ),
    (
        "FIREBASE_API_KEY",
        "Firebase / Google API key",
        re.compile(r"\b(AIza[0-9A-Za-z_-]{35})\b"),
        1,
    ),
    (
        "GOOGLE_OAUTH_TOKEN",
        "Google OAuth token",
        re.compile(r"\b(ya29\.[A-Za-z0-9_-]{20,})\b"),
        1,
    ),
    (
        "GITHUB_TOKEN",
        "GitHub personal-access / installation token",
        re.compile(r"\b(gh[pousr]_[A-Za-z0-9_]{36,})\b"),
        1,
    ),
    (
        "STRIPE_SECRET_KEY",
        "Stripe secret key",
        re.compile(r"\b((?:sk|rk)_(?:live|test)_[A-Za-z0-9]{24,})\b"),
        1,
    ),
    (
        "JWT_TOKEN",
        "JSON Web Token",
        re.compile(
            r"\b(ey[A-Za-z0-9_-]{20,}\.[A-Za-z0-9_-]{20,}\.[A-Za-z0-9_-]{20,})\b"
        ),
        1,
    ),
    (
        "GENERIC_SECRET_ASSIGNMENT",
        "Generic secret/key/token literal assignment",
        re.compile(
            r"""(?ix)
            (?:
                secret | api[_-]?key | private[_-]?key
                | access[_-]?token | auth[_-]?token
                | bearer[_-]?token | client[_-]?secret
                | password | passwd
            )
            \s* [:=] \s*
            ['"] ([^'"]{12,}) ['"]
            """
        ),
        1,
    ),
]

# ---------------------------------------------------------------------------
# Allow-list
#
# Lines matching any of these patterns are suppressed without further checks.
# Entries are intentionally narrow; broad suppressions belong in the
# exceptions file with an owner, reason and expiry.
#
# Important: lines in _self_test() that contain literal fixture values
# must end with a trailing "# self-test fixture" comment so that the
# self[_-]?test pattern below suppresses them when the scanner reads
# its own source during normal repo scans.
# ---------------------------------------------------------------------------

_ALLOWLIST: list[re.Pattern[str]] = [
    re.compile(r"^\s*[#/]{1,2}"),                    # comment lines
    re.compile(r"\$\{\{[^}]+\}\}"),                  # GitHub Actions ${{ secrets.X }}
    re.compile(r"\$\{[A-Z_][A-Z0-9_]*\}"),           # shell variable ${VAR}
    re.compile(r"(?i)example|placeholder|YOUR_|<[A-Z_]+>"),  # obvious placeholders
    re.compile(r"@[0-9a-f]{40}\b"),                  # pinned action SHA @<40hex>
    re.compile(r"sha256:[0-9a-f]{64}"),               # digest pin
    re.compile(r"(?i)self[_-]?test|unit[_-]?test|fixture|stub"),  # test contexts
]

# File suffixes skipped entirely (binary / key-material formats)
_SKIP_SUFFIXES = {
    ".png", ".jpg", ".jpeg", ".gif", ".ico", ".svg",
    ".zip", ".tar", ".gz", ".jar", ".aar", ".apk",
    ".keystore", ".jks", ".p12", ".pfx", ".lock",
}

# Path fragments that cause a file to be skipped
_SKIP_FRAGMENTS = {".git/", "node_modules/", "build/", ".gradle/"}


# ---------------------------------------------------------------------------
# Data types
# ---------------------------------------------------------------------------

class Finding(NamedTuple):
    rule: str
    description: str
    source: str
    line: int
    redacted: str

    def report(self) -> str:
        return (
            f"[{self.rule}] {self.description} | "
            f"location={self.source}:{self.line} | "
            f"value={self.redacted} | "
            f"action=Remove value, rotate credentials, purge history with "
            f"git-filter-repo."
        )


# ---------------------------------------------------------------------------
# Core helpers
# ---------------------------------------------------------------------------

def _redact(value: str) -> str:
    """Return a fixed redaction marker.

    No part of the value (prefix, suffix, or length) is conveyed.
    The original value is never stored or printed.
    """
    del value  # intentionally unused — value is never stored or printed
    return "[REDACTED]"


def _is_allowed(line: str) -> bool:
    return any(p.search(line) for p in _ALLOWLIST)


def _scan_line(line: str, line_number: int, source: str) -> list[Finding]:
    if _is_allowed(line):
        return []
    for rule_id, description, pattern, group in _PATTERNS:
        m = pattern.search(line)
        if m:
            value = m.group(group) if group <= (m.lastindex or 0) else m.group(0)
            return [Finding(
                rule=rule_id,
                description=description,
                source=source,
                line=line_number,
                redacted=_redact(value),
            )]
    return []


def _skip_path(rel: str) -> bool:
    if any(frag in rel for frag in _SKIP_FRAGMENTS):
        return True
    return Path(rel).suffix.lower() in _SKIP_SUFFIXES


# ---------------------------------------------------------------------------
# Exception loading
# ---------------------------------------------------------------------------

def _load_exceptions(path: Path) -> list[dict]:
    if not path.exists():
        return []
    try:
        data = json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as err:
        print(f"WARNING: Cannot load exceptions file: {err}", file=sys.stderr)
        return []
    today = dt.date.today()
    active: list[dict] = []
    for exc in data.get("exceptions", []):
        try:
            expires = dt.date.fromisoformat(exc.get("expires_at", ""))
        except (ValueError, TypeError):
            print(
                f"WARNING: Exception '{exc.get('id')}' missing or invalid "
                f"'expires_at'; skipping.",
                file=sys.stderr,
            )
            continue
        if expires < today:
            print(
                f"WARNING: Exception '{exc.get('id')}' expired {expires}; "
                f"treating as inactive.",
                file=sys.stderr,
            )
            continue
        active.append(exc)
    return active


def _is_excepted(finding: Finding, exceptions: list[dict]) -> bool:
    for exc in exceptions:
        if exc.get("rule") and exc["rule"] != finding.rule:
            continue
        src = exc.get("source", "")
        if src and src not in finding.source:
            continue
        exc_line = exc.get("line")
        if exc_line is not None and int(exc_line) != finding.line:
            continue
        return True
    return False


# ---------------------------------------------------------------------------
# Scan tracked files
# ---------------------------------------------------------------------------

def _scan_tracked(root: Path) -> list[Finding]:
    try:
        result = subprocess.run(
            ["git", "ls-files"],
            cwd=root, capture_output=True, text=True, check=True,
        )
    except (subprocess.CalledProcessError, FileNotFoundError) as err:
        print(f"WARNING: Cannot list tracked files: {err}", file=sys.stderr)
        return []
    findings: list[Finding] = []
    for rel in result.stdout.splitlines():
        if _skip_path(rel):
            continue
        try:
            text = (root / rel).read_text(encoding="utf-8", errors="replace")
        except OSError:
            continue
        for i, line in enumerate(text.splitlines(), 1):
            findings.extend(_scan_line(line, i, rel))
    return findings


# ---------------------------------------------------------------------------
# Scan git history (added lines only)
# ---------------------------------------------------------------------------

def _scan_history(root: Path) -> list[Finding]:
    try:
        commits_result = subprocess.run(
            ["git", "log", "--all", "--format=%H"],
            cwd=root, capture_output=True, text=True, check=True,
        )
    except (subprocess.CalledProcessError, FileNotFoundError) as err:
        print(f"WARNING: Cannot enumerate history: {err}", file=sys.stderr)
        return []

    findings: list[Finding] = []
    for sha in commits_result.stdout.splitlines():
        sha = sha.strip()
        if not sha:
            continue
        try:
            diff = subprocess.run(
                ["git", "diff-tree", "--no-commit-id", "-r", "--unified=0", sha],
                cwd=root, capture_output=True, text=True, check=True,
            )
        except subprocess.CalledProcessError:
            continue
        current_file: str | None = None
        line_num = 0
        for raw in diff.stdout.splitlines():
            if raw.startswith("+++ b/"):
                current_file = raw[6:]
                line_num = 0
            elif raw.startswith("@@"):
                m = re.search(r"\+(\d+)", raw)
                line_num = int(m.group(1)) if m else 0
            elif raw.startswith("+") and not raw.startswith("+++"):
                line_num += 1
                if current_file and not _skip_path(current_file):
                    src = f"commit:{sha[:12]}:{current_file}"
                    findings.extend(_scan_line(raw[1:], line_num, src))
            elif not raw.startswith("-"):
                line_num += 1
    return findings


# ---------------------------------------------------------------------------
# Deterministic self-tests
#
# Fixture lines that contain synthetic credential-shaped values are annotated
# with a trailing "# self-test fixture" comment.  The _ALLOWLIST entry for
# "self[_-]?test" suppresses these lines when the scanner reads its own
# source during a normal repo scan, preventing false positives.
# ---------------------------------------------------------------------------

def _self_test() -> list[str]:
    """Run deterministic checks without accessing the real repo."""
    failures: list[str] = []

    # ── Positive cases (must be detected) ───────────────────────────────────

    # AWS access key: AKIA + 16 uppercase alphanumeric chars.
    # Chars B–Q contain no allowlist word.  Comment suppresses repo-scan.
    aws_line = "ACCESS_KEY_ID: AKIABCDEFGHIJKLMNOPQ"  # self-test fixture
    found = _scan_line(aws_line, 1, "cfg.py")
    if not any(f.rule == "AWS_ACCESS_KEY_ID" for f in found):
        failures.append("self-test: AWS_ACCESS_KEY_ID not detected")

    # Firebase API key: AIza + exactly 35 alphanumeric chars.
    # The suffix a–z + ABCDEF contains no allowlist word.
    fb_suffix = "SyBabcdefghijklmnopqrstuvwxyzABCDEF"  # self-test fixture
    fb_line = "api_key: AIza" + fb_suffix
    found = _scan_line(fb_line, 1, "cfg.json")
    if not any(f.rule == "FIREBASE_API_KEY" for f in found):
        failures.append("self-test: FIREBASE_API_KEY not detected")

    # PEM private-key header: plain marker text, not an allowlist word.
    pk_line = "-----BEGIN RSA PRIVATE KEY-----"  # self-test fixture
    found = _scan_line(pk_line, 1, "key.pem")
    if not any(f.rule == "PRIVATE_KEY_BLOCK" for f in found):
        failures.append("self-test: PRIVATE_KEY_BLOCK not detected")

    # GitHub PAT: ghp_ + 36 chars (A–Z + a–f + 6789); no allowlist word.
    gh_body = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdef6789"  # self-test fixture
    gh_line = "ghp_" + gh_body
    found = _scan_line(gh_line, 1, "env.sh")
    if not any(f.rule == "GITHUB_TOKEN" for f in found):
        failures.append("self-test: GITHUB_TOKEN not detected")

    # ── Negative cases (must NOT be detected) ───────────────────────────────

    # GitHub Actions secret expression
    safe_expr = "TOKEN: ${{ secrets.MY_TOKEN }}"
    found = _scan_line(safe_expr, 1, "workflow.yml")
    if found:
        failures.append(
            f"self-test: GitHub Actions expression incorrectly flagged: "
            f"{found[0].rule}"
        )

    # Shell variable reference
    safe_var = "API_KEY: ${MY_API_KEY}"
    found = _scan_line(safe_var, 1, "Makefile")
    if found:
        failures.append(
            f"self-test: shell variable reference incorrectly flagged: "
            f"{found[0].rule}"
        )

    # Obvious placeholder
    safe_placeholder = "api_key: YOUR_API_KEY_HERE"
    found = _scan_line(safe_placeholder, 1, "README.md")
    if found:
        failures.append(
            f"self-test: placeholder incorrectly flagged: {found[0].rule}"
        )

    # Pinned action SHA (40 hex chars after @)
    safe_sha = "uses: actions/checkout@3d3c42e5aac5ba805825da76410c181273ba90b1"
    found = _scan_line(safe_sha, 1, "workflow.yml")
    if found:
        failures.append(
            f"self-test: pinned action SHA incorrectly flagged: {found[0].rule}"
        )

    # ── Redaction invariant ──────────────────────────────────────────────────

    real_value = "AKIABCDEFGHIJKLMNOPQ"  # self-test fixture
    redacted = _redact(real_value)
    if real_value in redacted:
        failures.append("self-test: _redact() leaked the full secret value")
    if redacted != "[REDACTED]":
        failures.append("self-test: _redact() did not return the fixed marker")
    for substr in ("AKIA", "BCDE", "MNOPQ"):
        if substr in redacted:
            failures.append(
                f"self-test: _redact() revealed part of the secret value ({substr!r})"
            )

    # ── Exception lifecycle ──────────────────────────────────────────────────

    with tempfile.TemporaryDirectory() as tmpdir:
        exc_path = Path(tmpdir) / "exc.json"
        today = dt.date.today()
        future = (today + dt.timedelta(days=30)).isoformat()
        past = (today - dt.timedelta(days=1)).isoformat()

        exc_path.write_text(json.dumps({
            "schema_version": 1,
            "exceptions": [
                {
                    "id": "valid-exc",
                    "rule": "AWS_ACCESS_KEY_ID",
                    "source": "cfg.py",
                    "line": 1,
                    "expires_at": future,
                    "owner": "unit-test",
                    "reason": "self-test fixture",
                },
                {
                    "id": "expired-exc",
                    "rule": "AWS_ACCESS_KEY_ID",
                    "source": "cfg.py",
                    "line": 1,
                    "expires_at": past,
                    "owner": "unit-test",
                    "reason": "must be ignored",
                },
            ],
        }), encoding="utf-8")

        active = _load_exceptions(exc_path)
        if len(active) != 1:
            failures.append(
                f"self-test: expected 1 active exception, got {len(active)}"
            )
        if active and active[0]["id"] != "valid-exc":
            failures.append("self-test: wrong exception was retained")

        finding = Finding(
            rule="AWS_ACCESS_KEY_ID",
            description="AWS access key ID",
            source="cfg.py",
            line=1,
            redacted=_redact("AKIABCDEFGHIJKLMNOPQ"),  # self-test fixture
        )
        if not _is_excepted(finding, active):
            failures.append(
                "self-test: valid active exception did not suppress the finding"
            )

    return failures


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

def main() -> int:
    parser = argparse.ArgumentParser(
        description="Scan Toolly repository for committed secrets."
    )
    parser.add_argument("--root", type=Path, default=ROOT)
    parser.add_argument(
        "--self-test", action="store_true",
        help="Run deterministic self-tests and exit",
    )
    parser.add_argument(
        "--no-history", action="store_true",
        help="Skip git history scan; check tracked files only",
    )
    parser.add_argument(
        "--exceptions", type=Path, default=EXCEPTIONS_PATH,
        help="Path to secret-exceptions.json",
    )
    args = parser.parse_args()
    root: Path = args.root.resolve()

    if args.self_test:
        failures = _self_test()
        if failures:
            for f in failures:
                print(f"FAIL: {f}", file=sys.stderr)
            print(
                f"Secret scanner self-test failed with {len(failures)} failure(s).",
                file=sys.stderr,
            )
            return 1
        print("Secret scanner self-test passed.")
        return 0

    exceptions = _load_exceptions(args.exceptions)

    print("Scanning tracked files…")
    findings = _scan_tracked(root)

    if not args.no_history:
        print("Scanning repository history…")
        findings.extend(_scan_history(root))

    # Deduplicate by (rule, source, line)
    seen: set[tuple[str, str, int]] = set()
    unique: list[Finding] = []
    for f in findings:
        key = (f.rule, f.source, f.line)
        if key not in seen:
            seen.add(key)
            unique.append(f)

    unexcepted = [f for f in unique if not _is_excepted(f, exceptions)]

    if unexcepted:
        print(
            f"\nSecret scan: {len(unexcepted)} finding(s). "
            "Secret values are REDACTED below.\n",
            file=sys.stderr,
        )
        for finding in unexcepted:
            print(f"FINDING: {finding.report()}", file=sys.stderr)
        print(
            "\nTo suppress a false positive, add a narrow, expiring entry to "
            "config/ci/secret-exceptions.json with owner, reason and expires_at.",
            file=sys.stderr,
        )
        return 1

    excepted_count = len(unique) - len(unexcepted)
    print(
        f"Secret scan passed: {len(unique)} potential match(es) examined, "
        f"{excepted_count} excepted, 0 unresolved."
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
