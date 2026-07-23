# Product Roadmap

This roadmap describes the phases of Toolly development. Dates are indicative.

---

## Phase 0 — Repository foundation (current)

**Goal:** Establish governance, documentation and architecture.

- [x] Repository governance files (README, CONTRIBUTING, SECURITY, CODEOWNERS)
- [x] Architecture overview and ADRs
- [x] Security baseline
- [x] Definition of done and benchmark plan
- [x] CI: Markdown lint and secret scan
- [x] Product scope, entitlements, monetization and subscription lifecycle (TLY-002)
- [x] India launch scope and product metrics (TLY-002)
- [ ] Domain and trademark clearance
- [ ] GitHub branch protection and environment configuration (see GITHUB_SETUP.md)

---

## Phase 1 — Architecture prototype

**Goal:** Validate the KMP boundary and encrypted vault before production implementation.

- [ ] KMP module structure (domain, data interfaces)
- [ ] Encrypted local vault prototype (SQLCipher or platform encryption evaluation)
- [ ] `expect`/`actual` camera capture boundary prototype
- [ ] Compose Multiplatform rendering benchmark on representative devices (DA-001)
- [ ] OCR engine evaluation on Indian documents (DA-004)
- [ ] Production Gate review

---

## Phase 2 — Core document capture (Android)

**Goal:** End-to-end document capture, storage and export on Android.

*Blocked until Production Gate is approved.*

- [ ] Camera capture (no commercial SDK)
- [ ] Page cropping and enhancement
- [ ] Encrypted vault persistence
- [ ] Local PDF/JPEG export
- [ ] English, Hindi and Kannada UI strings
- [ ] Accessibility (TalkBack, minimum WCAG 2.1 AA)
- [ ] Benchmark on representative Android devices

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

**Goal:** Phone-number OTP authentication with canonical account identity.

*Blocked until Production Gate is approved.*

- [ ] `ToollyAccountId` generation at registration
- [ ] Firebase Authentication integration (behind provider-neutral interface)
- [ ] OTP abuse controls (rate limiting, lockout)
- [ ] Trusted-device approval
- [ ] Account recovery via recovery codes
- [ ] Phone number stored as HMAC only

---

## Phase 5 — Optional cloud backup

**Goal:** End-to-end encrypted cloud backup and sync.

*Blocked until Phase 4 is complete and data-residency requirements are confirmed.*

- [ ] Provider-neutral sync engine
- [ ] Firebase Storage implementation
- [ ] Resumable upload and download
- [ ] Conflict resolution (local-wins)
- [ ] User-controlled backup toggle
- [ ] DPDP Act 2023 consent flow

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

## Phase 7 — Firebase-to-AWS migration (planned)

See [FIREBASE_TO_AWS_RUNBOOK.md](../operations/FIREBASE_TO_AWS_RUNBOOK.md).

---

## Release blockers

See [PRODUCTION_GATE.md](PRODUCTION_GATE.md) and [docs/security/SECURITY_BASELINE.md](../security/SECURITY_BASELINE.md).
