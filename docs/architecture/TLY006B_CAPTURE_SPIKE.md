# TLY-006B — ML Kit Capture Spike: Architecture Note

- **Issue:** toollyscan/toolly-mobile#22
- **Status:** Draft implementation; CI and physical-device evidence pending
- **Date:** 2026-07-24
- **Owner:** shivayogih

## Purpose

TLY-006B is a minimal Android phone/tablet spike for the document-capture boundary.
Only synthetic test documents are allowed. It does not include production vault,
authentication, Firebase, sync, billing, upload, or download behavior.

## Boundaries

```text
CaptureSpikeScreen
        |
        v
domain.DocumentScanner
        |
        +-- domain.FallbackDocumentScanner
              |-- mlkit.MlKitDocumentScannerAdapter
              +-- camerax.CameraXDocumentScannerAdapter (dependency-free stub)
                         |
                         v
              mlkit.TemporaryScanStore
                         |
                         v
              TemporaryAssetId (domain-safe handle)
```

The domain package contains no Android or provider types. `ScannedPage` carries a
validated Toolly `TemporaryAssetId`, not a URI, path, filename, or ML Kit object.
Provider `Uri` values remain inside the ML Kit adapter.

## Capture and fallback flow

```text
launch(config)
  -> reject concurrent request with Busy
  -> validate page limit (1..50)
  -> request ML Kit intent sender
  -> launch provider activity
  -> cancellation: Cancelled
  -> provider unavailable/init failure: invoke Toolly fallback router
  -> successful provider result: validate and copy each JPEG
  -> return Toolly temporary asset IDs
```

`FallbackDocumentScanner` invokes CameraX only for `ServiceUnavailable`. Storage,
invalid-result, lifecycle, and busy failures are not hidden by a fallback retry.
The CameraX adapter remains a stub and therefore brings no CameraX runtime dependency
or `CAMERA` permission into this spike.

ML Kit documents a minimum device total-RAM requirement of 1.7 GB. Toolly relies on the
provider's support result and does not use an invented memory-class threshold.

## Temporary asset ownership

`TemporaryScanStore` is the sole owner of spike plaintext files:

1. Open the provider URI inside the Android adapter.
2. Accept only JPEG/unknown MIME followed by JPEG SOI/EOI signature validation.
3. Enforce a 25 MiB per-page limit.
4. Copy into app-private cache as a `.part` file and sync it.
5. Rename on the same filesystem to a random 128-bit Toolly asset ID.
6. Return only `TemporaryAssetId` to the domain/UI.
7. Delete on explicit replacement/release, failed import, cancellation cleanup, or
   Activity destruction.

The spike never uploads these files or promotes them into a persistent vault.

## Adaptive and accessible UI

| Available width | Layout |
|-----------------|--------|
| `< 600 dp` | Stacked capture controls and adaptive thumbnail grid |
| `>= 600 dp` | Fixed controls pane and flexible thumbnail pane |

Android platform bitmap decoding receives app-private `File` objects at the UI adapter boundary.
Decoding is bounded and off-main, with no persistent plaintext cache. Page descriptions
contain only ordinal numbers, and status changes use a polite accessibility live region.

## Result contract

```text
ScanResult
  Success(pages)
  Cancelled
  Failure
    ServiceUnavailable | PermissionDenied | Busy | InvalidResult
    StorageFailure | LifecycleEnded
    PartialCapture(pages, allowlisted reason)
```

No free-form SDK exception message, file identifier, or document data can enter the
result contract.

## Verification

| Layer | Evidence |
|-------|----------|
| Domain and mapper | JVM tests for bounds, provider-neutral IDs, mapping, and fallback |
| Android manifest | Instrumented test requires zero permissions in this spike |
| Temporary storage | Instrumented JPEG copy, validation, resolution, and cleanup tests |
| Build | Android CI assembles app/test APKs, runs lint, and executes JVM tests |
| Supply chain | Gradle distribution checksum, dependency verification metadata, and locks |
| Acceptance | Physical phone and tablet capture/rotation/cancellation benchmark evidence |

The KMP suitability decision remains proposed until Android CI and required physical
device evidence pass. iOS/AVFoundation and future web adapters remain separate work.

See `docs/security/ANDROID_PERMISSION_POLICY.md` for the canonical capture, import,
export, upload/download, and notification permission rules.
