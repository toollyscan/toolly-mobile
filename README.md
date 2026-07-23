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
4. Firebase is the initial infrastructure provider, not a domain dependency.
5. Firebase and future AWS implementations must remain behind Toolly-owned contracts.
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
| ADR — KMP boundary | [docs/adr/0001-kotlin-multiplatform-boundary.md](docs/adr/0001-kotlin-multiplatform-boundary.md) |
| ADR — Local vault source of truth | [docs/adr/0002-local-vault-source-of-truth.md](docs/adr/0002-local-vault-source-of-truth.md) |
| ADR — Cloud provider portability | [docs/adr/0003-cloud-provider-portability.md](docs/adr/0003-cloud-provider-portability.md) |
| ADR — Authentication and account boundary | [docs/adr/0004-authentication-and-account-boundary.md](docs/adr/0004-authentication-and-account-boundary.md) |
| Decision register | [docs/product/DECISION_REGISTER.md](docs/product/DECISION_REGISTER.md) |
| Design audit | [docs/product/DESIGN_AUDIT.md](docs/product/DESIGN_AUDIT.md) |
| Security baseline | [docs/security/SECURITY_BASELINE.md](docs/security/SECURITY_BASELINE.md) |
| Roadmap | [docs/execution/ROADMAP.md](docs/execution/ROADMAP.md) |
| Production gate | [docs/execution/PRODUCTION_GATE.md](docs/execution/PRODUCTION_GATE.md) |
| GitHub setup | [docs/execution/GITHUB_SETUP.md](docs/execution/GITHUB_SETUP.md) |
| Definition of done | [docs/quality/DEFINITION_OF_DONE.md](docs/quality/DEFINITION_OF_DONE.md) |
| Benchmark plan | [docs/quality/BENCHMARK_PLAN.md](docs/quality/BENCHMARK_PLAN.md) |
| Firebase-to-AWS runbook | [docs/operations/FIREBASE_TO_AWS_RUNBOOK.md](docs/operations/FIREBASE_TO_AWS_RUNBOOK.md) |
| Cost controls | [docs/operations/COST_CONTROLS.md](docs/operations/COST_CONTROLS.md) |

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) and [SECURITY.md](SECURITY.md).

## Licence

Copyright © 2025 Toolly. All rights reserved.
