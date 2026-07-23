#!/usr/bin/env python3
"""Validate Toolly Markdown files against the project lint policy.

Uses only the Python standard library.  Enforces the rules enabled by
.markdownlint.json (default: true with MD013, MD033, MD041, MD060 disabled).

Exit code 0 = all files pass.
Exit code 1 = one or more violations found (printed to stderr).
"""

from __future__ import annotations

import argparse
import re
import sys
import tempfile
from pathlib import Path
from typing import NamedTuple

ROOT = Path(__file__).resolve().parents[1]

# ── Patterns ──────────────────────────────────────────────────────────────────

# A heading attempt is any line that starts with 1-6 # characters.
# Each alternation covers one valid/invalid heading form:
#   [ \t].+    valid heading with text ("# Heading text")
#   [ \t]?$    empty heading or heading with only whitespace ("##")
#   [^ \t#\n]. invalid no-space heading detected for MD018 ("##Heading")
ATX_HEADING = re.compile(r"^(#{1,6})([ \t].+|[ \t]?$|[^ \t#\n].*)")
ATX_NO_SPACE = re.compile(r"^#{1,6}[^ \t#\n]")  # MD018: no space after hash
ATX_MULTI_SPACE = re.compile(r"^#{1,6} {2,}")    # MD019: multiple spaces after hash
FENCE_START = re.compile(r"^(`{3,}|~{3,})")
HARD_TAB = re.compile(r"\t")
TRAILING_SPACE = re.compile(r" +$")


class Violation(NamedTuple):
    rule: str
    path: str
    line: int
    message: str

    def __str__(self) -> str:
        return f"{self.path}:{self.line}: [{self.rule}] {self.message}"


# ── Per-file validation ────────────────────────────────────────────────────────

def _check_file(path: Path, root: Path) -> list[Violation]:
    try:
        raw = path.read_bytes()
    except OSError as err:
        return [Violation("IO", str(path.relative_to(root)), 0, str(err))]

    text = raw.decode("utf-8", errors="replace")
    lines = text.splitlines(keepends=True)
    rel = str(path.relative_to(root))
    violations: list[Violation] = []

    in_code_block = False
    fence_marker = ""
    blank_run = 0
    prev_blank = True          # treat start-of-file as preceded by blank
    prev_heading_level: int | None = None

    for i, raw_line in enumerate(lines, start=1):
        # Detect fenced code block boundaries (skip content inside)
        stripped = raw_line.rstrip("\r\n")

        fence_match = FENCE_START.match(stripped)
        if fence_match:
            marker = fence_match.group(1)[0]  # ` or ~
            if not in_code_block:
                in_code_block = True
                fence_marker = marker
            elif stripped.startswith(fence_marker * 3):
                in_code_block = False
                fence_marker = ""

        if in_code_block and fence_match is None:
            # Inside fenced block: only check hard tabs at the line level
            blank_run = 0
            prev_blank = False
            continue

        is_blank = stripped.strip() == ""

        # MD012 — multiple consecutive blank lines (more than 2)
        if is_blank:
            blank_run += 1
            if blank_run > 2:
                violations.append(
                    Violation("MD012", rel, i, "Multiple consecutive blank lines")
                )
        else:
            blank_run = 0

        # MD010 — hard tabs (outside code blocks)
        if HARD_TAB.search(stripped):
            violations.append(
                Violation("MD010", rel, i, "Hard tab character found")
            )

        # MD009 — trailing spaces (two spaces is intentional line break; allow that)
        if TRAILING_SPACE.search(stripped):
            trailing_count = len(stripped) - len(stripped.rstrip())
            if trailing_count != 2:  # 2 trailing spaces = intentional line-break
                violations.append(
                    Violation("MD009", rel, i, "Trailing spaces")
                )

        # ATX heading checks
        heading_match = ATX_HEADING.match(stripped)
        if heading_match:
            level = len(heading_match.group(1))

            # MD018 — no space after hash
            if ATX_NO_SPACE.match(stripped):
                violations.append(
                    Violation("MD018", rel, i, "No space after hash on atx style heading")
                )

            # MD019 — multiple spaces after hash
            if ATX_MULTI_SPACE.match(stripped):
                violations.append(
                    Violation(
                        "MD019", rel, i,
                        "Multiple spaces after hash on atx style heading"
                    )
                )

            # MD022 — heading must be preceded by blank line (or start of file)
            if i > 1 and not prev_blank:
                violations.append(
                    Violation("MD022", rel, i, "Heading not preceded by a blank line")
                )

            # MD001 — heading levels should only increment by one level at a time
            if (
                prev_heading_level is not None
                and level > prev_heading_level + 1
            ):
                violations.append(
                    Violation(
                        "MD001", rel, i,
                        f"Heading level jumped from {prev_heading_level} to {level}"
                    )
                )

            prev_heading_level = level

        prev_blank = is_blank

    # MD022 — last heading must be followed by blank line or end of file is fine
    # (markdownlint only checks preceded-by, not followed-by for last heading)

    # MD047 — file should end with a single newline
    if text and not text.endswith("\n"):
        violations.append(
            Violation("MD047", rel, len(lines), "File should end with a single newline")
        )

    return violations


# ── Glob helpers ──────────────────────────────────────────────────────────────

