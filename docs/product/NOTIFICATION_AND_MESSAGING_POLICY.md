# Notification and Messaging Policy

## Purpose

Toolly uses notifications for security, local processing, backup, account, billing, reminders,
product communication and consented marketing. Notifications must be relevant, user-controlled,
privacy-safe and consistent across phones, tablets and future platforms.

System notification permission is not marketing consent. Marketing consent is separate,
purpose-specific, recorded and independently revocable.

## Notification categories

| Category | Android channel | Examples | Source | Default |
|----------|-----------------|----------|--------|---------|
| Security | `toolly_security` | New login, recovery change, suspicious activity, breach notice | Remote | Enabled after notification permission |
| Scan processing | `toolly_scan_processing` | OCR complete, PDF ready, export failed | Local | Enabled |
| Backup and sync | `toolly_backup_sync` | Backup complete/failed, restore complete, Wi-Fi required | Local and remote | Enabled |
| Account | `toolly_account` | Verification, provider linked, deletion progress | Remote | Enabled |
| Billing | `toolly_billing` | Trial ending, payment result, renewal or cancellation | Remote | Enabled |
| Reminders | `toolly_reminders` | Unfinished scan, backup disabled, low storage | Local | User controlled |
| Product updates | `toolly_product_updates` | Important update, maintenance, major feature | Remote | User controlled |
| Tips | `toolly_tips` | Better scanning and vault-security guidance | Local or remote | User controlled |
| Offers | `toolly_offers` | Premium offer, referral or seasonal promotion | Remote | Explicit opt-in |

Android channel settings and in-app preferences are both respected. Toolly never attempts to
circumvent an operating-system notification decision.

## Permission experience

On Android 13 and later, Toolly requests `POST_NOTIFICATIONS` only after explaining the value in
context. Denial does not block login, scanning, vault access, export or settings.

The preference screen provides:

- A master notification status with a link to system settings.
- Individual controls for reminders, product updates, tips and offers.
- Lock-screen preview controls.
- Quiet hours.
- Marketing-consent status and withdrawal.
- Per-device notification registration status.

## Local and remote delivery

Local notifications use WorkManager and Android NotificationManager for durable work and
user-configured reminders. Exact alarms are not used unless a future approved feature has a
legitimate exact-time requirement.

Remote notifications use Firebase Cloud Messaging through a trusted Cloud Function or backend.
Server credentials never ship in the client. Long processing triggered by FCM is delegated to
WorkManager.

High priority is limited to urgent, user-visible security or account events. Marketing, tips and
routine updates use normal priority.

## Payload contract

Remote payloads contain only an event type, random event ID, opaque resource reference, schema
version, creation time and expiry time.

Example:

```json
{
  "schemaVersion": 1,
  "event": "BACKUP_COMPLETED",
  "eventId": "random-event-id",
  "resourceId": "opaque-resource-id",
  "createdAt": "2026-07-24T10:00:00Z",
  "expiresAt": "2026-07-25T10:00:00Z"
}
```

The following are prohibited in notification payloads, topic names, analytics and lock-screen text:

- Page images or thumbnails.
- Document titles, filenames, folders or tags.
- OCR text or extracted entities.
- Filesystem paths or storage URLs.
- Phone numbers, email addresses or account-provider tokens.
- Encryption keys, recovery material or database passphrases.
- Payment instruments or detailed billing information.

Generic copy is used on the lock screen. For example, use "Your encrypted backup is complete"
instead of naming the document.

## Client processing

The notification client:

1. Validates the schema, event allowlist, timestamps and expiry.
2. Deduplicates by event ID.
3. Checks account, device, category and consent state.
4. Resolves sensitive details only after app unlock and authorization.
5. Uses safe deep links with typed routes.
6. Schedules longer work through WorkManager.
7. Records only privacy-safe delivery outcomes.
8. Handles token rotation, logout and account deletion.

