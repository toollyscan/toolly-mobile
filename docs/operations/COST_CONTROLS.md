# Cost Controls

This document describes the Firebase budget, quota and kill-switch controls for Toolly.

---

## Firebase budget alerts

Configure budget alerts in the Google Cloud Console under **Billing → Budgets & alerts**.

| Alert threshold | Action |
|----------------|--------|
| 50 % of monthly budget | Notify `shivayogih` by email. |
| 80 % of monthly budget | Notify `shivayogih` by email; review usage report. |
| 100 % of monthly budget | Notify `shivayogih` by email; consider kill-switch activation. |

The monthly budget must be set before any production traffic is enabled.

---

## Firebase quota limits

Apply the following quota limits in the Firebase Console and Google Cloud Console to prevent runaway spend.

### Firebase Authentication

| Quota | Limit | Rationale |
|-------|-------|-----------|
| SMS OTP requests per day | 1 000 (staging) / 10 000 (production) | Prevents OTP abuse and unexpected cost. |
| Anonymous sign-in per day | Disabled | Toolly requires phone authentication. |

### Firebase Storage

| Quota | Limit | Rationale |
|-------|-------|-----------|
| Daily download bandwidth | 50 GB (initial) | Prevents accidental egress cost. |
| Daily upload bandwidth | 10 GB (initial) | Review and increase as user base grows. |
| Maximum object size | 50 MB | Limit individual document upload size. |

### Firestore (if used)

| Quota | Limit | Rationale |
|-------|-------|-----------|
| Daily reads | 100 000 (initial) | Prevents accidental read amplification. |
| Daily writes | 20 000 (initial) | Review and increase as user base grows. |

---

## Kill-switch controls

A kill switch disables cloud sync for all users without a new application release.

### Firebase Remote Config kill switch

Configure a Remote Config parameter `cloud_sync_enabled` with a default value of `true`.

The sync engine must check this parameter before every sync operation:

```kotlin
if (remoteConfig.getBoolean("cloud_sync_enabled") == false) {
    // Skip sync; queue for retry when kill switch is lifted
    return
}
```

To activate the kill switch:

1. Set `cloud_sync_enabled = false` in the Firebase Remote Config console.
2. Publish the change.
3. Within 15 minutes, all active clients will stop syncing.
4. Document the activation in the operations log below.

To deactivate the kill switch:

1. Set `cloud_sync_enabled = true` in the Firebase Remote Config console.
2. Publish the change.
3. Clients will resume sync automatically.

---

## OTP cost controls

| Control | Value |
|---------|-------|
| Maximum OTP requests per phone number per 10 minutes | 3 |
| Maximum failed OTP attempts before lockout | 5 |
| Lockout duration | 30 minutes |

These controls are enforced in the application and must also be enforced server-side via Firebase App Check and Firebase Authentication rules.

---

## Cost review cadence

| Frequency | Action |
|-----------|--------|
| Weekly | Review Firebase billing dashboard for anomalies. |
| Monthly | Compare actual spend to budget; adjust quotas if necessary. |
| Quarterly | Review quota limits against user growth; plan budget for the next quarter. |

---

## Operations log

Record any kill-switch activations and significant budget events here.

| Date | Event | Activated by | Notes |
|------|-------|-------------|-------|
| — | — | — | No events recorded. |

---

## Related documents

- [FIREBASE_TO_AWS_RUNBOOK.md](FIREBASE_TO_AWS_RUNBOOK.md)
- [ADR-0003 — Cloud provider portability](../adr/0003-cloud-provider-portability.md)
