#!/usr/bin/env python3
"""Validate Toolly's Firebase-first environment, service, cost and policy contracts."""

from __future__ import annotations

import argparse
import json
import sys
import tempfile
from pathlib import Path
from typing import Any


ROOT = Path(__file__).resolve().parents[1]
CONFIG = ROOT / "config" / "firebase"
EXPECTED_ENVIRONMENTS = {"development", "test", "staging", "production"}
EXPECTED_SERVICES = {
    "authentication",
    "firestore",
    "storage",
    "functions",
    "fcm",
    "remote-config",
    "app-check",
    "crashlytics",
    "performance",
    "analytics",
}
REQUIRED_LOCAL_CAPABILITIES = {"scan", "vaultRead", "vaultWrite", "localExport"}
ALLOWED_CLOUD_MODES = {"normal", "contain-cost", "contain-incident"}


class ValidationError(RuntimeError):
    """Raised when a governance invariant is violated."""


def load_json(path: Path) -> dict[str, Any]:
    try:
        value = json.loads(path.read_text(encoding="utf-8"))
    except FileNotFoundError as exc:
        raise ValidationError(f"Missing required file: {path.relative_to(ROOT)}") from exc
    except json.JSONDecodeError as exc:
        raise ValidationError(
            f"Invalid JSON in {path.relative_to(ROOT)}: line {exc.lineno}: {exc.msg}"
        ) from exc
    if not isinstance(value, dict):
        raise ValidationError(f"{path.relative_to(ROOT)} must contain a JSON object")
    return value


def require(condition: bool, message: str) -> None:
    if not condition:
        raise ValidationError(message)


def validate_environments(data: dict[str, Any]) -> None:
    require(data.get("schemaVersion") == 1, "Environment schemaVersion must be 1")
    require(data.get("cloudProvider") == "firebase", "Firebase must be the current cloud provider")
    require(
        data.get("awsImplementationAllowed") is False,
        "AWS implementation must remain disabled in the current phase",
    )
    require(
        data.get("projectIdBindingStatus") == "unprovisioned",
        "TLY-008 is a design gate and must not claim provisioned Firebase projects",
    )

    environments = data.get("environments")
    require(isinstance(environments, list), "environments must be a list")
    names = {item.get("name") for item in environments if isinstance(item, dict)}
    require(names == EXPECTED_ENVIRONMENTS, "Exactly development/test/staging/production are required")

    aliases: set[str] = set()
    for environment in environments:
        require(isinstance(environment, dict), "Each environment must be an object")
        name = environment["name"]
        alias = environment.get("projectAlias")
        require(isinstance(alias, str) and alias, f"{name}: projectAlias is required")
        require(alias not in aliases, f"{name}: projectAlias must be unique")
        aliases.add(alias)
        require(environment.get("projectId") is None, f"{name}: projectId must remain unbound")
        require(
            environment.get("dataPolicy") != "production-data",
            f"{name}: use the explicit approved-user-data-only production policy",
        )
        if name != "production":
            require(
                environment.get("productionIdentityAllowed") is False,
                f"{name}: production identities are forbidden",
            )
        if name in {"staging", "production"}:
            require(
                environment.get("debugAppCheckTokensAllowed") is False,
                f"{name}: debug App Check tokens are forbidden",
            )
    production = next(item for item in environments if item["name"] == "production")
    require(
        production.get("dataPolicy") == "approved-user-data-only",
        "production must use the approved-user-data-only policy",
    )

    invariants = data.get("isolationInvariants", {})
    for key in (
        "separateFirebaseProjectPerEnvironment",
        "androidAndIosShareOnlySameEnvironmentBackend",
        "productionDeploymentRequiresNamedApproval",
    ):
        require(invariants.get(key) is True, f"Isolation invariant {key} must be true")
    for key in (
        "crossEnvironmentDataAccess",
        "productionDataInNonProduction",
        "productionCredentialsInNonProduction",
        "testPhoneNumbersInProduction",
    ):
        require(invariants.get(key) is False, f"Isolation invariant {key} must be false")
    require(
        invariants.get("unknownEnvironmentBehavior") == "fail-closed",
        "Unknown environment behavior must fail closed",
    )


