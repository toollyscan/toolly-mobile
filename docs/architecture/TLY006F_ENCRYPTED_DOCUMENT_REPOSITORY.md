# TLY-006F — Encrypted Android Document Repository

- **Issue:** #26
- **ADR:** ADR-0012
- **Status:** Implementation candidate; qualified review and device evidence pending
- **Platform:** Android phones and tablets

## Product outcome

The capture-to-library walking slice now publishes encrypted documents instead of retaining the
development-only plaintext repository:

```text
Scanner staging → validate JPEG → encrypt metadata and assets → fsync → atomic publish
→ authenticate → library/viewer
```

Firebase is not required for this local path. No document metadata, image, key, path or user-facing
content is uploaded or logged.

## Storage boundaries

| Data | Persistent form | Location |
|------|-----------------|----------|
| Vault scope | Opaque random UUID | Android no-backup app storage |
| Document metadata | Versioned AES-GCM metadata envelope | Android no-backup app storage |
| Page image | Versioned, chunked AES-GCM asset envelope | Android no-backup app storage |
| Commit state | Non-secret marker | Android no-backup app storage |
| Scanner result | Bounded temporary plaintext owned by scanner adapter | App cache until save/discard |
| Viewer pixels | Bounded bitmap only | Memory; recycled on screen/source change |

The former plaintext repository implementation is removed from production source. Its version-one
format is handled only by the bounded migration reader.

## Asset format

Each immutable page asset receives a unique random 256-bit data key. Android Keystore protects the
wrapped key using a separate platform wrapping alias. Page bytes are encrypted as independently
authenticated 64 KiB chunks.

Authenticated context binds:

- vault scope;
- asset ID;
- object kind;
- envelope version;
- chunk index and declared chunk count;
- total plaintext length;
- key-wrap versus content purpose.

The reader rejects wrong keys, modified tags, substitution, reordering, duplication, truncation and
appended bytes. Decryption is streamed with one bounded plaintext chunk at a time. The image decoder
drains to authenticated EOF before accepting the bitmap.

## Atomic publication and migration

1. Clean abandoned encrypted staging and unpublished destination directories.
2. Encrypt every asset directly into encrypted staging.
3. Reopen and authenticate every staged asset.
4. Encrypt and sync the manifest.
5. Sync the commit marker.
6. Atomically rename staging into the committed document directory.
7. Reopen the complete document and authenticate metadata and assets.

The legacy migration follows the same protocol. A plaintext legacy document is removed only after
its encrypted replacement reopens successfully. Interrupted migration is idempotent. A blocked
migration prevents mixed plaintext/encrypted writes and never silently resets the vault.

## Tests

Pure JVM tests cover canonical asset associated data and strict header framing. Android
instrumentation sources cover:

- multi-chunk round trip through a new adapter instance;
- absence of a plaintext subsequence in the persisted envelope;
- ciphertext tamper, truncation and AAD substitution;
- missing wrapping-key failure without reset;
- encrypted repository save/reopen;
- repository corruption failure;
- verified legacy migration before plaintext deletion.

CI compiles the Android application and instrumentation APK, runs unit tests and lint, and publishes
debug APK artifacts. Physical process-death, low-storage, performance and key-invalidation evidence
remains part of issue #26's representative-device gate.

## Production boundary

This implementation uses only Android platform JCA/Keystore cryptographic primitives and adds no
runtime dependency. It is not production-approved until ADR-0012 receives qualified cryptographic
review and required Android/Apple interoperability, device, recovery and migration evidence.
