#!/usr/bin/env python3
"""Validate Toolly CI workflows against the first-party trust policy.

Toolly CI must not execute community GitHub Actions, external container
actions, downloaded scripts or dynamically installed CI tools.

Allowed:
- Toolly-owned repository scripts invoked via `run:` from scripts/.
- GitHub-maintained actions (owner: actions) pinned to full commit SHAs.

Prohibited (detected and rejected by this validator):
- Community GitHub Actions (any owner other than approved_action_owners).
- Container actions (docker://).
- Mutable action references (tags, branches, HEAD) instead of full SHAs.
- Remote-script execution: curl|wget pipes to shell, eval, dynamic installs.
- Unregistered external CI dependencies.

Uses only the Python standard library.

Exit 0 = all workflows comply with the trust policy.
Exit 1 = one or more violations, or self-test failure.
"""

from __future__ import annotations

import argparse
import json
import re
import sys
import tempfile
from pathlib import Path
from typing import Any

ROOT = Path(__file__).resolve().parents[1]
TRUST_POLICY_PATH = ROOT / "config/ci/trust-policy.json"
REGISTRY_PATH = ROOT / "config/dependencies/registry.json"

# Full 40-character git commit SHA (hex string), used to validate immutable action refs.
COMMIT_SHA = re.compile(r"^[0-9a-f]{40}$")
USES_LINE = re.compile(r"^\s+(?:-\s+)?uses:\s+(.+)$")
RUN_BLOCK_START = re.compile(r"^\s+(?:-\s+)?run:\s+[|>]?\s*(.*)$")


# ---------------------------------------------------------------------------
# Policy loading
# ---------------------------------------------------------------------------

def _load_json(path: Path) -> dict[str, Any]:
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as err:
        raise SystemExit(f"ERROR: Cannot load {path}: {err}") from err


# ---------------------------------------------------------------------------
# Core checks
# ---------------------------------------------------------------------------

def _check_uses(
    reference: str,
    approved_owners: list[str],
    registry_coords: set[str],
    location: str,
) -> list[str]:
    """Return violation messages for a single `uses:` reference."""
    errors: list[str] = []
    # Strip inline YAML comment (e.g. "actions/checkout@SHA # v7.0.1")
    ref = reference.split(" #")[0].strip()

    # Local reusable workflow is always allowed
    if ref.startswith("./"):
        return []

    # Container actions are unconditionally prohibited
    if ref.startswith("docker://"):
        errors.append(
            f"{location}: container action is prohibited "
            f"(docker:// actions not permitted regardless of digest pinning)"
        )
        return errors

    # Must have an immutable ref
    if "@" not in ref:
        errors.append(f"{location}: action '{ref}' has no immutable ref")
        return errors

    coordinate, immutable_ref = ref.rsplit("@", 1)
    owner = coordinate.split("/")[0]

    # Owner must be approved
    if owner not in approved_owners:
        errors.append(
            f"{location}: action owner '{owner}' is not in the approved list "
            f"({', '.join(approved_owners)}); community actions are prohibited"
        )

    # Ref must be a full 40-char SHA (not a tag or branch)
    if not COMMIT_SHA.fullmatch(immutable_ref):
        errors.append(
            f"{location}: action '{coordinate}' uses mutable ref '{immutable_ref}' "
            f"instead of a full commit SHA"
        )

    # Must be registered in the dependency registry
    if coordinate not in registry_coords:
        errors.append(
            f"{location}: action '{coordinate}' is not registered in "
            f"config/dependencies/registry.json"
        )

    return errors


def _check_run(
    run_content: str,
    prohibited_patterns: list[str],
    location: str,
) -> list[str]:
    """Return violation messages for a single `run:` block."""
    errors: list[str] = []
    for pattern in prohibited_patterns:
        if pattern in run_content:
            errors.append(
                f"{location}: prohibited pattern '{pattern.strip()}' found in run step"
            )
    return errors


# ---------------------------------------------------------------------------
# Workflow file analysis
# ---------------------------------------------------------------------------