def validate_service_boundaries(data: dict[str, Any]) -> None:
    require(data.get("schemaVersion") == 1, "Service-boundary schemaVersion must be 1")
    require(data.get("cloudProvider") == "firebase", "Service boundaries must be Firebase-first")
    require(data.get("awsImplementationAllowed") is False, "AWS service adapters are prohibited")
    require(data.get("defaultPolicy") == "disabled-and-deny", "Default service policy must deny")

    services = data.get("services")
    require(isinstance(services, list), "services must be a list")
    ids = {service.get("id") for service in services if isinstance(service, dict)}
    require(ids == EXPECTED_SERVICES, "Firebase service inventory is incomplete or has unknown entries")

    for service in services:
        service_id = service["id"]
        require(service.get("domainContract"), f"{service_id}: domainContract is required")
        require(isinstance(service.get("allowedData"), list), f"{service_id}: allowedData is required")
        require(
            isinstance(service.get("prohibitedData"), list) and service["prohibitedData"],
            f"{service_id}: prohibitedData must be non-empty",
        )
        enabled = service.get("enabled")
        require(isinstance(enabled, dict), f"{service_id}: enabled matrix is required")
        require(
            set(enabled) == EXPECTED_ENVIRONMENTS,
            f"{service_id}: enabled matrix must cover every environment",
        )

    analytics = next(item for item in services if item["id"] == "analytics")
    require(
        set(analytics["enabled"].values()) == {"disabled"},
        "Analytics must remain disabled in every environment",
    )
    for service_id in {"crashlytics", "performance"}:
        service = next(item for item in services if item["id"] == service_id)
        require(
            service.get("automaticCollection") == "disabled-until-telemetry-gate",
            f"{service_id}: automatic collection must remain disabled",
        )
    remote_config = next(item for item in services if item["id"] == "remote-config")
    require(
        remote_config.get("authorizationSource") == "never-remote-config",
        "Remote Config must never be an authorization source",
    )
    app_check = next(item for item in services if item["id"] == "app-check")
    require(
        app_check.get("authorizationSource") == "never-app-check-alone",
        "App Check must not be treated as authorization",
    )


def validate_cost_model(data: dict[str, Any]) -> None:
    require(data.get("schemaVersion") == 1, "Cost-model schemaVersion must be 1")
    require(data.get("currency") == "INR", "India-first cost model must report in INR")
    require(
        data.get("status") == "model-defined-prices-and-load-evidence-pending",
        "Cost model must not claim final pricing or load evidence",
    )
    snapshot = data.get("pricingSnapshot", {})
    require(snapshot.get("effectiveAt") is None, "No pricing effective date may be invented")
    require(
        str(snapshot.get("status", "")).startswith("pending-"),
        "Pricing snapshot must remain pending until region/SKU evidence exists",
    )

    scenarios = data.get("workloadScenarios")
    require(isinstance(scenarios, list) and len(scenarios) >= 3, "At least three scenarios are required")
    scenario_ids = {scenario.get("id") for scenario in scenarios}
    require(
        {"free-base", "premium-base", "premium-allowance-edge"}.issubset(scenario_ids),
        "Free, premium base and allowance-edge scenarios are required",
    )
    for scenario in scenarios:
        require(scenario.get("status") == "hypothesis", f"{scenario.get('id')}: must be a hypothesis")
        usage = scenario.get("monthlyPerActiveUser")
        require(isinstance(usage, dict) and usage, f"{scenario.get('id')}: usage assumptions required")
        for metric, value in usage.items():
            require(
                isinstance(value, (int, float)) and value >= 0,
                f"{scenario.get('id')}.{metric}: usage must be non-negative",
            )
    free = next(item for item in scenarios if item["id"] == "free-base")
    require(
        free["monthlyPerActiveUser"].get("storageGiBMonth") == 0,
        "Free-base cannot silently include premium cloud document backup",
    )

    budget = data.get("budgetPolicy", {})
    require(budget.get("budgetsAreHardCaps") is False, "Budgets are alerts, not hard caps")
    require(budget.get("automaticBillingDisable") is False, "Automatic billing disable is unsafe")
    require(
        100 in budget.get("actualSpendAlertPercent", []),
        "Actual-spend alerts must include 100 percent",
    )
    require(
        budget.get("programmaticNotifications") == "required-before-staging-billing",
        "Programmatic budget notifications are required before staging billing",
    )

    anomaly = data.get("anomalyPolicy", {})
    for environment_group in ("developmentAndTest", "staging", "production"):
        policy = anomaly.get(environment_group, {})
        require(
            isinstance(policy.get("minimumCostImpactInr"), (int, float))
            and policy["minimumCostImpactInr"] > 0,
            f"{environment_group}: positive cost-impact threshold required",
        )
        require(
            isinstance(policy.get("minimumDeviationPercent"), (int, float))
            and 0 < policy["minimumDeviationPercent"] <= 100,
            f"{environment_group}: deviation threshold must be 1..100",
        )
    evidence = data.get("loadEvidenceRequired", {})
    cohorts = evidence.get("activeUserCohorts", [])
    require(1_000_000 in cohorts, "Million-user load/cost cohort must be modelled")
    require(
        {"otp-abuse", "backup-resume-storm", "function-retry-storm"}.issubset(
            set(evidence.get("trafficProfiles", []))
        ),
        "Abuse and replay-amplification traffic profiles are required",
    )


