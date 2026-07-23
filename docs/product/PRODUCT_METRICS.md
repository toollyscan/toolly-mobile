# Product Metrics

Toolly privacy-safe product metrics baseline.

---

## Principles

1. Document images, PDF content, OCR text, filenames and document metadata are **prohibited**
   from all analytics events.
2. Phone numbers, email addresses, OTPs, authentication tokens, encryption keys and recovery
   phrases are **prohibited**.
3. Raw personal identifiers (ToollyAccountId in plaintext where not required) must not appear
   in event properties unless they are necessary for the metric and appropriately hashed.
4. User-generated document categories, folder names and document titles are **prohibited**.
5. Every metric must serve a specific product decision.
6. Every metric must have a defined owner and a defined retention period.
7. Metrics must be reviewed before launch to confirm compliance with DPDP Act 2023.

---

## Prohibited analytics data (complete list)

- Document images and page thumbnails.
- PDF content or extracted text.
- OCR text or recognised data.
- Document titles and filenames.
- Folder names and user-generated categories.
- Phone numbers (plaintext or partial).
- Email addresses.
- OTP values.
- Authentication tokens and session identifiers.
- Encryption keys, derived keys and key material.
- Recovery phrases and recovery codes.
- Raw personal identifiers beyond what is required for the specific metric.

---

## Metric definitions

### M-001 — First scan completion

| Field | Value |
|-------|-------|
| Product question | What proportion of new users complete their first document scan? |
| Event name | `scan_completed` |
| Allowed properties | `is_first_scan: bool`, `page_count: int`, `enhancement_mode: enum[standard, advanced, none]`, `export_triggered: bool`, `platform: enum[android, ios]`, `app_version: string` |
| Prohibited properties | Document content, filename, folder name, phone number, account ID in plaintext. |
| Retention | 90 days |
| Owner | Product |
| Decision enabled | Onboarding funnel optimisation; feature prioritisation. |

---

### M-002 — Time to first scan

| Field | Value |
|-------|-------|
| Product question | How long does it take a new user to complete their first scan from first app open? |
| Event name | `first_scan_duration` |
| Allowed properties | `duration_seconds: int` (rounded to nearest 10 s), `platform: enum[android, ios]`, `app_version: string` |
| Prohibited properties | Any document content, filename, phone number, account ID in plaintext. |
| Retention | 90 days |
| Owner | Product |
| Decision enabled | Onboarding friction identification; capture flow improvement. |

---

### M-003 — Capture completion

| Field | Value |
|-------|-------|
| Product question | What proportion of capture sessions result in a saved document? |
| Event name | `capture_session_result` |
| Allowed properties | `result: enum[saved, abandoned, error]`, `page_count: int`, `platform: enum[android, ios]`, `app_version: string` |
| Prohibited properties | Document content, filename, phone number, account ID in plaintext. |
| Retention | 90 days |
| Owner | Product |
| Decision enabled | Capture flow quality; abandonment root cause. |

---

### M-004 — Export completion

| Field | Value |
|-------|-------|
| Product question | What proportion of users successfully export a document after scanning? |
| Event name | `export_completed` |
| Allowed properties | `format: enum[pdf, jpeg]`, `destination: enum[share, files, other]`, `platform: enum[android, ios]`, `app_version: string` |
| Prohibited properties | Document content, filename, phone number, account ID in plaintext. |
| Retention | 90 days |
| Owner | Product |
| Decision enabled | Export funnel quality; format preference. |

---

### M-005 — Subscription conversion

| Field | Value |
|-------|-------|
| Product question | What proportion of free users convert to a paid subscription? |
| Event name | `subscription_conversion` |
| Allowed properties | `plan: enum[monthly, annual]`, `trigger: enum[paywall, settings, trial_end]`, `platform: enum[android, ios]`, `app_version: string` |
| Prohibited properties | Price paid, phone number, account ID in plaintext, any document data. |
| Retention | 365 days |
| Owner | Product / Revenue |
| Decision enabled | Paywall placement; plan mix optimisation. |

---

### M-006 — Trial conversion

| Field | Value |
|-------|-------|
| Product question | What proportion of trial users convert to a paid plan? |
| Event name | `trial_conversion` |
| Allowed properties | `converted: bool`, `plan: enum[monthly, annual, none]`, `trial_duration_days: int`, `platform: enum[android, ios]`, `app_version: string` |
| Prohibited properties | Price, phone number, account ID in plaintext, any document data. |
| Retention | 365 days |
| Owner | Product / Revenue |
| Decision enabled | Trial length optimisation; trial-to-annual funnel. |

---

### M-007 — Renewal

| Field | Value |
|-------|-------|
| Product question | What proportion of subscribers renew at the end of their billing period? |
| Event name | `subscription_renewed` |
| Allowed properties | `plan: enum[monthly, annual]`, `renewal_number: int`, `platform: enum[android, ios]`, `app_version: string` |
| Prohibited properties | Price, phone number, account ID in plaintext. |
| Retention | 365 days |
| Owner | Revenue |
| Decision enabled | Retention and churn modelling; pricing validation. |

