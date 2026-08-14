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

## Phase 1 — Cross-platform architecture prototype

**Goal:** Establish shared Android/iOS product foundations and validate the encrypted vault.

- [ ] KMP module structure for domain, use cases, ports and presentation state
- [ ] Compose Multiplatform application shell for Android phone/tablet and iPhone/iPad
- [ ] Release-shaped splash, tutorial, welcome/auth and authenticated home navigation shared across platforms (TLY-013)
- [ ] Debug-only local authentication adapter that fails closed in release configuration (TLY-013)
- [ ] Shared English, Hindi and Kannada resources with native-language review
- [ ] Shared accessibility semantics with TalkBack and VoiceOver evidence
- [ ] Android and iOS composition roots with provider-neutral platform adapters
- [ ] ADR-0012 Android Keystore/JCA and Apple Keychain/CryptoKit encrypted-vault prototypes,
  including encrypted metadata, asset envelopes, migration, nonce, key-invalidation and failure tests
- [ ] ML Kit Android scanner adapter and Apple VisionKit/AVFoundation adapter
- [ ] Manual capture/crop fallback boundary on both platforms
- [ ] Image loading proving no decrypted disk cache on either platform
- [ ] Local and push notification prototype covering safe payloads, consent and token lifecycle
- [ ] Common, Android and iOS CI build/test gates
- [ ] Production Gate review

---

## Phase 2 — Core document product (Android and iOS)

**Goal:** Deliver the same capture, local vault, library, viewer and export outcomes on Android
phones/tablets and iPhone/iPad.

*Blocked until Production Gate is approved. Temporary platform gaps must be tracked in the parity
matrix and block beta/release approval.*

- [ ] Native capture adapters behind the shared Toolly scanner port
- [ ] Shared page review, ordering, cropping and enhancement behavior
- [ ] Encrypted vault persistence and recovery on both platforms
- [ ] Shared document library and viewer UI
- [ ] Local PDF/JPEG export through platform adapters
- [ ] English, Hindi and Kannada UI strings
- [ ] TalkBack and VoiceOver accessibility evidence
- [ ] Compact, medium and expanded layouts; landscape and multi-window behavior
- [ ] Security, processing, backup, billing and consented marketing notification categories
- [ ] Representative Android phone/tablet and iPhone/iPad benchmarks
- [ ] Platform parity matrix fully verified

---

## Phase 3 — Product hardening and parity verification

**Goal:** Remove temporary platform gaps and prove release-quality behavior.

- [ ] Shared feature acceptance suite passes on Android and iOS
- [ ] Capture, vault, export and recovery failure paths pass on representative physical devices
- [ ] Accessibility, localization and adaptive-layout audits pass
- [ ] Privacy, permission and data-retention behavior passes platform review
- [ ] Performance, memory, battery and startup targets pass
- [ ] No unexplained Android-only or iOS-only features remain

---

## Phase 4 — Authentication and account

**Goal:** Approved multi-provider authentication with canonical account identity.

*Connecting any build to the `production` Firebase project, or processing real user documents
through Phase 4 code, remains blocked until Production Gate is approved. Dev-environment (
`toollyscan-dev`) implementation work is not blocked by that gate and has started -- see D-050.*

- [ ] `ToollyAccountId` generation at registration -- interim only: a device-local placeholder is
  minted per Firebase UID (`FirebaseAccountAuthenticator`); not yet the server-assigned,
  cross-device-portable identity this item requires (see D-050)
- [ ] Firebase Authentication integration behind the canonical authentication port -- Android
  adapter implemented for email/password and pure-phone sign-in, bound to `toollyscan-dev`; not
  yet reviewed/approved as complete
- [ ] Phone OTP, email/password, Google and Apple Sign In on iOS -- Android has phone OTP and
  email/password; Google/Apple are no-ops pending a provider consent-UI adapter; iOS has no
  `AccountAuthenticator` adapter at all yet
- [ ] Provider-account linking and canonical-ID contract tests -- not implemented; the post-signup
  phone-verification-as-linking step is still local-only pending this
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

- [x] P0: Android physical-device encrypted Save fails closed with `ToollyErrorCode.CORRUPT` (TLY-011A / #51) —
  fixed (Android Keystore IV handling; provider-generated nonce, not caller-supplied) and merged to `main` via
  PR #55 (`02aded6`). Physical-device evidence: OnePlus Nord CE5, Android 16, single/multi-page save, order
  preserved, force-stop/restart/offline reopen all PASS (build SHA `3ae3d7b`, PR #55 comment, 2026-08-05).
- [ ] Release application shell and platform-host integration incomplete (TLY-013 / #52) — the Android shell,
  auth journey, crop/enhance port, export/share, backup/privacy UI and document naming/search are now merged
  to `main` (PR #55). Still incomplete: Apple capture is a tested port with no Swift host or iOS vault wired
  up yet (#48), so iOS remains capture-stubbed; and physical-device evidence for everything landed in PR #55
  beyond the original walking slice is still outstanding.

See [PRODUCTION_GATE.md](PRODUCTION_GATE.md) and [docs/security/SECURITY_BASELINE.md](../security/SECURITY_BASELINE.md).
