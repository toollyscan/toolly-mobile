package com.toolly.spike.capture.domain

/**
 * Structured errors from [DocumentScanner.launch].
 *
 * Error cases do not carry document pixels, filenames, paths, OCR text or PII.
 * Human-readable cause strings contain only non-sensitive diagnostic context.
 */
sealed class ScanError {
    /** The scanner service (e.g. ML Kit via Play Services) is not available on this device. */
    data object ServiceUnavailable : ScanError()

    /** The user denied or permanently revoked the CAMERA permission. */
    data object PermissionDenied : ScanError()

    /**
     * The session was interrupted after some pages were captured.
     * Callers may offer to continue with the partial set.
     */
    data class PartialCapture(
        val capturedPages: List<ScannedPage>,
        val cause: String,
    ) : ScanError()

    /** An unexpected error occurred. The [cause] string is safe to log at debug level. */
    data class Unknown(val cause: String) : ScanError()
}
