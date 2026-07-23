# Toolly — Document Scanner

Privacy-first, offline-first document scanning for Android and iOS.

**Store name:** Toolly: Document Scanner  
**Android application ID:** `com.toollyscan.app`  
**iOS bundle ID:** `com.toollyscan.app`  
**Canonical domain:** [toollyscan.com](https://toollyscan.com) · [toollyscan.in](https://toollyscan.in)  
**Initial market:** India  
**Launch languages:** English · Hindi · Kannada

---

## Architecture principles

1. The encrypted local vault is the source of truth.
2. Capture, processing, organisation and local export must work offline.
3. Cloud backup is optional, explicit, encrypted and resumable.
4. Firebase is the approved cloud platform for initial development and production releases. Firebase SDK code is confined to infrastructure adapters and never enters the domain.
5. AWS is not implemented in the current phase. Migration may be evaluated after approximately two years based on cost, scale, reliability and business needs; this is a planning assumption, not a deadline.
6. Canonical IDs, schemas, encryption envelopes, sync contracts and object keys belong to Toolly.
7. Firebase UID must not become the canonical document-owner ID.
8. Provider SDK types must not enter shared domain models.
9. Document content and personal information must never enter logs or analytics.
10. Production feature implementation is blocked until the production-readiness gate is approved.

---

## Documentation

| Area | Link |
|---|---|
| Architecture overview | [docs/architecture/README.md](docs/architecture/README.md) |
| Canonical domain model | [docs/architecture/CANONICAL_DOMAIN_MODEL.md](docs/architecture/CANONICAL_DOMAIN_MODEL.md) |
| Module boundaries | [docs/architecture/MODULE_BOUNDARIES.md](docs/architecture/MODULE_BOUNDARIES.md) |
| Vault and processing contracts | [docs/architecture/VAULT_AND_PROCESSING_CONTRACTS.md](docs/architecture/VAULT_AND_PROCESSING_CONTRACTS.md) |
| Sync and Firebase contracts | [docs/architecture/SYNC_AND_FIREBASE_CONTRACTS.md](docs/architecture/SYNC_AND_FIREBASE_CONTRACTS.md) |
| Schema evolution | [docs/architecture/SCHEMA_EVOLUTION.md](docs/architecture/SCHEMA_EVOLUTION.md) |
| Architecture fitness functions | [docs/architecture/ARCHITECTURE_FITNESS_FUNCTIONS.md](docs/architecture/ARCHITECTURE_FITNESS_FUNCTIONS.md) |
| Dependency policy and review | [docs/architecture/DEPENDENCY_POLICY.md](docs/architecture/DEPENDENCY_POLICY.md) · [docs/architecture/DEPENDENCY_REVIEW_TEMPLATE.md](docs/architecture/DEPENDENCY_REVIEW_TEMPLATE.md) |
| Supply-chain security | [docs/security/SUPPLY_CHAIN_SECURITY.md](docs/security/SUPPLY_CHAIN_SECURITY.md) |
| SBOM, provenance and vulnerability response | [docs/operations/SBOM_AND_PROVENANCE.md](docs/operations/SBOM_AND_PROVENANCE.md) · [docs/operations/DEPENDENCY_VULNERABILITY_RESPONSE.md](docs/operations/DEPENDENCY_VULNERABILITY_RESPONSE.md) |
| ADR — KMP boundary | [docs/adr/0001-kotlin-multiplatform-boundary.md](docs/adr/0001-kotlin-multiplatform-boundary.md) |
| ADR — Local vault source of truth | [docs/adr/0002-local-vault-source-of-truth.md](docs/adr/0002-local-vault-source-of-truth.md) |
| ADR — Cloud provider portability | [docs/adr/0003-cloud-provider-portability.md](docs/adr/0003-cloud-provider-portability.md) |
| ADR — Authentication and account boundary | [docs/adr/0004-authentication-and-account-boundary.md](docs/adr/0004-authentication-and-account-boundary.md) |
| ADR — Canonical data and operation model | [docs/adr/0005-canonical-data-and-operation-model.md](docs/adr/0005-canonical-data-and-operation-model.md) |
| ADR — Local outbox and conflict policy | [docs/adr/0006-local-outbox-and-conflict-policy.md](docs/adr/0006-local-outbox-and-conflict-policy.md) |
| ADR — Encryption envelope and key hierarchy | [docs/adr/0007-encryption-envelope-and-key-hierarchy.md](docs/adr/0007-encryption-envelope-and-key-hierarchy.md) |
| ADR — Dependency and supply-chain governance | [docs/adr/0008-dependency-and-supply-chain-governance.md](docs/adr/0008-dependency-and-supply-chain-governance.md) |
| Decision register | [docs/product/DECISION_REGISTER.md](docs/product/DECISION_REGISTER.md) |
| Product scope | [docs/product/PRODUCT_SCOPE.md](docs/product/PRODUCT_SCOPE.md) |
| Entitlements | [docs/product/ENTITLEMENTS.md](docs/product/ENTITLEMENTS.md) |
| Monetization | [docs/product/MONETIZATION.md](docs/product/MONETIZATION.md) |
| Subscription lifecycle | [docs/product/SUBSCRIPTION_LIFECYCLE.md](docs/product/SUBSCRIPTION_LIFECYCLE.md) |
| Product metrics | [docs/product/PRODUCT_METRICS.md](docs/product/PRODUCT_METRICS.md) |
| India launch scope | [docs/product/INDIA_LAUNCH_SCOPE.md](docs/product/INDIA_LAUNCH_SCOPE.md) |
| Design audit | [docs/product/DESIGN_AUDIT.md](docs/product/DESIGN_AUDIT.md) |
| Figma information architecture | [docs/design/FIGMA_INFORMATION_ARCHITECTURE.md](docs/design/FIGMA_INFORMATION_ARCHITECTURE.md) |
| Screen inventory | [docs/design/SCREEN_INVENTORY.md](docs/design/SCREEN_INVENTORY.md) |
| User flow matrix | [docs/design/USER_FLOW_MATRIX.md](docs/design/USER_FLOW_MATRIX.md) |
| Component inventory | [docs/design/COMPONENT_INVENTORY.md](docs/design/COMPONENT_INVENTORY.md) |
| Component state matrix | [docs/design/COMPONENT_STATE_MATRIX.md](docs/design/COMPONENT_STATE_MATRIX.md) |
| Responsive layouts | [docs/design/RESPONSIVE_LAYOUTS.md](docs/design/RESPONSIVE_LAYOUTS.md) |
| Accessibility requirements | [docs/design/ACCESSIBILITY_REQUIREMENTS.md](docs/design/ACCESSIBILITY_REQUIREMENTS.md) |
| Localization requirements | [docs/design/LOCALIZATION_REQUIREMENTS.md](docs/design/LOCALIZATION_REQUIREMENTS.md) |
| Content guidelines | [docs/design/CONTENT_GUIDELINES.md](docs/design/CONTENT_GUIDELINES.md) |
| Design tokens | [docs/design/DESIGN_TOKENS.md](docs/design/DESIGN_TOKENS.md) |
| Developer handoff | [docs/design/DEVELOPER_HANDOFF.md](docs/design/DEVELOPER_HANDOFF.md) |
| Figma completion gate | [docs/design/FIGMA_COMPLETION_GATE.md](docs/design/FIGMA_COMPLETION_GATE.md) |
| Figma audit report | [docs/design/FIGMA_AUDIT_REPORT.md](docs/design/FIGMA_AUDIT_REPORT.md) |
| Security baseline | [docs/security/SECURITY_BASELINE.md](docs/security/SECURITY_BASELINE.md) |
| Threat model | [docs/security/THREAT_MODEL.md](docs/security/THREAT_MODEL.md) |
| Data inventory and lifecycle | [docs/security/DATA_INVENTORY.md](docs/security/DATA_INVENTORY.md) · [docs/security/DATA_LIFECYCLE.md](docs/security/DATA_LIFECYCLE.md) |
| Authentication and recovery | [docs/security/AUTHENTICATION_ABUSE_CONTROLS.md](docs/security/AUTHENTICATION_ABUSE_CONTROLS.md) · [docs/security/TRUSTED_DEVICE_AND_RECOVERY.md](docs/security/TRUSTED_DEVICE_AND_RECOVERY.md) |
| Telemetry and Firebase processing | [docs/security/TELEMETRY_POLICY.md](docs/security/TELEMETRY_POLICY.md) · [docs/security/FIREBASE_PROCESSING_INVENTORY.md](docs/security/FIREBASE_PROCESSING_INVENTORY.md) |
| India privacy readiness | [docs/security/PRIVACY_READINESS.md](docs/security/PRIVACY_READINESS.md) |
| Security operations | [docs/operations/SECURITY_INCIDENT_RESPONSE.md](docs/operations/SECURITY_INCIDENT_RESPONSE.md) · [docs/operations/VULNERABILITY_DISCLOSURE.md](docs/operations/VULNERABILITY_DISCLOSURE.md) |
| Roadmap | [docs/execution/ROADMAP.md](docs/execution/ROADMAP.md) |
| Production gate | [docs/execution/PRODUCTION_GATE.md](docs/execution/PRODUCTION_GATE.md) |
| Foundation audit and pending work | [docs/execution/FOUNDATION_AUDIT_2026-07-23.md](docs/execution/FOUNDATION_AUDIT_2026-07-23.md) |
| GitHub setup | [docs/execution/GITHUB_SETUP.md](docs/execution/GITHUB_SETUP.md) |
| Definition of done | [docs/quality/DEFINITION_OF_DONE.md](docs/quality/DEFINITION_OF_DONE.md) |
| Benchmark plan | [docs/quality/BENCHMARK_PLAN.md](docs/quality/BENCHMARK_PLAN.md) |
| Benchmark governance and metrics | [docs/quality/BENCHMARK_GOVERNANCE.md](docs/quality/BENCHMARK_GOVERNANCE.md) · [docs/quality/BENCHMARK_METRICS.md](docs/quality/BENCHMARK_METRICS.md) |
| Benchmark corpus, devices and evidence | [docs/quality/CORPUS_POLICY.md](docs/quality/CORPUS_POLICY.md) · [docs/quality/DEVICE_MATRIX.md](docs/quality/DEVICE_MATRIX.md) · [docs/quality/BENCHMARK_EVIDENCE.md](docs/quality/BENCHMARK_EVIDENCE.md) |
| Cloud provider migration feasibility guide | [docs/operations/FIREBASE_TO_AWS_RUNBOOK.md](docs/operations/FIREBASE_TO_AWS_RUNBOOK.md) |
| Cost controls | [docs/operations/COST_CONTROLS.md](docs/operations/COST_CONTROLS.md) |

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) and [SECURITY.md](SECURITY.md).

## Licence

Copyright © 2026 Toolly. All rights reserved.
