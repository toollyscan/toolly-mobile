# Security Baseline

This document summarises the security threat model, controls and compliance posture for Toolly.

---

## Threat model summary

### Assets

| Asset | Sensitivity | Location |
|-------|-------------|----------|
| Document images and pages | High | Local vault (encrypted) |
| OCR text | High | Local vault (encrypted) |
| Phone number | High | HMAC-SHA256 hash only |
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

## Cryptography

| Purpose | Algorithm | Key storage |
|---------|-----------|-------------|
| Vault encryption | AES-256-GCM | Android Keystore / iOS Secure Enclave |
| Cloud backup encryption | AES-256-GCM | Key derived from vault master key |
| Phone number lookup | HMAC-SHA256 | Server-side HMAC key |
| Recovery codes | CSPRNG (256-bit entropy) | Vault (encrypted) |

---

## Authentication controls

- Primary method: phone number OTP.
- OTP rate limit: 3 requests per phone number per 10-minute window.
- OTP failure lockout: 5 failed attempts triggers a 30-minute lockout.
- OTP values are in-memory only. They are never logged, persisted or sent to analytics.
- New device access requires trusted-device approval or recovery code.

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
| Data minimisation | Partial | Phone number stored as HMAC only. |
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
