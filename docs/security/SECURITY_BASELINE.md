# Security Baseline

This document defines Toolly's security objectives and proposed baseline. Cryptographic algorithms, key derivation, recovery protocols, OTP thresholds and trusted-device mechanisms remain evidence pending until implementation spikes and qualified security review.

---

## Threat model summary

### Assets

| Asset | Sensitivity | Location |
|-------|-------------|----------|
| Document images and pages | High | Local vault (encrypted) |
| OCR text | High | Local vault (encrypted) |
| Phone identity | High | Firebase Authentication provider record; Toolly-owned duplication requires approved purpose |
| ToollyAccountId | Medium | Local vault, cloud record |
| Encryption keys | Critical | Platform hardware keystore |
| Recovery codes | Critical | Local vault (encrypted), user-held |
| OTP values | Critical | In-memory only; never persisted |
| Firebase credentials | High | Outside source control; environment-managed |

### Threats and controls

| Threat | Control |
|--------|---------|
| Device theft or loss | AES-256-GCM vault encryption; hardware-backed keys |
| Cloud storage breach | End-to-end encryption; provider cannot decrypt |
| OTP interception | OTP never logged; rate-limited; lockout enforced |
| SIM-swap attack | Trusted-device approval required for new devices |
| Credential stuffing | OTP-based auth; no password; rate limiting |
| Malicious dependency | Dependency policy: licence, CVE, size and removal analysis required |
| Secret in source control | Gitleaks in CI; no credentials committed |
| Log exfiltration | Document content, PII and key material never logged |
| Provider lock-in | Provider-neutral contracts; canonical Toolly IDs |
| Firebase UID takeover | ToollyAccountId is canonical; Firebase UID is a credential |

---

## Cryptography maturity

| Security objective | Proposed mechanism | Required evidence | Status |
|--------------------|--------------------|-------------------|--------|
| Local vault confidentiality and integrity | Authenticated encryption with per-asset or per-document data keys | Algorithm/nonce review, benchmark and fault-injection tests | Evidence pending |
| Platform key protection | Android Keystore and Apple Keychain/Secure Enclave capabilities through adapters | Device matrix and recovery analysis | Evidence pending |
| Encrypted cloud backup | Client-side encryption and versioned wrapped-key envelopes | Restore, rotation and compromise-recovery drill | Evidence pending |
| Account recovery | Trusted-device approval and/or user-held recovery material | Threat model, usability study and cryptographic review | Evidence pending |

No algorithm, derivation method, entropy value or recovery format is approved by this document alone.

---

## Authentication controls

- Login is required before the first scan.
- V1 supports phone OTP, email/password, Google, and Apple Sign In on iOS.
- OTP resend floors, rate limits, progressive backoff and lockout thresholds require abuse testing before final values are approved.
- OTP values, passwords, tokens and provider credentials are never written to application logs or analytics.
- Trusted-device approval and recovery protocols remain evidence pending.
- Firebase identities map to canonical Toolly account IDs and never become document-owner IDs.

---

## Privacy controls

The following data must never appear in logs, crash reports, analytics or error messages:

- Document images, page content and OCR text.
- Filenames and document metadata.
- Phone numbers (even hashed) in user-facing error messages.
- Email addresses.
- OTP values.
- Authentication tokens or session identifiers.
- Encryption keys or key material.
- Firebase UIDs in domain-layer logs.

---

## Indian data-protection readiness (DPDP Act 2023)

| Requirement | Status | Notes |
|-------------|--------|-------|
| Data principal consent | Planned | Consent must be obtained before cloud backup is enabled. |
| Purpose limitation | Planned | Document data used only for scanning and export. |
| Data minimisation | Partial | Toolly-owned stores avoid duplicating provider identity data without an approved purpose. |
| Data localisation | Under review | Firebase data residency for India must be confirmed. |
| Right to erasure | Planned | Account deletion must purge vault and cloud objects. |
| Grievance officer | Pending | Must be designated before launch. |

---

## Release blockers

The following security items must be resolved before any production release:

- [ ] Domain and trademark clearance for toollyscan.com / toollyscan.in.
- [ ] Firebase data-residency configuration confirmed for Indian user data.
- [ ] DPDP Act 2023 obligations mapped and consent flow implemented.
- [ ] Vault encryption and key management reviewed by a qualified security practitioner.
- [ ] OTP abuse controls tested and verified in a staging environment.
- [ ] Trusted-device approval and account-recovery flows tested end to end.
- [ ] Gitleaks scan passes on every commit to main.

---

## Related documents

- [ADR-0002 — Local vault source of truth](../adr/0002-local-vault-source-of-truth.md)
- [ADR-0003 — Cloud provider portability](../adr/0003-cloud-provider-portability.md)
- [ADR-0004 — Authentication and account boundary](../adr/0004-authentication-and-account-boundary.md)
- [SECURITY.md](../../SECURITY.md)
