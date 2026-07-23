#!/usr/bin/env python3
"""Validate Toolly dependency approvals, manifests and source boundaries."""

from __future__ import annotations

import argparse
import datetime as dt
import json
import re
import sys
import tempfile
import tomllib
import uuid
from pathlib import Path
from typing import Any


DEFAULT_ROOT = Path(__file__).resolve().parents[1]
SHA40 = re.compile(r"^[0-9a-f]{40}$")
SHA256_REF = re.compile(r"^sha256:[0-9a-f]{64}$")
SAFE_ID = re.compile(r"^[A-Za-z0-9][A-Za-z0-9._/-]*$")
USES = re.compile(r"^\s*uses:\s*([^\s#]+)", re.MULTILINE)
GRADLE_COORDINATE = re.compile(
    r"""["']([A-Za-z0-9_.-]+):([A-Za-z0-9_.-]+):([^"' \t]+)["']"""
)

REGISTRY_REQUIRED = {
    "id",
    "ecosystem",
    "coordinate",
    "requested_version",
    "immutable_ref",
    "scope",
    "purpose",
    "owner",
    "upstream",
    "licence_spdx",
    "licence_evidence",
    "approval_status",
    "approval_issue",
    "approved_by",
    "approved_at",
    "review_due_at",
    "cve_review",
    "transitives",
    "size",
    "data_processing",
    "alternatives",
    "removal_plan",
}


def load_json(path: Path) -> Any:
    with path.open("r", encoding="utf-8") as handle:
        return json.load(handle)


def required_keys(value: Any, keys: set[str], location: str) -> list[str]:
    if not isinstance(value, dict):
        return [f"{location}: expected object"]
    return [
        f"{location}: missing key '{key}'"
        for key in sorted(keys - set(value))
    ]


def parse_date(value: Any, location: str) -> tuple[dt.date | None, list[str]]:
    try:
        return dt.date.fromisoformat(str(value)), []
    except ValueError:
        return None, [f"{location}: expected YYYY-MM-DD date"]


def contains_dynamic_version(version: str, policy: dict[str, Any]) -> bool:
    return any(
        token.lower() in version.lower()
        for token in policy["versions"]["prohibited_patterns"]
    )


def registry_key(ecosystem: str, coordinate: str) -> tuple[str, str]:
    return ecosystem.lower(), coordinate.lower()


def validate_policy(policy: Any, location: str) -> list[str]:
    errors = required_keys(
        policy,
        {
            "schema_version",
            "policy_id",
            "policy_version",
            "current_phase",
            "versions",
            "repositories",
            "licences",
            "vulnerabilities",
            "ci",
            "coordinates",
            "source_boundaries",
            "domain_forbidden_tokens",
            "domain_path_fragments",
        },
        location,
    )
    if errors:
        return errors
    if policy["schema_version"] != 1:
        errors.append(f"{location}: unsupported schema_version")
    phase = policy["current_phase"]
    if phase.get("cloud_provider") != "firebase":
        errors.append(f"{location}: current cloud provider must remain firebase")
    if phase.get("aws_dependencies_allowed") is not False:
        errors.append(f"{location}: AWS dependencies must be disabled")
    allowed = set(policy["licences"]["engineering_allowed"])
    review = set(policy["licences"]["legal_review_required"])
    prohibited = set(
        policy["licences"]["prohibited_without_explicit_legal_and_product_exception"]
    )
    overlap = (allowed & review) | (allowed & prohibited) | (review & prohibited)
    if overlap:
        errors.append(f"{location}: licence classes overlap: {sorted(overlap)}")
    return errors