FCM registration tokens are treated as sensitive operational identifiers. They are stored only for
the authenticated device, protected by least-privilege access, rotated when refreshed and removed
on logout, device removal or account deletion.

Topics must be generic and non-personal. Email addresses, phone numbers, Toolly account IDs and
document-derived values are prohibited topic names.

## Marketing consent

Marketing is not enabled by notification permission, account creation, subscription purchase or
acceptance of general terms.

Before subscribing a device to offers, Toolly records:

| Field | Purpose |
|-------|---------|
| Consent purpose and version | Proves what the user accepted |
| Account and device reference | Applies consent to the intended destination |
| Locale and timezone | Delivers comprehensible copy at an appropriate time |
| Granted timestamp and source | Audit evidence |
| Withdrawal timestamp and source | Stops future marketing |
| Policy jurisdiction/version | Supports regional compliance review |

Marketing controls:

- Explicit opt-in with no preselected checkbox.
- One-step withdrawal in notification settings.
- No targeting based on document content or vault behavior.
- No unrelated third-party advertisements.
- No system-warning imitation.
- Frequency cap of two promotional notifications per rolling seven days.
- Default quiet hours from 21:00 to 09:00 local time.
- Campaign TTL and suppression after conversion or withdrawal.
- No personalized marketing to children without separately approved legal and product controls.

Transactional messages cannot contain promotional copy merely to bypass marketing consent.

## Measurement

Google Analytics remains disabled initially. Notification measurement is limited to the minimum
approved aggregate events:

- Send accepted or rejected.
- Delivery where the platform reports it.
- Open.
- User-visible failure.
- Offer redemption.
- Consent granted or withdrawn.

Campaign identifiers are random and not connected to document activity. Any expansion of
measurement requires an update to the telemetry allowlist, Firebase processing inventory, privacy
notice and dependency review.

## SMS, email, WhatsApp and calls

Push consent does not authorize SMS, email, WhatsApp or voice marketing. These channels are
excluded from the first release.

A future channel requires separate consent, sender registration, template review, unsubscribe or
revocation handling, retention rules and jurisdiction-specific legal approval. Indian commercial
SMS and calls require the applicable TRAI commercial-communication controls.

## Cross-platform model

Shared contracts define categories, consent, safe payloads and deep-link destinations. Delivery is
implemented by platform adapters:

| Platform | Adapter |
|----------|---------|
| Android phone/tablet | FCM and Android NotificationManager |
| iPhone/iPad | APNs, optionally routed through FCM |
| Web, future | Web Push/service worker |
| Cloud provider migration, future | Toolly messaging port with an approved provider adapter |

No provider token or message type enters shared domain models.

## Release gates

Before enabling production notifications:

- Notification channels and Android 13 permission flows pass device tests.
- Lock-screen privacy is verified on supported OS versions.
- Payload schemas reject prohibited data.
- Token rotation, logout, deletion and multi-device behavior are tested.
- Marketing opt-in, withdrawal, quiet hours, caps and suppression are tested.
- Firebase App Check and backend authorization are enforced.
- Data Safety, privacy notice and Firebase processing inventory match actual behavior.
- Indian legal review confirms launch notices, consent and promotional communication controls.
- Security and breach templates are approved by accountable owners.

## References

- [Android notification permission](https://developer.android.com/develop/ui/compose/notifications/notification-permission)
- [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging)
- [FCM Android message handling](https://firebase.google.com/docs/cloud-messaging/android/receive-messages)
- [Google Play system-notification policy](https://support.google.com/googleplay/android-developer/answer/9969861)
- [TRAI advice to commercial senders](https://www.trai.gov.in/advice-to-senders)
- [Digital Personal Data Protection Rules 2025](https://www.meity.gov.in/documents/act-and-policies/digital-personal-data-protection-rules-2025-gDOxUjMtQWa)

References were revalidated on 2026-07-24.
