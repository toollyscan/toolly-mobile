package com.toolly.spike.capture.camerax

import com.toolly.shared.capture.DocumentScanner
import com.toolly.shared.capture.ScanConfig
import com.toolly.shared.capture.ScanError
import com.toolly.shared.capture.ScanResult

/**
 * Stub [DocumentScanner] adapter representing the CameraX/manual-capture fallback path.
 *
 * ## Fallback selection — when this adapter is used instead of ML Kit
 *
 * The hosting composition root selects [CameraXDocumentScannerAdapter] when any of the
 * following conditions are detected at runtime:
 *
 * 1. **Play Services unavailable** — `GoogleApiAvailability.isGooglePlayServicesAvailable`
 *    does not return `ConnectionResult.SUCCESS`.
 * 2. **Device below ML Kit minimum capability** — the ML Kit initialization task reports
 *    the provider as unsupported. ML Kit documents a minimum total-RAM requirement of
 *    1.7 GB; Toolly does not invent a separate memory-class threshold.
 * 3. **Dynamic-feature delivery failed** — the on-demand ML Kit module download fails
 *    (network error, insufficient storage, or Play Store unavailable).
 * 4. **Manual-capture user preference** — a future settings flag may allow users to opt
 *    into a manual crop workflow regardless of ML Kit availability.
 *
 * ## Current state
 *
 * This is a **dependency-free stub implementation** for TLY-006B. Full CameraX camera lifecycle, permission
 * handling, rotation tracking and manual boundary-crop are not yet implemented. The stub
 * returns [ScanError.ServiceUnavailable] so callers can verify the fallback path is reached
 * without crashing.
 *
 * Full implementation is tracked in a follow-up issue linked from TLY-006B.
 */
class CameraXDocumentScannerAdapter : DocumentScanner {

    override suspend fun launch(config: ScanConfig): ScanResult =
        ScanResult.Failure(ScanError.ServiceUnavailable)
}