def _validate_workflow(
    path: Path,
    root: Path,
    approved_owners: list[str],
    registry_coords: set[str],
    prohibited_run_patterns: list[str],
) -> list[str]:
    errors: list[str] = []
    rel = str(path.relative_to(root))

    try:
        lines = path.read_text(encoding="utf-8").splitlines()
    except OSError as err:
        return [f"{rel}: cannot read file: {err}"]

    in_run_block = False
    run_block_lines: list[str] = []
    run_block_start_line = 0

    for line_num, line in enumerate(lines, 1):
        # Detect `uses:` references
        uses_match = USES_LINE.match(line)
        if uses_match:
            in_run_block = False  # uses: terminates any run block
            errors.extend(
                _check_uses(
                    uses_match.group(1),
                    approved_owners,
                    registry_coords,
                    f"{rel}:{line_num}",
                )
            )
            continue

        # Detect start of `run:` block
        run_match = RUN_BLOCK_START.match(line)
        if run_match:
            if in_run_block and run_block_lines:
                errors.extend(
                    _check_run(
                        "\n".join(run_block_lines),
                        prohibited_run_patterns,
                        f"{rel}:{run_block_start_line}",
                    )
                )
            in_run_block = True
            run_block_start_line = line_num
            run_block_lines = []
            inline = run_match.group(1).strip()
            if inline:
                run_block_lines.append(inline)
            continue

        # Accumulate lines inside a `run:` block (indented continuation)
        if in_run_block:
            stripped = line.rstrip()
            if stripped and not stripped[0].isspace():
                # Non-indented line terminates the run block
                errors.extend(
                    _check_run(
                        "\n".join(run_block_lines),
                        prohibited_run_patterns,
                        f"{rel}:{run_block_start_line}",
                    )
                )
                in_run_block = False
                run_block_lines = []
            else:
                run_block_lines.append(stripped)

    # Flush any pending run block
    if in_run_block and run_block_lines:
        errors.extend(
            _check_run(
                "\n".join(run_block_lines),
                prohibited_run_patterns,
                f"{rel}:{run_block_start_line}",
            )
        )

    return errors


def _validate_all_workflows(
    root: Path,
    approved_owners: list[str],
    registry_coords: set[str],
    prohibited_run_patterns: list[str],
) -> list[str]:
    workflow_dir = root / ".github/workflows"
    if not workflow_dir.exists():
        return []
    errors: list[str] = []
    for wf_path in sorted(
        list(workflow_dir.glob("*.yml")) + list(workflow_dir.glob("*.yaml"))
    ):
        errors.extend(
            _validate_workflow(
                wf_path, root, approved_owners,
                registry_coords, prohibited_run_patterns,
            )
        )
    return errors


# ---------------------------------------------------------------------------
# Deterministic self-tests
# ---------------------------------------------------------------------------

