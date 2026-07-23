# ADR-0004 — Authentication and Account Boundary

| Field | Value |
|-------|-------|
| Status | Accepted; protocol evidence pending |
| Date | 2026-07-23 |
| Author | shivayogih |

---

## Context

Toolly uses Firebase Authentication initially. Provider identities are useful credentials but must not define document ownership, encryption ownership or canonical account identity.

Authentication, recovery and trusted-device controls must support phone OTP, email/password, Google and Apple Sign In without leaking provider types into domain code.

---

## Decision

1. Authentication is required before the first scan. Guest scanning is excluded from V1 unless this ADR is explicitly superseded.
2. V1 supports phone OTP, email/password, Google and Apple Sign In on iOS.
3. Toolly assigns a canonical `ToollyAccountId` at account creation.
4. Firebase identities are provider credentials mapped to `ToollyAccountId`; they never become canonical document-owner IDs.
5. Domain models use Toolly-owned identity contracts and do not import Firebase SDK types.
6. Toolly-owned databases do not duplicate plaintext phone numbers without an approved purpose, retention period and security review.
7. Firebase Authentication processes provider identity data according to its configured service contract and privacy terms.
8. Phone numbers, email addresses, OTPs, passwords, tokens and provider credentials never enter application logs or analytics.
9. OTP resend floors, abuse thresholds, lockout values, account linking, trusted-device approval and recovery protocols require implementation spikes, threat analysis and approval before finalization.
10. Returning authenticated users retain offline access to their existing local library during transient network or provider outages.
11. Identity recovery, device trust and encrypted-backup key recovery are separate domains. A recovered provider session does not by itself release backup keys.
12. Support personnel and cloud administrators have no plaintext-document or recovery-key bypass.

---

## Evidence required

- Provider-account linking and canonical-ID contract tests.
- OTP abuse and lifecycle-replay tests.
- Account deletion, recreation and provider-linking scenarios.
- Trusted-device and recovery threat model.
- Recovery usability study.
- Firebase data inventory, retention and deletion review.
- Offline returning-user behavior tests.
- Security review of key recovery and account takeover controls.

---

## Consequences

**Positive:**

- Document ownership remains stable across provider changes.
- All approved sign-in methods share one canonical identity model.
- Firebase remains fully usable without becoming the domain architecture.
- Offline local-library access is separated from transient provider availability.

**Costs and risks:**

- Account linking and recovery require explicit conflict handling.
- Provider processing and Toolly-owned storage need separate data inventories.
- Recovery and trusted-device mechanisms cannot be finalized without security and usability evidence.

---

## Rejected alternatives

| Alternative | Reason rejected |
|-------------|----------------|
| Firebase UID as canonical owner ID | Couples document ownership to one provider and account lifecycle. |
| Guest scanning in V1 | Conflicts with the approved login-before-first-scan decision. |
| OTP-only authentication | Conflicts with approved email/password, Google and Apple methods. |
| Finalize recovery codes or lockout values without evidence | Creates security and usability risk before threat analysis and testing. |

---

## Related security specifications

- [Authentication abuse controls](../security/AUTHENTICATION_ABUSE_CONTROLS.md)
- [Trusted device and recovery](../security/TRUSTED_DEVICE_AND_RECOVERY.md)
- [Firebase processing inventory](../security/FIREBASE_PROCESSING_INVENTORY.md)
- [ADR-0007 — Encryption envelope and key hierarchy](0007-encryption-envelope-and-key-hierarchy.md)