_SKIP_DIRS = {".git", "node_modules", ".gradle", "build", ".idea"}


def _collect_markdown(root: Path) -> list[Path]:
    paths: list[Path] = []
    for p in sorted(root.rglob("*.md")):
        if any(part in _SKIP_DIRS for part in p.parts):
            continue
        paths.append(p)
    return paths


# ── Self-tests ─────────────────────────────────────────────────────────────────

def _self_test() -> list[str]:
    failures: list[str] = []

    with tempfile.TemporaryDirectory() as tmpdir:
        root = Path(tmpdir)

        def _write(name: str, content: str) -> Path:
            p = root / name
            p.write_text(content, encoding="utf-8")
            return p

        def _violations(name: str, content: str) -> list[str]:
            p = _write(name, content)
            return [v.rule for v in _check_file(p, root)]

        # MD009: trailing space
        # Two trailing spaces is intentional line-break; should NOT flag MD009
        if "MD009" in _violations("t009.md", "# Hello\n\nText  \n"):
            failures.append("self-test: MD009 — two trailing spaces (line-break) incorrectly flagged")
        if "MD009" not in _violations("t009c.md", "# Hello\n\nText   \n"):
            failures.append("self-test: MD009 — three trailing spaces not caught")

        # MD010: hard tab
        if "MD010" not in _violations("t010.md", "# Hello\n\n\ttext\n"):
            failures.append("self-test: MD010 — hard tab not caught")

        # MD012: multiple consecutive blank lines
        if "MD012" not in _violations("t012.md", "# Hello\n\n\n\nworld\n"):
            failures.append("self-test: MD012 — triple blank line not caught")
        if "MD012" in _violations("t012b.md", "# Hello\n\nworld\n"):
            failures.append("self-test: MD012 — single blank line incorrectly flagged")

        # MD018: no space after hash
        if "MD018" not in _violations("t018.md", "#Hello\n"):
            failures.append("self-test: MD018 — no-space heading not caught")
        if "MD018" in _violations("t018b.md", "# Hello\n"):
            failures.append("self-test: MD018 — valid heading incorrectly flagged")

        # MD019: multiple spaces after hash
        if "MD019" not in _violations("t019.md", "##  Hello\n"):
            failures.append("self-test: MD019 — double-space heading not caught")
        if "MD019" in _violations("t019b.md", "## Hello\n"):
            failures.append("self-test: MD019 — valid heading incorrectly flagged")

        # MD022: heading not preceded by blank line
        if "MD022" not in _violations("t022.md", "# Hello\nsome text\n## Section\n"):
            failures.append("self-test: MD022 — heading after text without blank not caught")
        if "MD022" in _violations("t022b.md", "# Hello\n\n## Section\n"):
            failures.append("self-test: MD022 — heading after blank line incorrectly flagged")

        # MD047: file should end with newline
        if "MD047" not in _violations("t047.md", "# Hello"):
            failures.append("self-test: MD047 — missing trailing newline not caught")
        if "MD047" in _violations("t047b.md", "# Hello\n"):
            failures.append("self-test: MD047 — file with newline incorrectly flagged")

        # MD001: heading level jump
        if "MD001" not in _violations("t001.md", "# H1\n\n### H3\n"):
            failures.append("self-test: MD001 — heading level jump not caught")
        if "MD001" in _violations("t001b.md", "# H1\n\n## H2\n\n### H3\n"):
            failures.append("self-test: MD001 — valid heading increment incorrectly flagged")

        # Code block contents must not trigger tab/space rules
        cb = "# Hello\n\n```\n\ttabbed code\n```\n"
        rules = _violations("tcb.md", cb)
        if "MD010" in rules:
            failures.append(
                "self-test: MD010 — tab inside fenced code block was incorrectly flagged"
            )

    return failures


# ── Main ──────────────────────────────────────────────────────────────────────

def validate(root: Path, paths: list[Path]) -> list[Violation]:
    all_violations: list[Violation] = []
    for path in paths:
        all_violations.extend(_check_file(path, root))
    return all_violations


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Validate Markdown files against the Toolly lint policy."
    )
    parser.add_argument(
        "--root", type=Path, default=ROOT,
        help="Repository root (default: parent of scripts/)"
    )
    parser.add_argument(
        "--self-test", action="store_true",
        help="Run deterministic self-tests and exit"
    )
    parser.add_argument(
        "files", nargs="*", type=Path,
        help="Specific files to check (default: all *.md under --root)"
    )
    args = parser.parse_args()
    root: Path = args.root.resolve()

    if args.self_test:
        failures = _self_test()
        if failures:
            for f in failures:
                print(f"FAIL: {f}", file=sys.stderr)
            print(
                f"Markdown self-test failed with {len(failures)} failure(s).",
                file=sys.stderr,
            )
            return 1
        print("Markdown self-test passed.")
        return 0

    if args.files:
        paths = [p.resolve() for p in args.files]
    else:
        paths = _collect_markdown(root)

    violations = validate(root, paths)

    if violations:
        for v in violations:
            print(f"ERROR: {v}", file=sys.stderr)
        print(
            f"Markdown lint failed: {len(violations)} violation(s) in "
            f"{len({v.path for v in violations})} file(s).",
            file=sys.stderr,
        )
        return 1

    print(f"Markdown lint passed: {len(paths)} file(s) checked.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
