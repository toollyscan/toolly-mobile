# Product Roadmap

This roadmap describes Toolly's development phases. Dates are indicative and a completed design document does not imply implementation evidence.

## Phase 0 — Product and engineering foundation (current)

**Goal:** Establish governance, validated product scope, executable architecture and release controls.

- [x] Repository governance, CI, secret scanning and documentation standards
- [x] Product scope, monetization, entitlement and India launch baselines (TLY-002)
- [x] Design specification and Figma completion gates (TLY-003)
- [x] Canonical domain, module, vault, processing, sync, Firebase and schema contracts (TLY-004)
- [x] Threat model, data inventory, recovery, auth abuse, lifecycle, telemetry, Firebase processing and incident-response designs (TLY-005)
- [x] Proposed versioned encryption-envelope and key-hierarchy ADR (TLY-005)
- [ ] TLY-005 implementation, legal, cryptographic and operational evidence
- [ ] Figma completion gates G1–G10 approved through live Figma evidence
- [ ] Domain and trademark clearance
- [ ] GitHub branch protection and environment configuration

## Phase 1 — Architecture and security prototypes

**Goal:** Validate the KMP boundary, encrypted vault and recovery behavior before production implementation.

- [ ] KMP module structure and provider-neutral ports
- [ ] Encrypted local-vault spike against ADR-0007 candidate choices
- [ ] Envelope, nonce, associated-data, rotation and failure-property tests
- [ ] Platform key-protection matrix on representative Android and Apple devices
- [ ] Trusted-device and encrypted-backup recovery usability/security spike
- [ ] Camera boundary and rendering benchmark
- [ ] OCR engine evaluation on representative Indian documents
- [ ] Production Gate review for the first vertical slice

## Phase 2 — Core document capture

**Goal:** Deliver tested offline capture, encrypted storage and local export on Android, then iOS.

*Blocked until the relevant Production Gate is approved.*

- [ ] Camera capture without an unapproved commercial SDK
- [ ] Page crop and enhancement
- [ ] Encrypted vault persistence with migration and corruption tests
- [ ] Local PDF/JPEG export
- [ ] English, Hindi and Kannada localization
- [ ] Accessibility and representative-device performance evidence

## Phase 3 — Authentication and account

**Goal:** Add approved multi-provider authentication without coupling ownership to Firebase.

*Blocked until the relevant Production Gate is approved.*

- [ ] Canonical `ToollyAccountId` creation and provider mapping
- [ ] Firebase Authentication behind Toolly-owned ports
- [ ] Phone OTP, email/password, Google and Apple Sign In on iOS
- [ ] Account-linking and canonical-ID contract tests
- [ ] Abuse controls validated in staging for every provider
- [ ] Identity deletion, recreation, correction and offline-returning-user tests

## Phase 4 — Optional encrypted cloud backup

**Goal:** Add explicit opt-in backup and sync while the local vault remains authoritative.

*Blocked until authentication, privacy and cryptography gates are approved.*

- [ ] Provider-neutral sync engine and atomic local outbox
- [ ] Firebase adapters matching the approved processing inventory
- [ ] Resumable verified upload, download and restore
- [ ] Revision-ancestry reconciliation and conflict preservation
- [ ] Backup notice, opt-in, withdrawal, deletion and export
- [ ] Cross-device recovery and key-rotation evidence

## Phase 5 — Subscription and entitlements

**Goal:** Implement sustainable monetization without weakening baseline security or local ownership.

- [ ] Provider-neutral entitlement model and offline cache
- [ ] Google Play Billing and StoreKit adapters
- [ ] Idempotent backend entitlement verification
- [ ] Subscription lifecycle, paywall, management and purchase restore
- [ ] India pricing, tax and store-product approval
- [ ] Tests proving subscription expiry does not block or delete local documents
- [ ] Tests proving security, privacy, deletion and recovery controls are not paywalled

## Phase 6 — India beta and general availability

- [ ] All Production Gate evidence approved
- [ ] Live Figma, accessibility and localization gates approved
- [ ] Domain and trademark clearance complete
- [ ] Privacy policy, terms, grievance process and store disclosures published
- [ ] Firebase production inventory and service-specific processing assessment approved
- [ ] Security incident tabletop and vulnerability channel test complete
- [ ] Performance and reliability targets met on representative devices

## Future cloud-provider evaluation

Firebase is the approved provider now. AWS implementation is outside the current plan. A migration may be evaluated after approximately two years based on measured cost, scale, reliability and business needs; this is not a committed deadline. See [FIREBASE_TO_AWS_RUNBOOK.md](../operations/FIREBASE_TO_AWS_RUNBOOK.md).

## Release blockers

See [PRODUCTION_GATE.md](PRODUCTION_GATE.md) and [SECURITY_BASELINE.md](../security/SECURITY_BASELINE.md).
