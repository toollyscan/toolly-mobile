#!/usr/bin/env python3
"""Validate Toolly benchmark contracts and checked-in evidence without dependencies."""

from __future__ import annotations

import argparse
import json
import math
import re
import sys
import tempfile
from pathlib import Path
from typing import Any


ROOT = Path(__file__).resolve().parents[1]
METRICS_PATH = ROOT / "benchmarks/contracts/metrics.v1.json"
CORPUS_PATH = ROOT / "benchmarks/corpus/manifest.v1.json"
EVIDENCE_ROOT = ROOT / "benchmarks/evidence"

SHA40 = re.compile(r"^[0-9a-f]{40}$")
SHA256 = re.compile(r"^[0-9a-f]{64}$")
UTC = re.compile(r"^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:\.\d+)?Z$")
SAFE_ID = re.compile(r"^[a-z0-9][a-z0-9._-]*$")

RUN_REQUIRED = {
    "schema_version",
    "run_id",
    "benchmark_id",
    "implementation_id",
    "git_commit",
    "build_variant",
    "started_at",
    "finished_at",
    "status",
    "evidence_maturity",
    "operator_role",
    "environment",
    "protocol",
    "corpus",
    "artifacts",
    "deviations",
}
ENV_REQUIRED = {
    "kind",
    "platform",
    "os_version",
    "device_tier",
    "manufacturer",
    "model_alias",
    "architecture",
    "ram_mib",
    "free_storage_mib",
    "power_source",
    "battery_percent_start",
    "battery_percent_end",
    "thermal_state_start",
    "thermal_state_end",
}
PROTOCOL_REQUIRED = {
    "id",
    "version",
    "warmup_iterations",
    "measured_iterations",
    "timeout_ms",
    "cooldown_seconds",
}
MEASUREMENT_REQUIRED = {
    "schema_version",
    "run_id",
    "benchmark_id",
    "metric_id",
    "case_id",
    "cohort_id",
    "iteration",
    "value",
    "unit",
    "status",
}
PROHIBITED_KEYS = {
    "document_content",
    "ocr_text",
    "recognized_text",
    "filename",
    "file_path",
    "phone",
    "phone_number",
    "email",
    "token",
    "auth_token",
    "password",
    "otp",
    "key",
    "key_material",
    "secret",
    "hardware_serial",
    "advertising_id",
    "user_id",
}


def load_json(path: Path) -> Any:
    with path.open("r", encoding="utf-8") as handle:
        return json.load(handle)


def required_keys(value: Any, required: set[str], location: str) -> list[str]:
    if not isinstance(value, dict):
        return [f"{location}: expected an object"]
    missing = sorted(required - set(value))
    return [f"{location}: missing key '{key}'" for key in missing]


def find_prohibited_keys(value: Any, location: str = "$") -> list[str]:
    errors: list[str] = []
    if isinstance(value, dict):
        for key, child in value.items():
            if key.lower() in PROHIBITED_KEYS:
                errors.append(f"{location}: prohibited key '{key}'")
            errors.extend(find_prohibited_keys(child, f"{location}.{key}"))
    elif isinstance(value, list):
        for index, child in enumerate(value):
            errors.extend(find_prohibited_keys(child, f"{location}[{index}]"))
    return errors


def validate_metrics(registry: Any) -> tuple[dict[str, str], list[str]]:
    errors = required_keys(
        registry,
        {"schema_version", "registry_id", "registry_version", "metrics"},
        str(METRICS_PATH),
    )
    metrics: dict[str, str] = {}
    if errors:
        return metrics, errors
    if registry["schema_version"] != 1:
        errors.append(f"{METRICS_PATH}: unsupported schema_version")
    if not isinstance(registry["metrics"], list) or not registry["metrics"]:
        errors.append(f"{METRICS_PATH}: metrics must be a non-empty array")
        return metrics, errors
    allowed_units = {"ms", "MiB", "bytes", "ratio"}
    allowed_direction = {"lower", "higher", "contextual"}
    for index, metric in enumerate(registry["metrics"]):
        location = f"{METRICS_PATH}:metrics[{index}]"
        errors.extend(required_keys(metric, {"id", "unit", "better"}, location))
        if not isinstance(metric, dict):
            continue
        metric_id = metric.get("id")
        unit = metric.get("unit")
        if not isinstance(metric_id, str) or not SAFE_ID.fullmatch(metric_id):
            errors.append(f"{location}: invalid metric id")
        elif metric_id in metrics:
            errors.append(f"{location}: duplicate metric id '{metric_id}'")
        elif isinstance(unit, str):
            metrics[metric_id] = unit
        if unit not in allowed_units:
            errors.append(f"{location}: unsupported unit '{unit}'")
        if metric.get("better") not in allowed_direction:
            errors.append(f"{location}: invalid direction")
    return metrics, errors