---

### M-008 — Cancellation

| Field | Value |
|-------|-------|
| Product question | What is the subscription cancellation rate and at what stage does it occur? |
| Event name | `subscription_cancelled` |
| Allowed properties | `plan: enum[monthly, annual]`, `renewal_number: int`, `platform: enum[android, ios]`, `app_version: string` |
| Prohibited properties | Cancellation reason text (user-generated), phone number, account ID in plaintext. |
| Retention | 365 days |
| Owner | Revenue |
| Decision enabled | Churn root-cause analysis; retention improvement. |

---

### M-009 — Restore purchase success

| Field | Value |
|-------|-------|
| Product question | What proportion of restore-purchase attempts succeed? |
| Event name | `restore_purchase_result` |
| Allowed properties | `result: enum[success, no_purchase_found, error]`, `platform: enum[android, ios]`, `app_version: string` |
| Prohibited properties | Phone number, account ID in plaintext, any document data. |
| Retention | 90 days |
| Owner | Product |
| Decision enabled | Restore flow reliability; customer-support reduction. |

---

### M-010 — Backup opt-in

| Field | Value |
|-------|-------|
| Product question | What proportion of premium users enable cloud backup? |
| Event name | `backup_opted_in` |
| Allowed properties | `platform: enum[android, ios]`, `app_version: string` |
| Prohibited properties | Phone number, account ID in plaintext, document count, document names. |
| Retention | 90 days |
| Owner | Product |
| Decision enabled | Backup feature adoption; cloud cost projection. |

---

### M-011 — Backup success

| Field | Value |
|-------|-------|
| Product question | What proportion of initiated backup operations complete successfully? |
| Event name | `backup_result` |
| Allowed properties | `result: enum[success, partial, failed]`, `failure_reason: enum[network, quota, auth, unknown]`, `platform: enum[android, ios]`, `app_version: string` |
| Prohibited properties | Document count, document size, document names, phone number, account ID in plaintext. |
| Retention | 90 days |
| Owner | Engineering / Product |
| Decision enabled | Backup reliability; quota sizing. |

---

### M-012 — Recovery success

| Field | Value |
|-------|-------|
| Product question | What proportion of account recovery attempts succeed? |
| Event name | `recovery_result` |
| Allowed properties | `result: enum[success, failed]`, `failure_reason: enum[invalid_code, expired, max_attempts, unknown]`, `platform: enum[android, ios]`, `app_version: string` |
| Prohibited properties | Recovery code, phone number, account ID in plaintext, OTP value. |
| Retention | 90 days |
| Owner | Engineering / Security |
| Decision enabled | Recovery flow reliability; security audit. |

---

### M-013 — Crash-free users

| Field | Value |
|-------|-------|
| Product question | What proportion of users experience no app crash in a session? |
| Event name | Collected by crash-reporting SDK (e.g. Firebase Crashlytics). |
| Allowed properties | Stack trace (no PII), app version, OS version, device model (anonymised). |
| Prohibited properties | Document content, OCR text, filenames, phone number, account ID in plaintext. |
| Retention | 90 days |
| Owner | Engineering |
| Decision enabled | Stability targeting; release gate. |

---

### M-014 — ANR-free users (Android)

| Field | Value |
|-------|-------|
| Product question | What proportion of Android users experience no ANR in a session? |
| Event name | Collected by Android Vitals / crash-reporting SDK. |
| Allowed properties | ANR trace (no PII), app version, OS version, device model (anonymised). |
| Prohibited properties | Document content, OCR text, filenames, phone number, account ID in plaintext. |
| Retention | 90 days |
| Owner | Engineering |
| Decision enabled | Stability targeting; Play Store rating protection. |

---

### M-015 — Support-contact rate

| Field | Value |
|-------|-------|
| Product question | What proportion of users contact support each month? |
| Event name | `support_contact_initiated` |
| Allowed properties | `channel: enum[email, in_app]`, `platform: enum[android, ios]`, `app_version: string`, `subscription_tier: enum[free, premium]` |
| Prohibited properties | Support message content, phone number, account ID in plaintext, document data. |
| Retention | 90 days |
| Owner | Support / Product |
| Decision enabled | Support cost modelling; UX improvement prioritisation. |

---

## Analytics implementation requirements

1. All analytics events must be reviewed against the prohibited-data list before shipping.
2. Analytics SDK must not receive document content, images or OCR output.
3. Analytics SDK must not receive phone numbers, OTP values or authentication tokens.
4. Analytics events must be reviewed for DPDP Act 2023 compliance before launch.
5. Users must be informed of analytics data collection in the privacy notice.
6. Users must have a mechanism to opt out of analytics (beyond crash reporting where platform-mandated).
7. Analytics retention periods must be configured in the analytics platform to match the values above.