def validate_registry(
    registry: Any, policy: dict[str, Any], location: str, today: dt.date
) -> tuple[dict[tuple[str, str], dict[str, Any]], list[str]]:
    errors = required_keys(
        registry,
        {"schema_version", "registry_id", "registry_version", "entries"},
        location,
    )
    indexed: dict[tuple[str, str], dict[str, Any]] = {}
    if errors:
        return indexed, errors
    if not isinstance(registry["entries"], list):
        return indexed, [f"{location}: entries must be an array"]

    entry_ids: set[str] = set()
    allowed_licences = set(policy["licences"]["engineering_allowed"])
    maximum_age = int(policy["vulnerabilities"]["maximum_review_age_days"])
    prohibited_fragments = [
        value.lower()
        for value in policy["coordinates"]["prohibited_fragments_current_phase"]
    ]

    for index, entry in enumerate(registry["entries"]):
        item_location = f"{location}:entries[{index}]"
        errors.extend(required_keys(entry, REGISTRY_REQUIRED, item_location))
        if not isinstance(entry, dict):
            continue
        entry_id = entry.get("id")
        ecosystem = str(entry.get("ecosystem", ""))
        coordinate = str(entry.get("coordinate", ""))
        version = str(entry.get("requested_version", ""))
        immutable_ref = str(entry.get("immutable_ref", ""))

        if not isinstance(entry_id, str) or not SAFE_ID.fullmatch(entry_id):
            errors.append(f"{item_location}: invalid id")
        elif entry_id in entry_ids:
            errors.append(f"{item_location}: duplicate id '{entry_id}'")
        else:
            entry_ids.add(entry_id)

        key = registry_key(ecosystem, coordinate)
        if key in indexed:
            errors.append(f"{item_location}: duplicate ecosystem/coordinate")
        else:
            indexed[key] = entry

        if contains_dynamic_version(version, policy):
            errors.append(f"{item_location}: mutable requested_version '{version}'")
        if any(fragment in coordinate.lower() for fragment in prohibited_fragments):
            errors.append(f"{item_location}: coordinate prohibited in current phase")
        if ecosystem == "github-action" and not SHA40.fullmatch(immutable_ref):
            errors.append(f"{item_location}: GitHub Action requires full commit SHA")
        if ecosystem == "container-action" and not SHA256_REF.fullmatch(immutable_ref):
            errors.append(f"{item_location}: container requires sha256 digest")
        if entry.get("approval_status") == "approved":
            if entry.get("licence_spdx") not in allowed_licences:
                errors.append(
                    f"{item_location}: approved dependency licence is not engineering-allowed"
                )
            if not entry.get("approved_by"):
                errors.append(f"{item_location}: approved_by is required")
        elif entry.get("approval_status") not in {"conditional", "rejected"}:
            errors.append(f"{item_location}: invalid approval_status")

        approved_at, date_errors = parse_date(
            entry.get("approved_at"), f"{item_location}:approved_at"
        )
        errors.extend(date_errors)
        review_due, date_errors = parse_date(
            entry.get("review_due_at"), f"{item_location}:review_due_at"
        )
        errors.extend(date_errors)
        if approved_at and review_due and review_due < approved_at:
            errors.append(f"{item_location}: review due before approval")
        if review_due and review_due < today:
            errors.append(f"{item_location}: dependency review expired on {review_due}")

        cve_review = entry.get("cve_review")
        errors.extend(
            required_keys(
                cve_review,
                {"status", "last_reviewed_at", "result", "sources"},
                f"{item_location}:cve_review",
            )
        )
        if isinstance(cve_review, dict):
            reviewed_at, date_errors = parse_date(
                cve_review.get("last_reviewed_at"),
                f"{item_location}:cve_review.last_reviewed_at",
            )
            errors.extend(date_errors)
            if reviewed_at and (today - reviewed_at).days > maximum_age:
                errors.append(f"{item_location}: vulnerability review is stale")
            if not cve_review.get("sources"):
                errors.append(f"{item_location}: vulnerability sources required")

        if not isinstance(entry.get("alternatives"), list) or not entry["alternatives"]:
            errors.append(f"{item_location}: at least one alternative is required")
        if not str(entry.get("removal_plan", "")).strip():
            errors.append(f"{item_location}: removal plan is required")
        data_processing = entry.get("data_processing")
        errors.extend(
            required_keys(
                data_processing,
                {"processes_user_documents", "network_hosts", "permissions"},
                f"{item_location}:data_processing",
            )
        )
        if isinstance(data_processing, dict) and data_processing.get(
            "processes_user_documents"
        ) is not False:
            errors.append(
                f"{item_location}: user-document processing needs separate privacy approval"
            )
    return indexed, errors


