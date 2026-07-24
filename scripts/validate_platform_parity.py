#!/usr/bin/env python3
"""Validate Toolly platform parity and production user-facing string boundaries."""

from __future__ import annotations

import argparse
import json
import re
import sys
import tempfile
from pathlib import Path
from typing import Any


DEFAULT_ROOT = Path(__file__).resolve().parents[1]
POLICY_PATH = Path("config/platform/parity.json")
VALID_STATUSES = {"pending", "in-progress", "candidate", "implemented", "verified"}
TEST_SOURCE_PARTS = {
    "test",
    "androidTest",
    "commonTest",
    "iosTest",
    "jvmTest",
    "testFixtures",
}

KOTLIN_USER_TEXT_PATTERNS = (
    re.compile(r"\bText\s*\(\s*(?:text\s*=\s*)?\""),
    re.compile(r"\bcontentDescription\s*=\s*\""),
    re.compile(r"\b(?:showSnackbar|makeText|setText)\s*\([^;\n]*\""),
)
MANIFEST_USER_TEXT_PATTERN = re.compile(
    r'android:(?:label|description|hint|text)="(?!@string/)[^"]+"'
)


def load_policy(root: Path) -> dict[str, Any]:
    with (root / POLICY_PATH).open(encoding="utf-8") as handle:
        return json.load(handle)


def validate_policy(policy: dict[str, Any]) -> list[str]:
    errors: list[str] = []
    if policy.get("schema_version") != 1:
        errors.append("parity policy schema_version must be 1")
    if policy.get("policy_id") != "toolly-platform-parity":
        errors.append("parity policy_id is invalid")
    if policy.get("platforms") != ["android", "ios"]:
        errors.append("platforms must be exactly android and ios")

    shared_ui = policy.get("shared_ui")
    if not isinstance(shared_ui, dict):
        errors.append("shared_ui must be an object")
    else:
        if shared_ui.get("framework") != "compose-multiplatform":
            errors.append("Compose Multiplatform must own shared Toolly UI")
        if shared_ui.get("locales") != ["en-IN", "hi-IN", "kn-IN"]:
            errors.append("shared locales must be en-IN, hi-IN and kn-IN")

    differences = policy.get("allowed_platform_differences")
    if not isinstance(differences, list) or not differences:
        errors.append("allowed_platform_differences must be a non-empty list")
    else:
        seen_difference_ids: set[str] = set()
        for index, item in enumerate(differences):
            location = f"allowed_platform_differences[{index}]"
            if not isinstance(item, dict):
                errors.append(f"{location} must be an object")
                continue
            difference_id = item.get("id")
            if not isinstance(difference_id, str) or not difference_id:
                errors.append(f"{location}.id is required")
            elif difference_id in seen_difference_ids:
                errors.append(f"{location}.id is duplicated")
            else:
                seen_difference_ids.add(difference_id)
            for key in ("android_adapter", "ios_adapter", "shared_contract"):
                if not isinstance(item.get(key), str) or not item[key]:
                    errors.append(f"{location}.{key} is required")

    features = policy.get("features")
    if not isinstance(features, list) or not features:
        errors.append("features must be a non-empty list")
    else:
        seen_feature_ids: set[str] = set()
        for index, item in enumerate(features):
            location = f"features[{index}]"
            if not isinstance(item, dict):
                errors.append(f"{location} must be an object")
                continue
            feature_id = item.get("id")
            if not isinstance(feature_id, str) or not feature_id:
                errors.append(f"{location}.id is required")
            elif feature_id in seen_feature_ids:
                errors.append(f"{location}.id is duplicated")
            else:
                seen_feature_ids.add(feature_id)

            android = item.get("android")
            ios = item.get("ios")
            if android not in VALID_STATUSES:
                errors.append(f"{location}.android has an invalid status")
            if ios not in VALID_STATUSES:
                errors.append(f"{location}.ios has an invalid status")
            tracking_issue = item.get("tracking_issue")
            if not isinstance(tracking_issue, int) or tracking_issue <= 0:
                errors.append(f"{location}.tracking_issue must be a positive issue number")
            if android != ios:
                reason = item.get("gap_reason")
                if not isinstance(reason, str) or not reason.strip():
                    errors.append(f"{location} has an unexplained platform gap")
    return errors


