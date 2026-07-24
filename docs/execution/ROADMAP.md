# Product Roadmap

This roadmap describes the phases of Toolly development. Dates are indicative.

---

## Phase 0 — Repository foundation (current)

**Goal:** Establish governance, documentation and architecture.

- [x] Repository governance files (README, CONTRIBUTING, SECURITY, CODEOWNERS)
- [x] Architecture overview and ADRs
- [x] Security baseline
- [x] Definition of done and benchmark plan
- [x] Product scope, entitlements, monetization and subscription lifecycle (TLY-002)
- [x] India launch scope and product metrics (TLY-002)
- [x] Design specification baseline: screen inventory, component inventory, tokens, accessibility, localization, Figma IA, user flow matrix, content guidelines, developer handoff, Figma completion gates (TLY-003)
- [x] Canonical domain, module, vault, processing, sync, Firebase and schema contracts (TLY-004)
- [x] Architecture fitness-function specification (TLY-004)
- [x] Security, privacy, Firebase processing, telemetry, recovery and incident-response design baseline (TLY-005)
- [x] Benchmark governance, corpus, device, metric and evidence contracts (TLY-006A)
- [x] Dependency, licence, SBOM, provenance and supply-chain governance (TLY-007)
- [x] Firebase environment, service-boundary, cost, abuse, signed-policy and IaC design (TLY-008)
- [x] CI: first-party-only execution — community actions replaced with Toolly-owned Python scripts (TLY-009)
- [x] CI: Markdown lint, secret scan, dependency-policy, CI trust-policy enforcement (TLY-009)
- [x] ADR-0010: first-party CI trust boundary (TLY-009)
- [x] Concise release/rollback checklist and release evidence template (TLY-009)
- [x] Minimal incident-response checklist (TLY-009)
- [x] Runtime dependency, adaptive device, future web and notification baseline (ADR-0011)
- [ ] TLY-005 implementation, legal, cryptographic and operational evidence
- [ ] Figma completion gates G1–G10 approved (TLY-003B — live Figma work; evidence pending)
- [ ] Domain and trademark clearance
- [ ] GitHub branch protection and environment configuration (see GITHUB_SETUP.md)

### TLY-009 deferred items (pre-beta milestone)

The following TLY-009 items are **not yet implemented** and must not be
claimed as completed.  They are deferred because they require operational
infrastructure, real traffic data or production builds that do not yet exist:

- Comprehensive SLO/SLI definitions with automated alerting and dashboards.
- Full disaster-recovery and backup-restore drill evidence.
- Store-release automation (Google Play and App Store submission pipelines).
- Extensive compliance evidence and auditor-facing reports.
- Advanced backup/restore drill scripts and scheduling.
- Enterprise-grade operational reporting and on-call rotation.
- Legal notification templates and breach-response workflows.
- Signing-key rotation and HSM integration.

These items will be addressed in the pre-beta milestone (Phase 1 → Phase 2
transition) when staging builds, production Firebase projects and real signing
material are available.

---

## Phase 1 — Architecture prototype

**Goal:** Validate the KMP boundary and encrypted vault before production implementation.

- [ ] KMP module structure (domain, data interfaces)
- [ ] Room, current SQLCipher Android, Keystore and encrypted-asset vault prototype against ADR-0007, including migration, nonce, rotation, key-invalidation and failure tests
- [ ] ML Kit scanner adapter spike covering first use, dynamic delivery, offline-after-install, cancellation and unsupported devices
- [ ] CameraX and manual-crop fallback boundary prototype
- [ ] Coil encrypted-vault fetcher proving no decrypted disk cache
- [ ] Adaptive Android phone/tablet shell covering compact, medium and expanded layouts
- [ ] Local and FCM notification prototype covering safe payloads, channels, consent and token lifecycle
- [ ] Compose Multiplatform rendering benchmark on representative devices (DA-001)
- [ ] OCR engine evaluation on Indian documents (DA-004)
- [ ] Production Gate review

---

## Phase 2 — Core document capture (Android)

**Goal:** End-to-end document capture, storage and export on Android.

*Blocked until Production Gate is approved.*