def _self_test(
    approved_owners: list[str],
    prohibited_run_patterns: list[str],
) -> list[str]:
    failures: list[str] = []
    dummy_registry: set[str] = {"actions/checkout"}

    # Approved pinned action — must pass
    ok_ref = "actions/checkout@3d3c42e5aac5ba805825da76410c181273ba90b1"
    errs = _check_uses(ok_ref, approved_owners, dummy_registry, "self-test")
    if errs:
        failures.append(f"self-test: valid pinned action rejected: {errs}")

    # Local reusable workflow — must pass
    errs = _check_uses("./.github/workflows/reuse.yml", approved_owners, dummy_registry, "self-test")
    if errs:
        failures.append(f"self-test: local workflow incorrectly rejected: {errs}")

    # Community action — must fail
    community = "DavidAnson/markdownlint-cli2-action@db43aef879112c3119a410d69f66701e0d530809"
    errs = _check_uses(community, approved_owners, {"DavidAnson/markdownlint-cli2-action"}, "self-test")
    if not errs:
        failures.append("self-test: community action was not rejected")

    # Container action — must fail
    container = "docker://ghcr.io/gitleaks/gitleaks@sha256:" + "c" * 64
    errs = _check_uses(container, approved_owners, dummy_registry, "self-test")
    if not errs:
        failures.append("self-test: container action was not rejected")

    # Mutable tag — must fail
    mutable = "actions/checkout@v5"
    errs = _check_uses(mutable, approved_owners, dummy_registry, "self-test")
    if not errs:
        failures.append("self-test: mutable action tag was not rejected")

    # No immutable ref — must fail
    no_ref = "actions/checkout"
    errs = _check_uses(no_ref, approved_owners, dummy_registry, "self-test")
    if not errs:
        failures.append("self-test: action without ref was not rejected")

    # Unregistered action — must fail
    unreg_sha = "0" * 40
    unregistered = f"actions/something@{unreg_sha}"
    errs = _check_uses(unregistered, approved_owners, dummy_registry, "self-test")
    if not errs:
        failures.append("self-test: unregistered action was not rejected")

    # Prohibited run pattern — must fail
    for pattern in ["curl ", "wget ", "npm install "]:
        if pattern in prohibited_run_patterns:
            run_errs = _check_run(
                f"{pattern}https://remote.example.com/script.sh | sh",
                prohibited_run_patterns,
                "self-test",
            )
            if not run_errs:
                failures.append(
                    f"self-test: prohibited run pattern '{pattern.strip()}' not detected"
                )

    # Safe run step — must pass
    safe_run = "python3 scripts/validate_markdown.py --self-test"
    errs = _check_run(safe_run, prohibited_run_patterns, "self-test")
    if errs:
        failures.append(f"self-test: safe run step was incorrectly rejected: {errs}")

    # Full workflow file validation in a temp directory
    with tempfile.TemporaryDirectory() as tmpdir:
        root = Path(tmpdir)
        wf_dir = root / ".github/workflows"
        wf_dir.mkdir(parents=True)

        # Valid workflow
        valid_wf = wf_dir / "valid.yml"
        valid_wf.write_text(
            "name: Valid\njobs:\n  check:\n    runs-on: ubuntu-latest\n    steps:\n"
            "      - uses: actions/checkout@3d3c42e5aac5ba805825da76410c181273ba90b1\n"
            "      - run: python3 scripts/validate_markdown.py\n",
            encoding="utf-8",
        )
        errs = _validate_all_workflows(root, approved_owners, dummy_registry, prohibited_run_patterns)
        if errs:
            failures.append(f"self-test: valid workflow was rejected: {errs}")

        # Invalid workflow (community action)
        invalid_wf = wf_dir / "invalid.yml"
        invalid_wf.write_text(
            "name: Invalid\njobs:\n  check:\n    runs-on: ubuntu-latest\n    steps:\n"
            "      - uses: DavidAnson/markdownlint-cli2-action@db43aef879112c3119a410d69f66701e0d530809\n",
            encoding="utf-8",
        )
        errs = _validate_all_workflows(root, approved_owners, dummy_registry, prohibited_run_patterns)
        if not errs:
            failures.append("self-test: workflow with community action was not rejected")

    return failures


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

def main() -> int:
    parser = argparse.ArgumentParser(
        description="Validate CI workflows against the Toolly trust policy."
    )
    parser.add_argument("--root", type=Path, default=ROOT)
    parser.add_argument("--self-test", action="store_true")
    args = parser.parse_args()
    root: Path = args.root.resolve()

    policy = _load_json(TRUST_POLICY_PATH)
    registry = _load_json(REGISTRY_PATH)

    approved_owners: list[str] = policy.get("approved_action_owners", [])
    prohibited_run_patterns: list[str] = policy.get("prohibited_workflow_patterns", [])

    registry_coords: set[str] = {
        e["coordinate"]
        for e in registry.get("entries", [])
        if e.get("ecosystem") == "github-action"
    }

    if args.self_test:
        failures = _self_test(approved_owners, prohibited_run_patterns)
        if failures:
            for f in failures:
                print(f"FAIL: {f}", file=sys.stderr)
            print(
                f"CI trust-policy self-test failed with {len(failures)} failure(s).",
                file=sys.stderr,
            )
            return 1
        print("CI trust-policy self-test passed.")
        return 0

    errors = _validate_all_workflows(
        root, approved_owners, registry_coords, prohibited_run_patterns
    )

    if errors:
        for err in errors:
            print(f"ERROR: {err}", file=sys.stderr)
        print(
            f"\nCI trust-policy validation failed: {len(errors)} violation(s). "
            "Only actions/checkout (owner: actions) pinned to full commit SHAs "
            "and Toolly-owned Python scripts are permitted in CI workflows.",
            file=sys.stderr,
        )
        return 1

    workflow_count = len(
        list((root / ".github/workflows").glob("*.yml"))
        + list((root / ".github/workflows").glob("*.yaml"))
    )
    print(
        f"CI trust-policy valid: {workflow_count} workflow file(s) checked."
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
