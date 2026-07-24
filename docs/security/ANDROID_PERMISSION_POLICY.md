# Android Permission and User-Selected File Policy

## Status and scope

This is Toolly's least-privilege baseline for Android phones and tablets. The TLY-006B
ML Kit spike requests no Android permissions and performs no upload, download, backup,
sync, analytics, advertising, or marketing messaging.

Every future permission must be introduced with the feature that needs it, documented,
tested on supported Android versions, and removed when the feature is removed.

## Capture

| Capability | Approved Android route | Runtime permission |
|------------|------------------------|--------------------|
| ML Kit Document Scanner | Google-owned scanner activity | None requested by Toolly |
| Future CameraX fallback | Toolly camera screen | `CAMERA`, requested just in time |
| Image import | Android Photo Picker | None |
| PDF/document import | Storage Access Framework `ACTION_OPEN_DOCUMENT` | None |

ML Kit owns its capture UI and camera access. Toolly must not request `CAMERA` merely
because ML Kit is present. A future CameraX implementation must explain the need before
requesting `CAMERA`, continue safely after denial, and provide settings recovery only
after permanent denial.

## Import, download, export, and sharing

- Photo Picker and Storage Access Framework grants are scoped to the items selected by
  the user. Toolly must validate MIME type, independent file signature, size, page limit,
  provider failures, and revoked access before processing.
- External provider URIs never enter domain models and are never sent directly to cloud
  code. Content is copied into Toolly-controlled app-private storage first.
- Cloud backup upload accepts only authenticated ciphertext and non-sensitive integrity
  metadata. Plaintext pages, thumbnails, OCR, document titles, keys, and external URIs
  are prohibited.
- Cloud download writes ciphertext to app-private temporary storage, verifies integrity
  and authenticity, then atomically promotes it into the encrypted vault. Partial,
  failed, cancelled, and duplicate transfer files are removed deterministically.
- User-visible PDF/JPEG export uses `ACTION_CREATE_DOCUMENT`.
- Sharing uses a non-exported `FileProvider`, a temporary read-only URI grant, the
  narrowest path configuration, and deterministic grant/file expiry.
- App-private files need no storage or media permission.

## Network and background transfer

`INTERNET` is an install-time normal permission and may be added only with the first
network feature. `ACCESS_NETWORK_STATE` may be added only when connection-aware retry
or messaging is implemented. Neither grants access to user files.

Background transfers use WorkManager or the platform-recommended user-initiated
transfer mechanism. Toolly does not add exact-alarm, unrestricted-background, or
foreground-service permissions speculatively. Offline, metered, roaming, cancellation,
process death, low-storage, quota, integrity-failure, and retry behavior require tests.

## Notifications

`POST_NOTIFICATIONS` is requested on Android 13+ only at contextual value, not at first
launch. Transactional/security and marketing channels are separate. Marketing requires
explicit opt-in, independent revocation, quiet hours, and frequency caps. Denial must
not block scanning, local vault access, import/export, or in-app transfer status.

Notification payloads contain only allowlisted event types and opaque references.
Document titles, OCR, filenames, paths, phone numbers, email addresses, tokens, and key
material are prohibited.

## Other protected capabilities

- Biometrics use the platform biometric prompt to unlock a Keystore-protected key; raw
  biometric data is never available to Toolly.
- Contacts, phone, SMS, location, microphone, nearby devices, Bluetooth, calendar,
  accessibility service, overlays, package installation, VPN, call logs, and device
  administration are outside the approved baseline.
- A future capability requiring one of these permissions needs a separate product,
  security, privacy, store-policy, and data-retention review.

## Prohibited baseline permissions

The following must not appear in the supported baseline:

- `MANAGE_EXTERNAL_STORAGE`
- `READ_EXTERNAL_STORAGE` or `WRITE_EXTERNAL_STORAGE`
- `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, or `READ_MEDIA_AUDIO`
- microphone, location, contacts, SMS, phone, call-log, calendar, or Bluetooth access
- exact alarms, overlays, accessibility service, package installation, or device admin

Manifest tests and CI enforce this policy. Permission denial, revocation, URI expiry,
process recreation, cleanup, and accessibility messaging are release-test scenarios.
