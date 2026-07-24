# TLY-006B — ML Kit Capture Spike: Architecture Note

- **Issue:** toollyscan/toolly-mobile#22
- **Status:** Spike executable code merged; physical-device evidence pending
- **Date:** 2026-07-24
- **Owner:** shivayogih

## Purpose

This note records the architecture of the TLY-006B capture spike: a minimal executable
Android module that proves the ML Kit Document Scanner capture boundary on phones and
tablets using synthetic test documents only.

## Module

The spike lives in `spike-capture/`. It is a self-contained Android application module
added for TLY-006B only. No production authentication, vault, sync or billing modules
are scaffolded.

## DocumentScanner port

```
com.toolly.spike.capture.domain.DocumentScanner
```

A Kotlin interface with a single `suspend fun launch(config: ScanConfig): ScanResult`.
No Android, ML Kit, or CameraX imports are present. The port is intentionally free of
provider SDK types so it can be moved to a shared KMP contracts module without changes.

## Adapter layer

```
domain.DocumentScanner
    ├── mlkit.MlKitDocumentScannerAdapter   (primary — requires Play Services)
    └── camerax.CameraXDocumentScannerAdapter (fallback — stub for spike)
```

### MlKitDocumentScannerAdapter

- Uses `GmsDocumentScanning` behind the `DocumentScanner` port.
- Bridges the Android `ActivityResultLauncher` callback to a Kotlin coroutine via
  `CompletableDeferred`.
- Delegates result mapping to `MlKitResultMapper` (pure object, no Android imports,
  fully unit-testable on the JVM).
- Returns `ScanResult.Failure(ScanError.ServiceUnavailable)` on any Play Services,
  initialization, or intent-sender failure.

### CameraXDocumentScannerAdapter (stub)

Returns `ScanResult.Failure(ScanError.ServiceUnavailable)` in all cases.

Selected over the ML Kit adapter when:

1. `GoogleApiAvailability.isGooglePlayServicesAvailable` ≠ `SUCCESS`
2. ML Kit initialization fails with `UNSUPPORTED`
3. On-demand ML Kit module download fails
4. Device memory class is below the ML Kit minimum
5. User has opted into a manual-capture preference (future)

Full CameraX implementation (permission handling, camera lifecycle, rotation, manual
boundary crop) is a follow-up tracked in the TLY-006B issue.

## Adapter selection

`CaptureSpikeActivity` checks Play Services at startup and wires the appropriate
adapter. Both adapters are injected through the `DocumentScanner` interface so the
Compose UI (`CaptureSpikeScreen`) is independent of the active implementation.

## Adaptive Compose harness

`CaptureSpikeScreen` reads `LocalConfiguration.current.screenWidthDp`:

| Width | Layout | Notes |
|-------|--------|-------|
| < 600 dp | Phone — stacked | Capture button above thumbnail grid |
| ≥ 600 dp | Tablet — side-by-side | Controls fixed-width left pane; thumbnails fill right |

`ThumbnailGrid` uses a `LazyVerticalGrid` with `GridCells.Adaptive(100.dp)` to fill
available width on both form factors.

Coil `AsyncImage` loads temporary JPEG URIs from app-private storage. Disk cache is
disabled for all vault-origin content in production (ADR-0011). The same policy is
applied in the spike for consistency.

## Result types

```
ScanResult
    Success(pages: List<ScannedPage>)
    Cancelled
    Failure(error: ScanError)
        ScanError.ServiceUnavailable
        ScanError.PermissionDenied
        ScanError.PartialCapture(capturedPages, cause)
        ScanError.Unknown(cause)
```

Cancellation is never converted to a generic failure. Partial results are preserved in
`PartialCapture` so the caller can offer to continue with captured pages.

## Unit tests

| Test class | Coverage |
|------------|----------|
| `MlKitResultMapperTest` | Success mapping, cancellation, empty-page implicit cancel, unknown code, ServiceUnavailable, PartialCapture, no-page partial |
| `DocumentScannerContractTest` | Cancelled, Success, ServiceUnavailable, PartialCapture, PermissionDenied, Unknown; config pass-through |
| `CameraXDocumentScannerAdapterTest` | Stub consistency across configurations and repeated calls |

All tests run on the JVM without Android, ML Kit or CameraX dependencies.

## Privacy controls

- No document pixels, OCR text, filenames, paths, PII, tokens or key material are
  logged at any level.
- `ScanError` cause strings contain only non-sensitive diagnostic context.
- Temporary JPEG files written by ML Kit to `cacheDir` are owned by the caller of
  `DocumentScanner.launch`; deletion is the caller's responsibility before the spike
  session ends.
- No `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE` or `MANAGE_EXTERNAL_STORAGE`
  permissions are requested.
- No network upload, no Firebase, no cloud sync.

## Benchmark evidence

Cold/warm launch, capture return time, memory observations and phone/tablet evidence
are pending physical-device execution. A run.json and measurements.jsonl must be
produced using the protocol template at `benchmarks/templates/run.template.json` and
stored in `benchmarks/evidence/camera-boundary/` before TLY-006B is accepted.

## Dependency scope

All 15 new dependencies are conditionally approved for TLY-006B (see
`config/dependencies/registry.json`). Transitive enumeration, verified artifact
checksums (`gradle/verification-metadata.xml`) and per-configuration lockfiles
(`spike-capture/gradle.lockfile`) are pending the first local build.

Candidate versions must be revalidated before any dependency is promoted to
`"approval_status": "approved"` for production use.

## ADR-0001 decision update

The KMP/native camera boundary is confirmed on the Android side: the domain port
(`DocumentScanner`) has no Android imports and is suitable for inclusion in a shared
KMP contracts module. Native Android handles Activity lifecycle, ActivityResult API and
SDK initialization; the shared contract carries only Toolly-owned result types.

iOS/AVFoundation boundary evaluation remains pending and is not blocked by this spike.
