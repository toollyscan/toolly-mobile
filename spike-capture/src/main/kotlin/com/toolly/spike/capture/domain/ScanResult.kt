package com.toolly.spike.capture.domain

/**
 * Outcome of a [DocumentScanner.launch] call.
 *
 * All variants are terminal: the session is complete when a result is delivered.
 * Callers must handle every branch; ignoring [Failure] or [Cancelled] silently is
 * a contract violation.
 */
sealed class ScanResult {
    /** One or more pages were captured successfully. */
    data class Success(val pages: List<ScannedPage>) : ScanResult()

    /** The user explicitly cancelled the capture session. No pages are returned. */
    data object Cancelled : ScanResult()

    /** The session failed. See [ScanError] for structured error categories. */
    data class Failure(val error: ScanError) : ScanResult()
}
