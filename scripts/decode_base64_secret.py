#!/usr/bin/env python3
"""Decode a base64 CI secret into a file.

Used by CI to materialize Firebase config files (google-services.json,
GoogleService-Info.plist) from repo secrets before the Android/iOS build
steps that need them. Kept as a real committed script rather than an
inline `python3 -c "<multi-line>"` argument or a bash heredoc: both of
those forms, combined with an `actions/cache` step earlier in the same
job, were found to silently break GitHub's own workflow registration
for this repo (zero jobs ever scheduled, workflow_dispatch rejected as
absent, registered name falling back to the raw file path) -- root
caused by bisecting a disposable diagnostic workflow. See git history
on .github/workflows/android.yml and multiplatform.yml around
2026-08-14 for the investigation.

Whitespace (including any stray CR characters picked up when a secret
value is copied via a Windows clipboard) is stripped from the input
before decoding, so this is robust regardless of which line-ending
convention produced the pasted value.
"""

from __future__ import annotations

import base64
import os
import sys


def main() -> int:
    if len(sys.argv) != 3:
        print(
            "usage: decode_base64_secret.py <ENV_VAR_NAME> <OUTPUT_PATH>",
            file=sys.stderr,
        )
        return 2

    env_var_name, output_path = sys.argv[1], sys.argv[2]
    raw = os.environ.get(env_var_name)
    if not raw:
        print(f"::warning::{env_var_name} is not set; skipping.", file=sys.stderr)
        return 0

    cleaned = "".join(raw.split())
    with open(output_path, "wb") as handle:
        handle.write(base64.b64decode(cleaned))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
