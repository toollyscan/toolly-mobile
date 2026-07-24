# Android Runtime Stack and Security Profile

## Purpose

This document is the implementation-facing baseline for Toolly's first Android phone and tablet
walking slice. It records which SDK owns each capability, the Toolly boundary around that SDK and
the security controls required before release.

No coordinate in this document may be added to Gradle until the dependency registry, lock,
verification and SBOM requirements are satisfied.

## Walking-slice stack

| Flow | SDK or library | Toolly boundary | Required controls |
|------|----------------|-----------------|-------------------|
| Capture or gallery import | ML Kit Document Scanner | `DocumentScanner` | Handle dynamic download, unsupported devices, cancellation and temporary-result deletion |
| Capture fallback | CameraX and Android Photo Picker | `DocumentScanner`, `DocumentImporter` | Camera lifecycle, permissions, rotation, manual crop and low-memory tests |
| Boundary detection and edit | ML Kit first; Toolly geometry contracts | `BoundaryEngine` | Canonical normalized coordinates; no SDK geometry types in domain |
| Crop and enhancement | ML Kit first; versioned Toolly recipe | `PageProcessor` | Preserve original, record recipe/version and verify deterministic export |
| Metadata database | Room with current SQLCipher Android | `VaultMetadataStore` | Keystore-protected random passphrase, migrations, corruption and recovery |
| Asset vault | App-private files with Toolly encryption envelope | `VaultAssetStore` | Authenticated encryption, unique nonces, atomic writes and no plaintext backup |
| Images and thumbnails | Android bitmap APIs | `VaultImageLoader` | Off-main bounded decode, no persistent plaintext cache, clear memory on vault lock |
| Document library | Compose Material 3 Adaptive, Paging and Flow | `DocumentRepository` | Compact/medium/expanded layouts, accessibility and stable paging keys |
| PDF preview | Android `PdfRenderer` | `PdfPreviewer` | Worker-thread rendering, bounded bitmap size and untrusted-file handling |
| PDF generation | Android `PdfDocument` | `DocumentExporter` | Background execution, cancellation, atomic finalization and cleanup |
| JPEG generation | Android bitmap encoding | `DocumentExporter` | Quality bounds, metadata stripping and memory limits |
| Export and share | Storage Access Framework and FileProvider | `ShareGateway` | Short-lived content URI grants and temporary-file deletion |
| Background tasks | WorkManager | `BackgroundJobScheduler` | Idempotency, retry bounds, cancellation and foreground progress where required |
| Vault unlock | Android Keystore and BiometricPrompt | `VaultKeyProtector` | Key invalidation, fallback policy, lock timeout and failed-attempt behavior |
| Authentication | Firebase Authentication | `AuthenticationPort` | Canonical Toolly account ID, provider linking and abuse controls |
| Cloud metadata | Firestore | `CloudMetadataPort` | No plaintext document content; least-privilege Rules and deletion |
| Encrypted backup | Cloud Storage | `BackupObjectPort` | Explicit opt-in, ciphertext only, resumable integrity verification |
| Backend operations | Cloud Functions | Toolly application ports | Authentication, authorization, App Check, idempotency and bounded retries |
| Remote configuration | Remote Config | Signed Toolly policy adapter | Public data only; never secrets or authorization |
| Crash diagnostics | Crashlytics | `DiagnosticsPort` | Deny-by-default allowlist; no titles, OCR, paths, PII, tokens or keys |
| Remote notifications | Firebase Cloud Messaging | `RemoteNotificationGateway` | Consent/category policy, opaque payloads and token lifecycle |
| Local notifications | Android NotificationManager and WorkManager | `LocalNotificationScheduler` | Channel controls, lock-screen privacy and deduplication |

## Dependency rules

1. Domain and use-case modules import no Android, Google Play services, Firebase, Room or SQLCipher types.
2. Every SDK has one adapter owned by a platform or infrastructure module.
3. SDK-specific models are mapped at the adapter boundary.
4. No decrypted page or thumbnail is stored in any persistent image cache.
5. SQLCipher encrypts the database; Toolly asset encryption separately protects binary files.
6. Firebase receives no plaintext document page, thumbnail, title, OCR text or encryption key.
7. Notifications receive no sensitive document data.
8. Scanner, vault and local export remain usable when Firebase is unavailable.
9. Marketing consent never gates security, local scanning, vault access or document export.
10. A dependency may be replaced without changing domain models or use-case APIs.

## Proposed Android modules

```text
app-android
core-foundation
core-domain
core-contracts
core-security
core-ui
feature-auth
feature-scan
feature-library
feature-export
feature-settings
adapter-scanner-mlkit
adapter-scanner-camerax
adapter-vault-room-sqlcipher
adapter-vault-files
adapter-image-platform
adapter-firebase
adapter-notifications
```

Physical modules should be introduced incrementally. A module is justified by ownership,
replacement, security or build boundaries rather than by folder count.

## Phone and tablet acceptance

| Area | Phone | Tablet |
|------|-------|--------|
| Navigation | Bottom navigation or compact navigation | Navigation rail or adaptive drawer |
| Library | Single-pane grid/list | List-detail or responsive grid |
| Editor | Main page with bottom thumbnails/tools | Main page with side thumbnails/tools |
| Crop | Full-screen canvas with bottom controls | Larger canvas with side or bottom controls |
| Export | Sheet or full-screen flow | Centered panel or side panel |
| Settings | Single-pane | List-detail |
| Orientation | Portrait and landscape | Portrait, landscape and multi-window |

Layouts use window width and posture, not a hard-coded tablet boolean.

## Future web boundary

The web client reuses canonical models, use cases, validation, encrypted-envelope definitions,
notification categories and sync contracts. It does not reuse Android adapters.

Expected future replacements include browser capture/import, Web Crypto, IndexedDB or OPFS,
Firebase Web or a provider-neutral API adapter, and Web Push. The web UI technology remains
deferred until a production spike.
