#!/usr/bin/env python3
"""Emit generated Gradle evidence as base64 for one-time CI bootstrap review."""

from __future__ import annotations

import base64
from pathlib import Path


FILES = (
    Path("gradle/verification-metadata.xml"),
    Path("spike-capture/gradle.lockfile"),
)


def main() -> int:
    for path in FILES:
        if not path.is_file():
            raise SystemExit(f"Missing generated evidence: {path}")
        payload = base64.b64encode(path.read_bytes()).decode("ascii")
        print(f"::group::TOOLLY_GRADLE_EVIDENCE {path}")
        print(payload)
        print("::endgroup::")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
