#!/usr/bin/env python3
"""Fail CI when the Android app packages a different Compose ABI than shared UI."""

from __future__ import annotations

import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RUNTIME_CONFIGURATIONS = {"debugRuntimeClasspath", "releaseRuntimeClasspath"}
RUNTIME_MODULE = "androidx.compose.runtime:runtime-android"


def compose_versions(path: Path) -> dict[str, set[str]]:
    versions: dict[str, set[str]] = {}
    for raw_line in path.read_text(encoding="utf-8").splitlines():
        if not raw_line.startswith("androidx.compose.") or "=" not in raw_line:
            continue
        dependency, raw_configurations = raw_line.split("=", 1)
        if not RUNTIME_CONFIGURATIONS.intersection(raw_configurations.split(",")):
            continue
        module, version = dependency.rsplit(":", 1)
        versions.setdefault(module, set()).add(version)
    return versions


def main() -> int:
    shared = compose_versions(ROOT / "shared-ui/gradle.lockfile")
    android = compose_versions(ROOT / "spike-capture/gradle.lockfile")
    errors: list[str] = []

    for name, versions in (("shared-ui", shared), ("spike-capture", android)):
        if RUNTIME_MODULE not in versions:
            errors.append(f"{name}: missing {RUNTIME_MODULE}")
        for module, resolved in sorted(versions.items()):
            if len(resolved) != 1:
                errors.append(
                    f"{name}: {module} resolves multiple runtime versions: "
                    + ", ".join(sorted(resolved))
                )

    for module in sorted(shared.keys() & android.keys()):
        if shared[module] != android[module]:
            errors.append(
                f"{module}: shared-ui={sorted(shared[module])}, "
                f"spike-capture={sorted(android[module])}"
            )

    if errors:
        for error in errors:
            print(f"ERROR: Compose runtime alignment: {error}", file=sys.stderr)
        return 1

    runtime_version = next(iter(android[RUNTIME_MODULE]))
    print(f"Compose runtime alignment valid: Android and shared UI use {runtime_version}.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