def validate_exceptions(exceptions: Any, location: str, today: dt.date) -> list[str]:
    errors = required_keys(exceptions, {"schema_version", "exceptions"}, location)
    if errors:
        return errors
    for index, exception in enumerate(exceptions["exceptions"]):
        item_location = f"{location}:exceptions[{index}]"
        errors.extend(
            required_keys(
                exception,
                {
                    "id",
                    "rule",
                    "exact_target",
                    "reason",
                    "owner",
                    "issue",
                    "expires_at",
                    "compensating_control",
                },
                item_location,
            )
        )
        if not isinstance(exception, dict):
            continue
        target = str(exception.get("exact_target", ""))
        if not target or target in {"*", "**", "/"}:
            errors.append(f"{item_location}: broad exception target prohibited")
        expires_at, date_errors = parse_date(
            exception.get("expires_at"), f"{item_location}:expires_at"
        )
        errors.extend(date_errors)
        if expires_at and expires_at < today:
            errors.append(f"{item_location}: exception expired")
    return errors


def catalogue_coordinate(
    value: Any, versions: dict[str, Any]
) -> tuple[str | None, str | None]:
    if isinstance(value, str):
        parts = value.split(":")
        if len(parts) == 3:
            return ":".join(parts[:2]), parts[2]
        return None, None
    if not isinstance(value, dict):
        return None, None
    module = value.get("module")
    if not module and value.get("group") and value.get("name"):
        module = f"{value['group']}:{value['name']}"
    version = value.get("version")
    if isinstance(version, dict):
        version = version.get("require") or version.get("strictly") or version.get("prefer")
    if not version:
        version_ref = value.get("version.ref")
        if not version_ref and isinstance(value.get("version"), dict):
            version_ref = value["version"].get("ref")
        if version_ref:
            version = versions.get(version_ref)
    return str(module) if module else None, str(version) if version else None


def validate_catalog(
    catalog: Any,
    indexed: dict[tuple[str, str], dict[str, Any]],
    policy: dict[str, Any],
    location: str,
) -> list[str]:
    errors: list[str] = []
    if not isinstance(catalog, dict):
        return [f"{location}: expected TOML object"]
    versions = catalog.get("versions", {})
    for alias, value in catalog.get("libraries", {}).items():
        coordinate, version = catalogue_coordinate(value, versions)
        item_location = f"{location}:libraries.{alias}"
        if not coordinate or not version:
            errors.append(f"{item_location}: coordinate/version could not be resolved")
            continue
        entry = indexed.get(registry_key("gradle-library", coordinate))
        if not entry:
            errors.append(f"{item_location}: dependency is not registered")
            continue
        if version != str(entry["requested_version"]):
            errors.append(f"{item_location}: version differs from registry")
        if contains_dynamic_version(version, policy):
            errors.append(f"{item_location}: mutable version prohibited")
    for alias, value in catalog.get("plugins", {}).items():
        item_location = f"{location}:plugins.{alias}"
        if not isinstance(value, dict):
            errors.append(f"{item_location}: plugin must use structured form")
            continue
        plugin_id = value.get("id")
        version = value.get("version")
        if isinstance(version, dict):
            version = version.get("require") or version.get("ref")
            if version in versions:
                version = versions[version]
        entry = indexed.get(registry_key("gradle-plugin", str(plugin_id)))
        if not entry:
            errors.append(f"{item_location}: plugin is not registered")
            continue
        if str(version) != str(entry["requested_version"]):
            errors.append(f"{item_location}: version differs from registry")
    return errors