def validate_operational_policy_schema(data: dict[str, Any]) -> None:
    require(
        data.get("$schema") == "https://json-schema.org/draft/2020-12/schema",
        "Operational policy must use JSON Schema 2020-12",
    )
    required = set(data.get("required", []))
    require(
        {"environment", "generation", "issuedAt", "expiresAt", "keyId", "payload", "signature"}.issubset(
            required
        ),
        "Signed envelope fields are incomplete",
    )
    properties = data.get("properties", {})
    modes = set(
        properties.get("payload", {})
        .get("properties", {})
        .get("cloudMode", {})
        .get("enum", [])
    )
    require(modes == ALLOWED_CLOUD_MODES, "Operational policy cloud modes are invalid")
    local_properties = (
        properties.get("payload", {})
        .get("properties", {})
        .get("localCapabilities", {})
        .get("properties", {})
    )
    require(
        set(local_properties) == REQUIRED_LOCAL_CAPABILITIES,
        "All protected local capabilities must be represented",
    )
    for capability in REQUIRED_LOCAL_CAPABILITIES:
        require(
            local_properties.get(capability, {}).get("const") is True,
            f"Operational policy must never disable local {capability}",
        )
    verification = data.get("x-toolly-verification", {})
    require(
        "not trusted" in verification.get("transport", ""),
        "Remote Config must be explicitly untrusted for authorization",
    )
    replay_controls = set(verification.get("replayControls", []))
    require(
        {
            "environment binding",
            "strictly increasing generation",
            "issued-at and expiry validation using bounded clock policy",
            "key-id rotation and revocation",
        }.issubset(replay_controls),
        "Operational policy replay controls are incomplete",
    )


def validate_all(config_root: Path = CONFIG) -> None:
    validate_environments(load_json(config_root / "environments.json"))
    validate_service_boundaries(load_json(config_root / "service-boundaries.json"))
    validate_cost_model(load_json(config_root / "cost-model.json"))
    validate_operational_policy_schema(load_json(config_root / "operational-policy.schema.json"))


def expect_failure(config_root: Path, mutation: Any, description: str) -> None:
    files = {
        path.name: load_json(path)
        for path in config_root.glob("*.json")
    }
    mutation(files)
    with tempfile.TemporaryDirectory(prefix="toolly-firebase-policy-") as temp_dir:
        target = Path(temp_dir)
        for name, value in files.items():
            (target / name).write_text(
                json.dumps(value, indent=2, sort_keys=True) + "\n",
                encoding="utf-8",
            )
        try:
            validate_all(target)
        except ValidationError:
            return
    raise ValidationError(f"Self-test did not reject: {description}")


def run_self_test() -> None:
    expect_failure(
        CONFIG,
        lambda files: files["environments.json"].update({"awsImplementationAllowed": True}),
        "AWS implementation enabled",
    )
    expect_failure(
        CONFIG,
        lambda files: files["environments.json"]["environments"].pop(),
        "missing production environment",
    )
    expect_failure(
        CONFIG,
        lambda files: files["service-boundaries.json"]["services"][-1]["enabled"].update(
            {"production": "enabled"}
        ),
        "Analytics enabled in production",
    )
    expect_failure(
        CONFIG,
        lambda files: files["cost-model.json"]["budgetPolicy"].update(
            {"budgetsAreHardCaps": True}
        ),
        "budget represented as a hard cap",
    )
    expect_failure(
        CONFIG,
        lambda files: files["operational-policy.schema.json"]["properties"]["payload"][
            "properties"
        ]["localCapabilities"]["properties"]["scan"].update({"const": False}),
        "remote policy disables local scanning",
    )
    expect_failure(
        CONFIG,
        lambda files: files["operational-policy.schema.json"]["required"].remove("signature"),
        "unsigned operational policy",
    )


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--self-test",
        action="store_true",
        help="Prove that representative unsafe mutations are rejected.",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    try:
        validate_all()
        if args.self_test:
            run_self_test()
    except ValidationError as exc:
        print(f"Firebase governance validation failed: {exc}", file=sys.stderr)
        return 1
    print("Firebase governance valid: 4 environments, 10 services, cost model and signed policy.")
    if args.self_test:
        print("Firebase governance self-test passed.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