- [ ] ML Kit document capture behind the Toolly scanner port, with CameraX/manual fallback
- [ ] Page cropping and enhancement with versioned Toolly processing recipes
- [ ] Encrypted vault persistence
- [ ] Local PDF/JPEG export
- [ ] English, Hindi and Kannada UI strings
- [ ] Accessibility (TalkBack, minimum WCAG 2.1 AA)
- [ ] Adaptive phone/tablet layouts, landscape and multi-window behavior
- [ ] Security, processing, backup, billing and consented marketing notification channels
- [ ] Benchmark on representative Android phones and tablets

---

## Phase 3 — Core document capture (iOS)

**Goal:** Feature parity on iPhone and iPad.

*Blocked until Phase 2 is complete.*

- [ ] Camera capture (no commercial SDK)
- [ ] Page cropping and enhancement
- [ ] Encrypted vault persistence
- [ ] Local PDF/JPEG export
- [ ] English, Hindi and Kannada UI strings
- [ ] Accessibility (VoiceOver, minimum WCAG 2.1 AA)
- [ ] Benchmark on representative iPhone and iPad devices

---

## Phase 4 — Authentication and account

**Goal:** Approved multi-provider authentication with canonical account identity.

*Blocked until Production Gate is approved.*

- [ ] `ToollyAccountId` generation at registration
- [ ] Firebase Authentication integration behind the canonical authentication port
- [ ] Phone OTP, email/password, Google and Apple Sign In on iOS
- [ ] Provider-account linking and canonical-ID contract tests
- [ ] OTP abuse controls validated in staging
- [ ] Trusted-device, identity recovery and encrypted-backup recovery implementation evidence approved under TLY-005
- [ ] Provider identity processing, Toolly persistence, retention and deletion reviewed

---

## Phase 5 — Optional cloud backup

**Goal:** End-to-end encrypted cloud backup and sync.

*Blocked until Phase 4 is complete and service-specific processing locations, transfers, retention and deletion are approved.*

- [ ] Provider-neutral sync engine and atomic local outbox
- [ ] Firebase metadata/object adapters
- [ ] Resumable verified upload, download and restore
- [ ] Revision-ancestry reconciliation and conflict preservation
- [ ] User-controlled backup toggle
- [ ] Approved backup notice, consent, withdrawal, deletion and export flows

---

## Phase 5a — Subscription and entitlements

**Goal:** Implement premium subscription, entitlement service and in-app purchase flow.

*Blocked until Production Gate monetization gate is approved.*

- [ ] Entitlement domain model (provider-neutral; no billing SDK types in domain)
- [ ] Offline entitlement cache with freshness policy
- [ ] Google Play Billing integration (behind Toolly-owned entitlement contract)
- [ ] StoreKit integration (behind Toolly-owned entitlement contract)
- [ ] Backend entitlement verification service (idempotent)
- [ ] Subscription lifecycle state machine (all states in SUBSCRIPTION_LIFECYCLE.md)
- [ ] Free and premium entitlement enforcement per ENTITLEMENTS.md
- [ ] Subscription expiry behaviour: local documents unaffected — verified by test
- [ ] Paywall and subscription management UI
- [ ] Restore purchase flow
- [ ] India pricing validated and store products created

---

## Phase 6 — General availability

**Goal:** India launch on Google Play and App Store.

- [ ] All security release blockers resolved
- [ ] Domain and trademark clearance complete
- [ ] Privacy policy and terms published
- [ ] Grievance officer designated
- [ ] Performance benchmarks on all representative devices meet targets in BENCHMARK_PLAN.md

---

## Phase 7 — Cloud provider migration feasibility (planning assumption, not a committed deadline)

If a provider migration is ever approved, see [FIREBASE_TO_AWS_RUNBOOK.md](../operations/FIREBASE_TO_AWS_RUNBOOK.md) for the feasibility procedures. AWS implementation is not part of the current product phase.

---

## Release blockers

See [PRODUCTION_GATE.md](PRODUCTION_GATE.md) and [docs/security/SECURITY_BASELINE.md](../security/SECURITY_BASELINE.md).