def workflow_reference_error(
    reference: str,
    indexed: dict[tuple[str, str], dict[str, Any]],
    location: str,
) -> list[str]:
    if reference.startswith("./"):
        return []
    if reference.startswith("docker://"):
        body = reference.removeprefix("docker://")
        if "@" not in body:
            return [f"{location}: container Action is not digest-pinned"]
        coordinate, immutable_ref = body.rsplit("@", 1)
        if not SHA256_REF.fullmatch(immutable_ref):
            return [f"{location}: container Action requires sha256 digest"]
        entry = indexed.get(registry_key("container-action", coordinate))
        if not entry:
            return [f"{location}: container Action is not registered"]
        if entry["immutable_ref"] != immutable_ref:
            return [f"{location}: container digest differs from registry"]
        return []
    if "@" not in reference:
        return [f"{location}: GitHub Action has no immutable ref"]
    coordinate, immutable_ref = reference.rsplit("@", 1)
    if not SHA40.fullmatch(immutable_ref):
        return [f"{location}: GitHub Action requires full commit SHA"]
    entry = indexed.get(registry_key("github-action", coordinate))
    if not entry:
        return [f"{location}: GitHub Action is not registered"]
    if entry["immutable_ref"] != immutable_ref:
        return [f"{location}: Action SHA differs from registry"]
    return []


def validate_workflows(
    root: Path, indexed: dict[tuple[str, str], dict[str, Any]]
) -> list[str]:
    errors: list[str] = []
    workflow_root = root / ".github/workflows"
    if not workflow_root.exists():
        return errors
    for path in sorted(
        list(workflow_root.glob("*.yml")) + list(workflow_root.glob("*.yaml"))
    ):
        content = path.read_text(encoding="utf-8")
        for line_number, line in enumerate(content.splitlines(), start=1):
            match = USES.match(line)
            if match:
                errors.extend(
                    workflow_reference_error(
                        match.group(1),
                        indexed,
                        f"{path.relative_to(root)}:{line_number}",
                    )
                )
    return errors


def validate_build_manifests(
    root: Path,
    indexed: dict[tuple[str, str], dict[str, Any]],
    policy: dict[str, Any],
) -> list[str]:
    errors: list[str] = []
    build_files = sorted(root.rglob("*.gradle")) + sorted(root.rglob("*.gradle.kts"))
    for path in build_files:
        if any(part in {".git", "build", ".gradle"} for part in path.parts):
            continue
        content = path.read_text(encoding="utf-8")
        relative = path.relative_to(root)
        for token in policy["repositories"]["prohibited_tokens"]:
            if token in content:
                errors.append(f"{relative}: prohibited repository token '{token}'")
        for match in GRADLE_COORDINATE.finditer(content):
            coordinate = f"{match.group(1)}:{match.group(2)}"
            version = match.group(3)
            if not indexed.get(registry_key("gradle-library", coordinate)):
                errors.append(f"{relative}: unregistered direct coordinate '{coordinate}'")
            if contains_dynamic_version(version, policy):
                errors.append(f"{relative}: mutable version '{version}'")
    if build_files:
        verification = root / "gradle/verification-metadata.xml"
        lock_files = [
            path
            for path in root.rglob("*lockfile")
            if ".gradle" not in path.parts and "build" not in path.parts
        ]
        if not verification.exists():
            errors.append(
                "Gradle build exists but gradle/verification-metadata.xml is missing"
            )
        if not lock_files:
            errors.append("Gradle build exists but no committed dependency lockfile exists")

    if (root / "Podfile").exists() and not (root / "Podfile.lock").exists():
        errors.append("Podfile exists but Podfile.lock is missing")
    if (root / "Package.swift").exists() and not (root / "Package.resolved").exists():
        errors.append("Package.swift exists but Package.resolved is missing")
    if (root / "package.json").exists() and not any(
        (root / name).exists()
        for name in ("package-lock.json", "pnpm-lock.yaml", "yarn.lock")
    ):
        errors.append("package.json exists but no supported lockfile is committed")

    gitignore = root / ".gitignore"
    if gitignore.exists():
        ignored = {
            line.strip()
            for line in gitignore.read_text(encoding="utf-8").splitlines()
            if line.strip() and not line.lstrip().startswith("#")
        }
        for lock_name in ("Podfile.lock", "Package.resolved", "gradle.lockfile"):
            if lock_name in ignored:
                errors.append(f".gitignore: required lockfile '{lock_name}' is ignored")
    return errors


