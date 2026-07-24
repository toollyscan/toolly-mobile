package com.toolly.spike.capture.domain

/**
 * Structured errors from [DocumentScanner.launch].
 *
 * Error cases do not carry document pixels, filenames, paths, OCR text or PII.
 * Error values are an allowlist. Arbitrary SDK exception text must never cross this boundary.
 */
sealed class ScanError {
    /** The scanner service (e.g. ML Kit via Play Services) is not available on this device. */
    data object ServiceUnavailable : ScanError()

    /** The user denied or permanently revoked the CAMERA permission. */
    data object PermissionDenied : ScanError()

    /** Another capture request is already active. */
    data object Busy : ScanError()

    /** The provider returned a result that Toolly could not validate. */
    data object InvalidResult : ScanError()

    /** A validated source page could not be copied into Toolly-owned temporary storage. */
    data object StorageFailure : ScanError()

    /** The host lifecycle ended while capture was active. */
    data object LifecycleEnded : ScanError()

    /**
     * The session was interrupted after some pages were captured.
     * Callers may offer to continue with the partial set.
     */
    data class PartialCapture(
        val capturedPages: List<ScannedPage>,
        val reason: PartialCaptureReason,
    ) : ScanError()
}

/** Allowlisted reasons for a capture that produced only a validated subset of pages. */
enum class PartialCaptureReason {
    SOURCE_READ_FAILED,
    STORAGE_WRITE_FAILED,
    SESSION_INTERRUPTED,
}
