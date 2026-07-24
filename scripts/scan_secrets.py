#!/usr/bin/env python3
"""Scan Toolly repository tracked files and git history for committed secrets.

Design rules:
- Scans current tracked files and every commit in git history.
- Never prints or logs discovered secret values; reports only the fixed
  marker [REDACTED].
- Fails with exit code 1 if any git operation required for scanning fails.
- Reports prohibited signing/credential artifact files as
  PROHIBITED_SECRET_ARTIFACT regardless of content.
- Exceptions must supply all required fields with exact values; partial
  source matching, wildcard rules and broad exceptions are rejected.
- Uses only the Python standard library.

Exit 0 = no findings after exceptions.
Exit 1 = one or more unexcepted findings, scanner failure, or self-test
         failure.
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
# Design:
# - Comment lines are NOT suppressed — a secret in a comment is still a
#   secret and must be removed and rotated.
# - "example" and "placeholder" are NOT suppressed — too broad and easily
#   abused to hide real credentials.
# - Only exact, clearly-synthetic placeholder formats are allowed:
#   YOUR_UPPER_CASE_NAME and <UPPER_CASE_NAME>.
#
# Important: lines in _self_test() that contain literal fixture values
# must end with a trailing "# self-test fixture" comment so that the
# self[_-]?test pattern below suppresses them when the scanner reads
# its own source during normal repo scans.
# ---------------------------------------------------------------------------

_ALLOWLIST: list[re.Pattern[str]] = [
    re.compile(r"\$\{\{[^}]+\}\}"),              # GitHub Actions ${{ secrets.X }}
    re.compile(r"\$\{[A-Z_][A-Z0-9_]*\}"),       # shell variable ${VAR}
    re.compile(r"\bYOUR_[A-Z][A-Z0-9_]*\b"),     # YOUR_API_KEY style placeholder
    re.compile(r"<[A-Z][A-Z0-9_]*>"),            # <UPPER_CASE> style placeholder
    re.compile(r"@[0-9a-f]{40}\b"),              # pinned action SHA @<40hex>
    re.compile(r"sha256:[0-9a-f]{64}"),           # digest pin
    re.compile(r"(?i)self[_-]?test|unit[_-]?test|fixture|stub"),  # test contexts
]

# ---------------------------------------------------------------------------
# Prohibited artifact suffixes
#
# These file types contain or ARE signing material / credentials.
# Their presence anywhere in tracked files or history is itself a finding.
# They are NOT scanned for content; their existence is the violation.
# ---------------------------------------------------------------------------

_PROHIBITED_ARTIFACT_SUFFIXES = frozenset({
    ".jks", ".keystore", ".p12", ".pfx",
    ".pem", ".key", ".p8", ".mobileprovision",
})

# File suffixes skipped entirely (binary / non-text formats that are not
# credential artifacts — credential artifact suffixes are handled above).
_SKIP_SUFFIXES = frozenset({
    ".png", ".jpg", ".jpeg", ".gif", ".ico", ".svg",
    ".zip", ".tar", ".gz", ".jar", ".aar", ".apk", ".lock",
})

# Path fragments that cause a file to be skipped
_SKIP_FRAGMENTS = {".git/", "node_modules/", "build/", ".gradle/"}

# Complete set of rule identifiers used in findings and required by exceptions.
_KNOWN_RULES: frozenset[str] = frozenset({
    "AWS_ACCESS_KEY_ID",
    "PRIVATE_KEY_BLOCK",
    "FIREBASE_API_KEY",
    "GOOGLE_OAUTH_TOKEN",
    "GITHUB_TOKEN",
    "STRIPE_SECRET_KEY",
    "JWT_TOKEN",
    "GENERIC_SECRET_ASSIGNMENT",
    "PROHIBITED_SECRET_ARTIFACT",
})


# ---------------------------------------------------------------------------
# Scanner error
# ---------------------------------------------------------------------------

class ScannerError(RuntimeError):
    """Raised when a required git operation cannot be completed.

    The scanner must fail closed: any inability to enumerate tracked files
    or repository history is reported as exit code 1, never silently ignored.
    """


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
# Exception loading and validation
# ---------------------------------------------------------------------------

def _validate_exception_entry(exc: object) -> str | None:
    """Return an error message if *exc* is invalid; return None if valid.

    Validation rules:
    - All seven fields must be present and non-empty.
    - 'rule' must be an exact known rule identifier.
    - 'source' must be a non-empty string (exact path — no wildcards).
    - 'line' must be a positive integer.
    - 'expires_at' must be a valid ISO date (checked in _load_exceptions).
    """
    if not isinstance(exc, dict):
        return f"exception entry is not a JSON object (got {type(exc).__name__})"
    exc_id = exc.get("id")
    if not isinstance(exc_id, str) or not exc_id.strip():
        return "exception missing non-empty 'id'"
    rule = exc.get("rule")
    if not isinstance(rule, str) or rule not in _KNOWN_RULES:
        return (
            f"exception '{exc_id}': 'rule' must be one of "
            f"{sorted(_KNOWN_RULES)}, got {rule!r}"
        )
    source = exc.get("source")
    if not isinstance(source, str) or not source.strip():
        return f"exception '{exc_id}': 'source' must be a non-empty string"
    if "*" in source or "?" in source:
        return f"exception '{exc_id}': 'source' must not contain wildcards"
    line = exc.get("line")
    if not isinstance(line, int) or line < 1:
        return f"exception '{exc_id}': 'line' must be a positive integer"
    owner = exc.get("owner")
    if not isinstance(owner, str) or not owner.strip():
        return f"exception '{exc_id}': 'owner' must be a non-empty string"
    reason = exc.get("reason")
    if not isinstance(reason, str) or not reason.strip():
        return f"exception '{exc_id}': 'reason' must be a non-empty string"
    if "expires_at" not in exc:
        return f"exception '{exc_id}': 'expires_at' is required"
    return None


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
        validation_error = _validate_exception_entry(exc)
        if validation_error:
            print(
                f"WARNING: Rejecting invalid exception: {validation_error}",
                file=sys.stderr,
            )
            continue
        try:
            expires = dt.date.fromisoformat(exc["expires_at"])
        except (ValueError, TypeError):
            print(
                f"WARNING: Exception '{exc.get('id')}' has invalid "
                f"'expires_at' format; rejecting.",
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
    """Return True only if an active exception matches all fields exactly.

    Matching rules:
    - 'rule' must equal the finding rule exactly.
    - 'source' must equal the finding source exactly (no partial matching).
    - 'line' must equal the finding line number exactly.

    All three fields must match; no partial or wildcard matching is permitted.
    """
    for exc in exceptions:
        if exc["rule"] != finding.rule:
            continue
        if exc["source"] != finding.source:
            continue
        if int(exc["line"]) != finding.line:
            continue
        return True
    return False


# ---------------------------------------------------------------------------
# Scan tracked files
# ---------------------------------------------------------------------------

def _scan_tracked(root: Path) -> list[Finding]:
    """Scan all tracked files for secrets and prohibited artifact types.

    Raises ScannerError if git ls-files cannot be executed.
    """
    try:
        result = subprocess.run(
            ["git", "ls-files"],
            cwd=root, capture_output=True, text=True, check=True,
        )
    except (subprocess.CalledProcessError, FileNotFoundError) as err:
        raise ScannerError(f"Cannot list tracked files: {err}") from err
    findings: list[Finding] = []
    for rel in result.stdout.splitlines():
        suffix = Path(rel).suffix.lower()
        # Prohibited artifact: its presence is itself the finding.
        if suffix in _PROHIBITED_ARTIFACT_SUFFIXES:
            findings.append(Finding(
                rule="PROHIBITED_SECRET_ARTIFACT",
                description="Signing/credential artifact must not be committed",
                source=rel,
                line=0,
                redacted="[REDACTED]",
            ))
            continue
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
    """Scan every commit in git history for secrets and prohibited artifacts.

    Raises ScannerError if git log cannot be executed.
    """
    try:
        commits_result = subprocess.run(
            ["git", "log", "--all", "--format=%H"],
            cwd=root, capture_output=True, text=True, check=True,
        )
    except (subprocess.CalledProcessError, FileNotFoundError) as err:
        raise ScannerError(f"Cannot enumerate history: {err}") from err

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
                # Prohibited artifact: any commit that introduces one is a finding.
                if Path(current_file).suffix.lower() in _PROHIBITED_ARTIFACT_SUFFIXES:
                    findings.append(Finding(
                        rule="PROHIBITED_SECRET_ARTIFACT",
                        description="Signing/credential artifact must not be committed",
                        source=f"commit:{sha[:12]}:{current_file}",
                        line=0,
                        redacted="[REDACTED]",
                    ))
            elif raw.startswith("@@"):
                m = re.search(r"\+(\d+)", raw)
                line_num = int(m.group(1)) if m else 0
            elif raw.startswith("+") and not raw.startswith("+++"):
                line_num += 1
                if current_file and not _skip_path(current_file):
                    if Path(current_file).suffix.lower() not in _PROHIBITED_ARTIFACT_SUFFIXES:
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

    today = dt.date.today()
    future_date = (today + dt.timedelta(days=30)).isoformat()
    past_date = (today - dt.timedelta(days=1)).isoformat()

    # ── Positive cases (must be detected) ───────────────────────────────────

    # AWS access key: AKIA + 16 uppercase alphanumeric chars.
    aws_line = "ACCESS_KEY_ID: AKIABCDEFGHIJKLMNOPQ"  # self-test fixture
    found = _scan_line(aws_line, 1, "cfg.py")
    if not any(f.rule == "AWS_ACCESS_KEY_ID" for f in found):
        failures.append("self-test: AWS_ACCESS_KEY_ID not detected")

    # Firebase API key: AIza + exactly 35 alphanumeric chars.
    fb_suffix = "SyBabcdefghijklmnopqrstuvwxyzABCDEF"  # self-test fixture
    fb_line = "api_key: AIza" + fb_suffix
    found = _scan_line(fb_line, 1, "cfg.json")
    if not any(f.rule == "FIREBASE_API_KEY" for f in found):
        failures.append("self-test: FIREBASE_API_KEY not detected")

    # PEM private-key header.
    pk_line = "-----BEGIN RSA PRIVATE KEY-----"  # self-test fixture
    found = _scan_line(pk_line, 1, "key.pem")
    if not any(f.rule == "PRIVATE_KEY_BLOCK" for f in found):
        failures.append("self-test: PRIVATE_KEY_BLOCK not detected")

    # GitHub PAT: ghp_ + 36 chars.
    gh_body = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdef6789"  # self-test fixture
    gh_line = "ghp_" + gh_body
    found = _scan_line(gh_line, 1, "env.sh")
    if not any(f.rule == "GITHUB_TOKEN" for f in found):
        failures.append("self-test: GITHUB_TOKEN not detected")

    # ── Comment lines are NOT suppressed ────────────────────────────────────
    # A secret in a comment line must still be detected.

    comment_aws = "# ACCESS_KEY_ID: AKIABCDEFGHIJKLMNOPQ"  # self-test fixture
    found = _scan_line(comment_aws, 1, "cfg.py")
    if not any(f.rule == "AWS_ACCESS_KEY_ID" for f in found):
        failures.append(
            "self-test: AWS_ACCESS_KEY_ID in comment line was not detected"
        )

    # ── "example" and "placeholder" are NOT suppressed ──────────────────────
    # The words alone must not silence a real credential match.

    # A line containing "example" elsewhere still has a detectable key.
    example_aws = "example AKIABCDEFGHIJKLMNOPQ value"  # self-test fixture
    found = _scan_line(example_aws, 1, "README.md")
    if not any(f.rule == "AWS_ACCESS_KEY_ID" for f in found):
        failures.append(
            "self-test: AWS key alongside 'example' was incorrectly suppressed"
        )

    # A line containing "placeholder" elsewhere still has a detectable key.
    placeholder_aws = "placeholder AKIABCDEFGHIJKLMNOPQ"  # self-test fixture
    found = _scan_line(placeholder_aws, 1, "README.md")
    if not any(f.rule == "AWS_ACCESS_KEY_ID" for f in found):
        failures.append(
            "self-test: AWS key alongside 'placeholder' was incorrectly suppressed"
        )

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

    # YOUR_* placeholder — still allowed as an exact synthetic format.
    # Uses quotes so GENERIC_SECRET_ASSIGNMENT would fire without the allowlist.
    safe_your = 'api_key = "YOUR_API_KEY_VALUE_HERE"'
    found = _scan_line(safe_your, 1, "README.md")
    if found:
        failures.append(
            f"self-test: YOUR_ placeholder incorrectly flagged: {found[0].rule}"
        )

    # <UPPER_CASE> placeholder — still allowed.
    safe_angle = 'password = "<YOUR_DB_PASSWORD>"'
    found = _scan_line(safe_angle, 1, "README.md")
    if found:
        failures.append(
            f"self-test: <UPPER_CASE> placeholder incorrectly flagged: {found[0].rule}"
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

    # ── Prohibited artifact suffix detection ────────────────────────────────

    for prohibited_suffix in (".jks", ".keystore", ".p12", ".pfx", ".pem",
                               ".key", ".p8", ".mobileprovision"):
        if prohibited_suffix not in _PROHIBITED_ARTIFACT_SUFFIXES:
            failures.append(
                f"self-test: {prohibited_suffix!r} missing from "
                f"_PROHIBITED_ARTIFACT_SUFFIXES"
            )
        if prohibited_suffix in _SKIP_SUFFIXES:
            failures.append(
                f"self-test: {prohibited_suffix!r} is in _SKIP_SUFFIXES "
                f"(must not be skipped silently)"
            )

    # ── ScannerError on missing git root ────────────────────────────────────

    try:
        _scan_tracked(Path("/nonexistent-toolly-self-test-root"))
        failures.append(
            "self-test: _scan_tracked() did not raise ScannerError on invalid root"
        )
    except ScannerError:
        pass

    try:
        _scan_history(Path("/nonexistent-toolly-self-test-root"))
        failures.append(
            "self-test: _scan_history() did not raise ScannerError on invalid root"
        )
    except ScannerError:
        pass

    # ── Exception validation ─────────────────────────────────────────────────

    def _good_exc(overrides: dict | None = None) -> dict:
        base = {
            "id": "exc-001",
            "rule": "AWS_ACCESS_KEY_ID",
            "source": "config/secrets.py",
            "line": 42,
            "owner": "team-security",
            "reason": "self-test fixture value",
            "expires_at": future_date,
        }
        if overrides:
            base.update(overrides)
        return base

    # Valid exception should pass validation
    if _validate_exception_entry(_good_exc()) is not None:
        failures.append("self-test: valid exception was incorrectly rejected")

    # Missing 'id'
    if _validate_exception_entry(_good_exc({"id": ""})) is None:
        failures.append("self-test: empty 'id' was not rejected")

    # Unknown rule
    if _validate_exception_entry(_good_exc({"rule": "UNKNOWN_RULE"})) is None:
        failures.append("self-test: unknown rule was not rejected")

    # Wildcard rule ('*') must be rejected
    if _validate_exception_entry(_good_exc({"rule": "*"})) is None:
        failures.append("self-test: wildcard rule '*' was not rejected")

    # Empty source
    if _validate_exception_entry(_good_exc({"source": ""})) is None:
        failures.append("self-test: empty source was not rejected")

    # Wildcard source
    if _validate_exception_entry(_good_exc({"source": "*.py"})) is None:
        failures.append("self-test: wildcard source was not rejected")

    # Non-positive line
    if _validate_exception_entry(_good_exc({"line": 0})) is None:
        failures.append("self-test: line=0 was not rejected")

    if _validate_exception_entry(_good_exc({"line": -1})) is None:
        failures.append("self-test: line=-1 was not rejected")

    # Missing owner
    if _validate_exception_entry(_good_exc({"owner": ""})) is None:
        failures.append("self-test: empty owner was not rejected")

    # Missing reason
    if _validate_exception_entry(_good_exc({"reason": ""})) is None:
        failures.append("self-test: empty reason was not rejected")

    # Missing expires_at entirely
    exc_no_expiry = {k: v for k, v in _good_exc().items() if k != "expires_at"}
    if _validate_exception_entry(exc_no_expiry) is None:
        failures.append("self-test: missing expires_at was not rejected")

    # ── _is_excepted: exact source matching ──────────────────────────────────

    finding_exact = Finding(
        rule="AWS_ACCESS_KEY_ID",
        description="AWS access key ID",
        source="config/secrets.py",
        line=42,
        redacted="[REDACTED]",
    )

    exc_exact = [{
        "rule": "AWS_ACCESS_KEY_ID",
        "source": "config/secrets.py",
        "line": 42,
    }]
    if not _is_excepted(finding_exact, exc_exact):
        failures.append("self-test: exact-match exception did not suppress the finding")

    # Partial source must NOT match
    exc_partial = [{
        "rule": "AWS_ACCESS_KEY_ID",
        "source": "secrets",          # partial path component
        "line": 42,
    }]
    if _is_excepted(finding_exact, exc_partial):
        failures.append("self-test: partial source incorrectly matched the finding")

    # ── Exception lifecycle (load + active filtering) ────────────────────────

    with tempfile.TemporaryDirectory() as tmpdir:
        exc_path = Path(tmpdir) / "exc.json"

        exc_path.write_text(json.dumps({
            "schema_version": 1,
            "exceptions": [
                _good_exc({"id": "valid-exc"}),
                _good_exc({
                    "id": "expired-exc",
                    "expires_at": past_date,
                }),
                # Invalid entry (missing owner) must be rejected by _load_exceptions
                _good_exc({"id": "bad-exc", "owner": ""}),
            ],
        }), encoding="utf-8")

        active = _load_exceptions(exc_path)
        if len(active) != 1:
            failures.append(
                f"self-test: expected 1 active exception, got {len(active)}"
            )
        if active and active[0]["id"] != "valid-exc":
            failures.append("self-test: wrong exception was retained")

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

    try:
        print("Scanning tracked files…")
        findings = _scan_tracked(root)

        if not args.no_history:
            print("Scanning repository history…")
            findings.extend(_scan_history(root))
    except ScannerError as err:
        print(f"ERROR: Scanner failure — {err}", file=sys.stderr)
        print(
            "The scanner must be able to enumerate all tracked files and history. "
            "Resolve the git error above before merging.",
            file=sys.stderr,
        )
        return 1

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