def validate_source_boundaries(root: Path, policy: dict[str, Any]) -> list[str]:
    errors: list[str] = []
    extensions = {".kt", ".java", ".swift", ".m", ".mm"}
    for path in sorted(root.rglob("*")):
        if not path.is_file() or path.suffix not in extensions:
            continue
        if any(part in {".git", "build", ".gradle", "Pods"} for part in path.parts):
            continue
        relative = "/" + path.relative_to(root).as_posix()
        content = path.read_text(encoding="utf-8", errors="replace")
        for rule in policy["source_boundaries"]:
            if not any(token in content for token in rule["tokens"]):
                continue
            if not any(
                fragment in relative for fragment in rule["allowed_path_fragments"]
            ):
                errors.append(
                    f"{relative}: violates source boundary '{rule['id']}'"
                )
        if any(fragment in relative for fragment in policy["domain_path_fragments"]):
            for token in policy["domain_forbidden_tokens"]:
                if token in content:
                    errors.append(
                        f"{relative}: domain/common source contains forbidden token '{token}'"
                    )
    return errors


def emit_sbom(
    registry: dict[str, Any], output: Path, source_revision: str = "unresolved"
) -> None:
    canonical = json.dumps(registry, sort_keys=True, separators=(",", ":"))
    serial = uuid.uuid5(uuid.NAMESPACE_URL, canonical)
    components = []
    for entry in registry["entries"]:
        properties = [
            {"name": "toolly:scope", "value": entry["scope"]},
            {"name": "toolly:approval-status", "value": entry["approval_status"]},
            {
                "name": "toolly:transitive-completeness",
                "value": "incomplete-governance-preview",
            },
        ]
        component: dict[str, Any] = {
            "type": "library",
            "bom-ref": f"{entry['ecosystem']}:{entry['coordinate']}@{entry['immutable_ref']}",
            "group": entry["ecosystem"],
            "name": entry["coordinate"],
            "version": entry["requested_version"],
            "licenses": [{"expression": entry["licence_spdx"]}],
            "externalReferences": [
                {"type": "vcs", "url": entry["upstream"]},
                {"type": "license", "url": entry["licence_evidence"]},
            ],
            "properties": properties,
        }
        if SHA256_REF.fullmatch(entry["immutable_ref"]):
            component["hashes"] = [
                {
                    "alg": "SHA-256",
                    "content": entry["immutable_ref"].removeprefix("sha256:"),
                }
            ]
        components.append(component)
    sbom = {
        "bomFormat": "CycloneDX",
        "specVersion": "1.6",
        "serialNumber": f"urn:uuid:{serial}",
        "version": 1,
        "metadata": {
            "component": {
                "type": "application",
                "name": "toolly-mobile-governance-preview",
                "version": source_revision,
            },
            "properties": [
                {
                    "name": "toolly:sbom-authority",
                    "value": "direct-and-ci-register-only-not-release-sbom",
                }
            ],
        },
        "components": components,
    }
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(json.dumps(sbom, indent=2) + "\n", encoding="utf-8")


