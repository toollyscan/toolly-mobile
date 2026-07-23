# Security Baseline

This index defines Toolly's minimum security posture and links the detailed control specifications. A documented control is not implementation evidence. Every release gate remains open until its named tests, reviews and operational evidence exist.

## Control status

| Area | Design status | Implementation status | Release authority |
|------|---------------|-----------------------|-------------------|
| Threat model and data inventory | Defined | Evidence pending | Product owner and security reviewer |
| Encryption envelope and key hierarchy | Proposed in ADR-0007 | Spike and evidence pending | Qualified cryptography reviewer |
| Authentication abuse controls | Defined | Staging evidence pending | Security reviewer |
| Trusted device and recovery | Defined at protocol level | Usability and security evidence pending | Product owner and security reviewer |
| Retention, deletion and export | Contract defined | End-to-end evidence pending | Privacy owner and legal counsel |
| Telemetry allowlist | Policy defined | Generated-schema tests pending | Security and privacy owners |
| Firebase processing | Initial inventory defined | Console and contract evidence pending | Privacy owner and legal counsel |
| Firebase environment, cost and operational policy | Defined in ADR-0009 | Project, load and configuration evidence pending | Product, security and operations owners |
| Incident and vulnerability operations | Runbooks defined | Contacts and exercise pending | Incident commander |

## Non-negotiable objectives

- Confidential document content and cryptographic material never enter logs, analytics, crash reports, support tooling or notification payloads.
- Local document access does not depend on a working cloud provider for a returning authenticated user.
- Identity-provider credentials do not become document ownership or encryption identities.
- Cloud backup is opt-in and cannot begin before clear notice, consent and processing configuration are recorded.
- Recovery does not give support personnel or the cloud provider a plaintext-document bypass.
- Security and privacy controls are baseline protections and are never restricted to a paid tier.
- Every sensitive data field, event, processor and retention rule is deny-by-default until registered.

## Detailed specifications

| Topic | Specification |
|-------|---------------|
| Threats, assets and trust boundaries | [THREAT_MODEL.md](THREAT_MODEL.md) |
| Product and provider data inventory | [DATA_INVENTORY.md](DATA_INVENTORY.md) |
| Proposed encryption envelope | [ADR-0007](../adr/0007-encryption-envelope-and-key-hierarchy.md) |
| Trusted devices and recovery | [TRUSTED_DEVICE_AND_RECOVERY.md](TRUSTED_DEVICE_AND_RECOVERY.md) |
| Authentication abuse controls | [AUTHENTICATION_ABUSE_CONTROLS.md](AUTHENTICATION_ABUSE_CONTROLS.md) |
| Retention, deletion and export | [DATA_LIFECYCLE.md](DATA_LIFECYCLE.md) |
| Telemetry allowlist and tests | [TELEMETRY_POLICY.md](TELEMETRY_POLICY.md) |
| Firebase services and processing | [FIREBASE_PROCESSING_INVENTORY.md](FIREBASE_PROCESSING_INVENTORY.md) |
| Firebase abuse and signed policy | [FIREBASE_ABUSE_AND_POLICY_CONTROLS.md](FIREBASE_ABUSE_AND_POLICY_CONTROLS.md) |
| India privacy readiness | [PRIVACY_READINESS.md](PRIVACY_READINESS.md) |
| Software supply-chain security | [SUPPLY_CHAIN_SECURITY.md](SUPPLY_CHAIN_SECURITY.md) |
| Security incident response | [SECURITY_INCIDENT_RESPONSE.md](../operations/SECURITY_INCIDENT_RESPONSE.md) |
| Vulnerability disclosure | [VULNERABILITY_DISCLOSURE.md](../operations/VULNERABILITY_DISCLOSURE.md) |

## Required verification profile

Implementations must provide:

- Unit and property tests for envelope parsing, associated-data binding, nonce uniqueness and failure behavior.
- Integration tests for interrupted writes, restore, deletion, export, account linking and account recreation.
- Device tests for key invalidation, backup restore, lost device, biometric changes and secure-storage behavior.
- Staging abuse tests for every authentication provider and account-linking path.
- Generated telemetry-schema tests that reject unknown events and prohibited properties.
- Firebase console exports or screenshots recording enabled services, regions, retention, access and deletion settings.
- `validate_firebase_governance.py --self-test` plus environment, App Check, signed-policy, budget, anomaly and workload evidence.
- Dependency, secret, static-analysis and mobile security checks appropriate to the implemented platform.
- A qualified cryptography review before ADR-0007 can become accepted.
- Qualified legal review before launch claims, notices, retention periods or regulatory conclusions become approved.
- An incident tabletop and verified private vulnerability-reporting channel before beta.

OWASP MASVS is the verification taxonomy; referencing it does not claim certification or compliance.

## Firebase and India processing

Firebase is the approved initial provider, but processing location and retention are service-specific. Firebase Authentication processing cannot be represented as an India-region deployment. Every Firebase service must therefore have a separate purpose, data category, location/transfer assessment, retention rule, access owner and deletion test.

The Digital Personal Data Protection Act, 2023 and the Digital Personal Data Protection Rules, 2025 have phased commencement. Product requirements and public notices must reflect the provisions effective at launch and be approved by qualified Indian counsel. This repository is an engineering baseline, not legal advice.

## Release blockers

- [ ] ADR-0007 is approved after implementation evidence and qualified cryptography review.
- [ ] Local vault, backup, recovery and key lifecycle tests pass on the approved device matrix.
- [ ] Authentication abuse and account-linking controls pass staging tests for every provider.
- [ ] Telemetry generated allowlist and prohibited-data tests pass.
- [ ] Firebase service inventory matches production console configuration.
- [ ] Four isolated environment bindings, signed operational policy and cost/load evidence satisfy ADR-0009.
- [ ] Service-specific processing locations, transfers and retention are legally reviewed.
- [ ] Deletion, consent withdrawal and export pass end-to-end tests.
- [ ] Security incident contacts, access and evidence storage are verified in an exercise.
- [ ] Private vulnerability intake is monitored and response targets are approved.
- [ ] Privacy notice, consent, grievance and user-rights flows are approved by qualified counsel.
- [ ] Secret scanning and applicable dependency/security checks pass on the release commit.

## Review cadence

Review this baseline for every new sensitive field, authentication method, processor, SDK, telemetry event, cryptographic format or recovery path, and at least once per release. Update the threat model and decision register in the same pull request when assumptions change.