def is_production_source(path: Path) -> bool:
    return not any(part in TEST_SOURCE_PARTS for part in path.parts)


def validate_user_facing_strings(root: Path) -> list[str]:
    errors: list[str] = []
    for path in sorted(root.rglob("*.kt")):
        if not is_production_source(path):
            continue
        if any(part in {".git", ".gradle", "build"} for part in path.parts):
            continue
        content = path.read_text(encoding="utf-8", errors="replace")
        for line_number, line in enumerate(content.splitlines(), start=1):
            if any(pattern.search(line) for pattern in KOTLIN_USER_TEXT_PATTERNS):
                errors.append(
                    f"{path.relative_to(root)}:{line_number}: "
                    "hard-coded user-facing text; use a localized resource"
                )

    for path in sorted(root.rglob("AndroidManifest.xml")):
        if not is_production_source(path):
            continue
        content = path.read_text(encoding="utf-8", errors="replace")
        for line_number, line in enumerate(content.splitlines(), start=1):
            if MANIFEST_USER_TEXT_PATTERN.search(line):
                errors.append(
                    f"{path.relative_to(root)}:{line_number}: "
                    "hard-coded manifest text; use @string resource"
                )
    return errors


def self_test() -> list[str]:
    failures: list[str] = []
    valid_policy = {
        "schema_version": 1,
        "policy_id": "toolly-platform-parity",
        "platforms": ["android", "ios"],
        "shared_ui": {
            "framework": "compose-multiplatform",
            "locales": ["en-IN", "hi-IN", "kn-IN"],
        },
        "allowed_platform_differences": [
            {
                "id": "capture",
                "android_adapter": "android",
                "ios_adapter": "ios",
                "shared_contract": "Capture",
            }
        ],
        "features": [
            {
                "id": "library",
                "android": "candidate",
                "ios": "pending",
                "gap_reason": "Tracked implementation gap.",
                "tracking_issue": 38,
            }
        ],
    }
    if validate_policy(valid_policy):
        failures.append("self-test: valid parity policy was rejected")

    unexplained = json.loads(json.dumps(valid_policy))
    unexplained["features"][0]["gap_reason"] = None
    if not validate_policy(unexplained):
        failures.append("self-test: unexplained platform gap was accepted")

    with tempfile.TemporaryDirectory() as temporary:
        root = Path(temporary)
        production = root / "app/src/main/kotlin/Screen.kt"
        production.parent.mkdir(parents=True)
        production.write_text('fun screen() { Text("Literal") }\n', encoding="utf-8")
        if not validate_user_facing_strings(root):
            failures.append("self-test: production UI literal was accepted")

        production.write_text(
            "fun screen() { Text(stringResource(AppString.title)) }\n",
            encoding="utf-8",
        )
        fixture = root / "app/src/test/kotlin/Fixture.kt"
        fixture.parent.mkdir(parents=True)
        fixture.write_text('val fixture = "test-only"\n', encoding="utf-8")
        if validate_user_facing_strings(root):
            failures.append("self-test: localized UI or test fixture was rejected")
    return failures


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=DEFAULT_ROOT)
    parser.add_argument("--self-test", action="store_true")
    args = parser.parse_args()

    root = args.root.resolve()
    errors: list[str] = []
    try:
        policy = load_policy(root)
    except (OSError, json.JSONDecodeError) as error:
        errors.append(f"cannot load parity policy: {error}")
    else:
        errors.extend(validate_policy(policy))
        errors.extend(validate_user_facing_strings(root))
    if args.self_test:
        errors.extend(self_test())

    if errors:
        for error in errors:
            print(f"ERROR: {error}", file=sys.stderr)
        print(
            f"Platform parity validation failed with {len(errors)} error(s).",
            file=sys.stderr,
        )
        return 1

    print("Platform parity and localized-string boundaries are valid.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