def validate_corpus(manifest: Any) -> list[str]:
    location = str(CORPUS_PATH)
    errors = required_keys(
        manifest,
        {
            "schema_version",
            "corpus_id",
            "corpus_version",
            "status",
            "data_policy",
            "languages",
            "required_cohorts",
            "items",
        },
        location,
    )
    if errors:
        return errors
    if manifest["status"] not in {"definition_only", "active", "retired"}:
        errors.append(f"{location}: invalid status")
    if sorted(manifest["languages"]) != ["en", "hi", "kn"]:
        errors.append(f"{location}: English, Hindi and Kannada are required")
    cohort_ids: set[str] = set()
    for index, cohort in enumerate(manifest["required_cohorts"]):
        item_location = f"{location}:required_cohorts[{index}]"
        errors.extend(
            required_keys(cohort, {"id", "minimum_items_per_language"}, item_location)
        )
        if not isinstance(cohort, dict):
            continue
        cohort_id = cohort.get("id")
        minimum = cohort.get("minimum_items_per_language")
        if not isinstance(cohort_id, str) or not SAFE_ID.fullmatch(cohort_id):
            errors.append(f"{item_location}: invalid cohort id")
        elif cohort_id in cohort_ids:
            errors.append(f"{item_location}: duplicate cohort id")
        else:
            cohort_ids.add(cohort_id)
        if not isinstance(minimum, int) or minimum <= 0:
            errors.append(f"{item_location}: minimum must be a positive integer")
    for index, item in enumerate(manifest["items"]):
        item_location = f"{location}:items[{index}]"
        errors.extend(
            required_keys(
                item,
                {
                    "id",
                    "source_type",
                    "languages",
                    "cohort_ids",
                    "content_sha256",
                    "contains_personal_data",
                    "privacy_status",
                    "licence",
                },
                item_location,
            )
        )
        if not isinstance(item, dict):
            continue
        if item.get("contains_personal_data") is not False:
            errors.append(f"{item_location}: personal data is prohibited")
        if item.get("privacy_status") != "approved":
            errors.append(f"{item_location}: item is not privacy-approved")
        if not SHA256.fullmatch(str(item.get("content_sha256", ""))):
            errors.append(f"{item_location}: invalid SHA-256 digest")
        unknown = set(item.get("cohort_ids", [])) - cohort_ids
        if unknown:
            errors.append(f"{item_location}: unknown cohorts {sorted(unknown)}")
    errors.extend(find_prohibited_keys(manifest))
    return errors


def validate_run(run: Any, location: str) -> list[str]:
    errors = required_keys(run, RUN_REQUIRED, location)
    if errors:
        return errors
    errors.extend(required_keys(run["environment"], ENV_REQUIRED, f"{location}:environment"))
    errors.extend(required_keys(run["protocol"], PROTOCOL_REQUIRED, f"{location}:protocol"))
    errors.extend(
        required_keys(
            run["corpus"],
            {"manifest_version", "cohort_ids"},
            f"{location}:corpus",
        )
    )
    for key in ("run_id", "benchmark_id", "implementation_id"):
        value = run.get(key)
        if not isinstance(value, str) or not SAFE_ID.fullmatch(value):
            errors.append(f"{location}: invalid {key}")
    if not SHA40.fullmatch(str(run.get("git_commit", ""))):
        errors.append(f"{location}: git_commit must be a full lowercase SHA")
    for key in ("started_at", "finished_at"):
        if not UTC.fullmatch(str(run.get(key, ""))):
            errors.append(f"{location}: {key} must be UTC ISO-8601")
    if run.get("status") not in {"complete", "aborted", "failed"}:
        errors.append(f"{location}: invalid status")
    if run.get("evidence_maturity") not in {"supplemental", "candidate"}:
        errors.append(f"{location}: invalid evidence_maturity")
    environment = run.get("environment", {})
    if isinstance(environment, dict):
        if environment.get("kind") not in {
            "physical_device",
            "emulator",
            "simulator",
            "host",
        }:
            errors.append(f"{location}: invalid environment kind")
        if environment.get("platform") not in {"android", "ios", "host"}:
            errors.append(f"{location}: invalid platform")
        for key in ("ram_mib", "free_storage_mib"):
            if not isinstance(environment.get(key), int) or environment.get(key, 0) <= 0:
                errors.append(f"{location}: environment.{key} must be positive")
        for key in ("battery_percent_start", "battery_percent_end"):
            value = environment.get(key)
            if not isinstance(value, int) or not 0 <= value <= 100:
                errors.append(f"{location}: environment.{key} must be 0..100")
    protocol = run.get("protocol", {})
    if isinstance(protocol, dict):
        for key in (
            "warmup_iterations",
            "measured_iterations",
            "timeout_ms",
            "cooldown_seconds",
        ):
            value = protocol.get(key)
            if not isinstance(value, int) or value < 0:
                errors.append(f"{location}: protocol.{key} must be non-negative")
        if protocol.get("measured_iterations", 0) <= 0:
            errors.append(f"{location}: measured_iterations must be positive")
    if run.get("evidence_maturity") == "candidate":
        if environment.get("kind") != "physical_device":
            errors.append(f"{location}: candidate evidence requires a physical device")
        if run.get("status") != "complete":
            errors.append(f"{location}: candidate evidence must be complete")
    if not isinstance(run.get("artifacts"), list) or not run["artifacts"]:
        errors.append(f"{location}: at least one artifact is required")
    else:
        for index, artifact in enumerate(run["artifacts"]):
            artifact_location = f"{location}:artifacts[{index}]"
            errors.extend(
                required_keys(
                    artifact,
                    {"path", "sha256", "media_type", "contains_personal_data"},
                    artifact_location,
                )
            )
            if not isinstance(artifact, dict):
                continue
            if not SHA256.fullmatch(str(artifact.get("sha256", ""))):
                errors.append(f"{artifact_location}: invalid SHA-256 digest")
            if artifact.get("contains_personal_data") is not False:
                errors.append(f"{artifact_location}: personal data is prohibited")
    errors.extend(find_prohibited_keys(run))
    return errors