def self_test(
    indexed: dict[tuple[str, str], dict[str, Any]], policy: dict[str, Any]
) -> list[str]:
    failures: list[str] = []
    checkout = indexed.get(registry_key("github-action", "actions/checkout"))
    if not checkout:
        return ["self-test: checkout registry fixture missing"]
    valid_reference = f"actions/checkout@{checkout['immutable_ref']}"
    if workflow_reference_error(valid_reference, indexed, "self-test-valid"):
        failures.append("self-test: valid pinned Action was rejected")
    if not workflow_reference_error(
        "actions/checkout@v5", indexed, "self-test-mutable"
    ):
        failures.append("self-test: mutable Action tag was accepted")
    if not workflow_reference_error(
        "unknown/action@" + "0" * 40, indexed, "self-test-unregistered"
    ):
        failures.append("self-test: unregistered Action was accepted")

    invalid_catalog = {
        "versions": {"x": "1.0.0"},
        "libraries": {
            "unknown": {
                "module": "example:unknown",
                "version": {"ref": "x"},
            }
        },
    }
    if not validate_catalog(invalid_catalog, indexed, policy, "self-test-catalog"):
        failures.append("self-test: unregistered catalog dependency was accepted")
    if not contains_dynamic_version("1.+", policy):
        failures.append("self-test: dynamic version was accepted")

    with tempfile.TemporaryDirectory() as temporary:
        root = Path(temporary)
        invalid_domain = root / "domain-model/src/commonMain/kotlin/Invalid.kt"
        invalid_domain.parent.mkdir(parents=True)
        invalid_domain.write_text(
            "package invalid\nimport com.google.firebase.Firebase\n",
            encoding="utf-8",
        )
        boundary_errors = validate_source_boundaries(root, policy)
        if not boundary_errors:
            failures.append("self-test: Firebase leakage into domain was accepted")

        invalid_domain.unlink()
        valid_adapter = root / "firebase-adapter/src/main/kotlin/Valid.kt"
        valid_adapter.parent.mkdir(parents=True)
        valid_adapter.write_text(
            "package adapter\nimport com.google.firebase.Firebase\n",
            encoding="utf-8",
        )
        if validate_source_boundaries(root, policy):
            failures.append("self-test: allowed Firebase adapter import was rejected")

        invalid_aws = root / "firebase-adapter/src/main/kotlin/InvalidAws.kt"
        invalid_aws.write_text(
            "package adapter\nimport software.amazon.awssdk.services.s3.S3Client\n",
            encoding="utf-8",
        )
        if not validate_source_boundaries(root, policy):
            failures.append("self-test: current-phase AWS import was accepted")
    return failures


def validate(root: Path, run_self_test: bool, emit_path: Path | None) -> list[str]:
    policy_path = root / "config/dependencies/policy.json"
    registry_path = root / "config/dependencies/registry.json"
    exceptions_path = root / "config/dependencies/exceptions.json"
    catalog_path = root / "gradle/libs.versions.toml"
    errors: list[str] = []
    try:
        policy = load_json(policy_path)
        registry = load_json(registry_path)
        exceptions = load_json(exceptions_path)
        with catalog_path.open("rb") as handle:
            catalog = tomllib.load(handle)
    except (OSError, json.JSONDecodeError, tomllib.TOMLDecodeError) as error:
        return [f"cannot load dependency contract: {error}"]

    today = dt.datetime.now(dt.timezone.utc).date()
    errors.extend(validate_policy(policy, str(policy_path.relative_to(root))))
    indexed, registry_errors = validate_registry(
        registry, policy, str(registry_path.relative_to(root)), today
    )
    errors.extend(registry_errors)
    errors.extend(
        validate_exceptions(
            exceptions, str(exceptions_path.relative_to(root)), today
        )
    )
    errors.extend(
        validate_catalog(catalog, indexed, policy, str(catalog_path.relative_to(root)))
    )
    errors.extend(validate_workflows(root, indexed))
    errors.extend(validate_build_manifests(root, indexed, policy))
    errors.extend(validate_source_boundaries(root, policy))
    if run_self_test and not registry_errors:
        errors.extend(self_test(indexed, policy))
    if emit_path and not errors:
        emit_sbom(registry, emit_path)
    return errors


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=DEFAULT_ROOT)
    parser.add_argument("--self-test", action="store_true")
    parser.add_argument("--emit-sbom", type=Path)
    args = parser.parse_args()

    errors = validate(args.root.resolve(), args.self_test, args.emit_sbom)
    if errors:
        for error in errors:
            print(f"ERROR: {error}", file=sys.stderr)
        print(
            f"Dependency policy validation failed with {len(errors)} error(s).",
            file=sys.stderr,
        )
        return 1

    registry = load_json(args.root / "config/dependencies/registry.json")
    print(
        "Dependency policy valid; "
        f"{len(registry['entries'])} approved baseline dependency entries checked."
    )
    if args.emit_sbom:
        print(
            f"Governance SBOM preview written to {args.emit_sbom}; "
            "it is not a resolved release SBOM."
        )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
