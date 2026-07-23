# ADR-0004 — Authentication and Account Boundary

| Field | Value |
|-------|-------|
| Status | Accepted |
| Date | 2025-07-01 |
| Author | shivayogih |

---

## Context

Toolly uses Firebase Authentication as the authentication provider. Firebase assigns each user a Firebase UID (a provider-scoped identifier).

If Firebase UID is used as the canonical document-owner ID throughout the application:

- Migration to another authentication provider requires rewriting all ownership references.
- Users who delete and recreate their Firebase account receive a new UID, breaking document ownership.
- The UID is a Firebase internal identifier; Toolly has no control over its format or stability.

Additionally, mobile-number authentication via OTP is the expected primary authentication method for the Indian market. OTP flows are vulnerable to abuse (rate-limiting, SIM-swap, SMS interception), requiring explicit controls.

---

## V1 authentication decision

Authentication is required before the first scan. V1 supports phone OTP, email/password, Google, and Apple Sign In on iOS. Guest scanning is excluded unless a later approved decision explicitly supersedes this ADR.

Firebase Authentication is the current identity provider behind Toolly-owned contracts. Firebase identities map to canonical `ToollyAccountId` values and never become document-owner IDs.

Toolly-owned databases must not duplicate plaintext phone numbers without an approved purpose, retention period and security review. Firebase Authentication processes provider identity data according to its configured service contract and privacy terms. Phone numbers, OTPs, passwords, tokens and provider credentials never enter application logs or analytics.

Trusted-device approval, recovery material and abuse thresholds remain evidence pending.

## Decision

1. Toolly assigns every account a **`ToollyAccountId`** (UUID v4) at registration. This is the canonical document-owner ID.
2. Firebase UID is stored as a **provider credential** in the account record, alongside the `ToollyAccountId`. It is never used as a primary key in domain models.
3. The account record maps `ToollyAccountId → [ProviderCredential]`, allowing multiple authentication providers per account.
4. OTP abuse controls:
   - Maximum 3 OTP requests per phone number per 10-minute window.
   - Maximum 5 failed OTP attempts before a 30-minute lockout.
   - OTP values are never logged, never sent to analytics and never appear in error messages.
5. Trusted-device approval:
   - A new device must be approved by an existing trusted device before accessing the vault.
   - If no trusted device is available, account recovery requires a verified backup code.
6. Account recovery:
   - Recovery codes are generated at registration, displayed once, and stored encrypted in the local vault.
   - Recovery codes are never stored in plaintext on the server.
7. Phone numbers are stored as HMAC-SHA256 hashes for lookup. Plaintext phone numbers are never persisted in the database or logs.

---

## Consequences

**Positive:**

- `ToollyAccountId` is stable across authentication provider migrations.
- Phone numbers are not stored in recoverable plaintext.
- OTP abuse controls reduce SIM-swap and SMS interception risk.
- Multi-provider authentication is possible in future (e.g., Google, Apple Sign-In).

**Negative:**

- Account registration is more complex: two IDs must be created and linked atomically.
- Recovery-code UX requires careful design to prevent user confusion.
- HMAC lookup requires the server to hold the HMAC key securely.

---

## Rejected alternatives

| Alternative | Reason rejected |
|-------------|----------------|
| Firebase UID as canonical ID | Creates lock-in; breaks ownership on account recreation. |
| Plaintext phone number storage | Violates privacy-first requirement; DPDP Act 2023 obligation. |
| No trusted-device approval | Allows any device with valid credentials to access the vault; insufficient for a document scanner. |