def validate_measurement(
    measurement: Any,
    metrics: dict[str, str],
    run: dict[str, Any],
    location: str,
) -> list[str]:
    errors = required_keys(measurement, MEASUREMENT_REQUIRED, location)
    if errors:
        return errors
    if measurement.get("run_id") != run.get("run_id"):
        errors.append(f"{location}: run_id does not match manifest")
    if measurement.get("benchmark_id") != run.get("benchmark_id"):
        errors.append(f"{location}: benchmark_id does not match manifest")
    metric_id = measurement.get("metric_id")
    if metric_id not in metrics:
        errors.append(f"{location}: unknown metric_id '{metric_id}'")
    elif measurement.get("unit") != metrics[metric_id]:
        errors.append(f"{location}: unit does not match metric registry")
    if not isinstance(measurement.get("iteration"), int) or measurement["iteration"] < 0:
        errors.append(f"{location}: iteration must be a non-negative integer")
    if measurement.get("status") not in {
        "success",
        "failed",
        "timeout",
        "excluded",
    }:
        errors.append(f"{location}: invalid status")
    value = measurement.get("value")
    if measurement.get("status") == "success":
        if not isinstance(value, (int, float)) or isinstance(value, bool):
            errors.append(f"{location}: successful sample requires numeric value")
        elif not math.isfinite(value):
            errors.append(f"{location}: value must be finite")
        elif measurement.get("unit") == "ratio" and not 0 <= value <= 1:
            errors.append(f"{location}: ratio must be 0..1")
        elif measurement.get("unit") in {"ms", "MiB", "bytes"} and value < 0:
            errors.append(f"{location}: measurement cannot be negative")
    errors.extend(find_prohibited_keys(measurement))
    return errors


def validate_evidence(metrics: dict[str, str], evidence_root: Path) -> list[str]:
    errors: list[str] = []
    run_paths = sorted(evidence_root.glob("*/*/run.json"))
    for run_path in run_paths:
        location = str(run_path)
        try:
            run = load_json(run_path)
        except (OSError, json.JSONDecodeError) as error:
            errors.append(f"{location}: cannot read JSON: {error}")
            continue
        errors.extend(validate_run(run, location))
        measurement_path = run_path.with_name("measurements.jsonl")
        if not measurement_path.exists():
            errors.append(f"{location}: missing measurements.jsonl")
            continue
        samples = 0
        with measurement_path.open("r", encoding="utf-8") as handle:
            for line_number, line in enumerate(handle, start=1):
                if not line.strip():
                    continue
                samples += 1
                sample_location = f"{measurement_path}:{line_number}"
                try:
                    measurement = json.loads(line)
                except json.JSONDecodeError as error:
                    errors.append(f"{sample_location}: invalid JSON: {error}")
                    continue
                errors.extend(
                    validate_measurement(measurement, metrics, run, sample_location)
                )
        if run.get("evidence_maturity") == "candidate" and samples == 0:
            errors.append(f"{measurement_path}: candidate evidence has no samples")
    return errors


def self_test(metrics: dict[str, str]) -> list[str]:
    valid_run = {
        "schema_version": 1,
        "run_id": "self-test-01",
        "benchmark_id": "camera-boundary",
        "implementation_id": "self-test",
        "git_commit": "0" * 40,
        "build_variant": "test",
        "started_at": "2026-07-23T00:00:00Z",
        "finished_at": "2026-07-23T00:01:00Z",
        "status": "complete",
        "evidence_maturity": "supplemental",
        "operator_role": "validator",
        "environment": {
            "kind": "host",
            "platform": "host",
            "os_version": "self-test",
            "device_tier": "supplemental",
            "manufacturer": "self-test",
            "model_alias": "self-test",
            "architecture": "self-test",
            "ram_mib": 1024,
            "free_storage_mib": 1024,
            "power_source": "external",
            "battery_percent_start": 100,
            "battery_percent_end": 100,
            "thermal_state_start": "unknown",
            "thermal_state_end": "unknown",
        },
        "protocol": {
            "id": "self-test",
            "version": "1.0.0",
            "warmup_iterations": 0,
            "measured_iterations": 1,
            "timeout_ms": 1000,
            "cooldown_seconds": 0,
        },
        "corpus": {
            "manifest_version": "1.0.0-definition",
            "cohort_ids": ["printed-a4-clean"],
        },
        "artifacts": [
            {
                "path": "measurements.jsonl",
                "sha256": "0" * 64,
                "media_type": "application/x-ndjson",
                "contains_personal_data": False,
            }
        ],
        "deviations": [],
    }
    valid_measurement = {
        "schema_version": 1,
        "run_id": "self-test-01",
        "benchmark_id": "camera-boundary",
        "metric_id": "camera.first_frame",
        "case_id": "synthetic-01",
        "cohort_id": "printed-a4-clean",
        "iteration": 0,
        "value": 10.0,
        "unit": "ms",
        "status": "success",
    }
    failures: list[str] = []
    if validate_run(valid_run, "self-test-valid-run"):
        failures.append("validator rejected valid run")
    if validate_measurement(
        valid_measurement, metrics, valid_run, "self-test-valid-measurement"
    ):
        failures.append("validator rejected valid measurement")
    invalid_candidate = json.loads(json.dumps(valid_run))
    invalid_candidate["evidence_maturity"] = "candidate"
    if not validate_run(invalid_candidate, "self-test-invalid-candidate"):
        failures.append("validator accepted non-physical candidate evidence")
    invalid_measurement = dict(valid_measurement)
    invalid_measurement["unit"] = "seconds"
    invalid_measurement["ocr_text"] = "prohibited"
    invalid_errors = validate_measurement(
        invalid_measurement, metrics, valid_run, "self-test-invalid-measurement"
    )
    if len(invalid_errors) < 2:
        failures.append("validator accepted invalid unit or prohibited data")
    with tempfile.TemporaryDirectory() as temp:
        root = Path(temp)
        if validate_evidence(metrics, root):
            failures.append("validator rejected an empty evidence directory")
    return failures


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--self-test",
        action="store_true",
        help="also execute validator acceptance/rejection tests",
    )
    args = parser.parse_args()

    errors: list[str] = []
    try:
        registry = load_json(METRICS_PATH)
        corpus = load_json(CORPUS_PATH)
    except (OSError, json.JSONDecodeError) as error:
        print(f"ERROR: cannot load benchmark contract: {error}", file=sys.stderr)
        return 1

    metrics, metric_errors = validate_metrics(registry)
    errors.extend(metric_errors)
    errors.extend(validate_corpus(corpus))
    if args.self_test and not metric_errors:
        errors.extend(self_test(metrics))
    if not metric_errors:
        errors.extend(validate_evidence(metrics, EVIDENCE_ROOT))

    if errors:
        for error in errors:
            print(f"ERROR: {error}", file=sys.stderr)
        print(f"Benchmark validation failed with {len(errors)} error(s).", file=sys.stderr)
        return 1

    run_count = len(list(EVIDENCE_ROOT.glob("*/*/run.json")))
    print(
        "Benchmark contracts valid; "
        f"{len(metrics)} metrics registered; {run_count} evidence run(s) checked."
    )
    if run_count == 0:
        print("No benchmark claim exists yet; Production Gate evidence remains pending.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
