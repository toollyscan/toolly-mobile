package com.toolly.spike.capture.mlkit

import com.toolly.spike.capture.domain.ScanError
import com.toolly.spike.capture.domain.ScanResult
import com.toolly.spike.capture.domain.ScannedPage

/**
 * Pure mapping from raw ML Kit activity-result values to Toolly [ScanResult].
 *
 * This object is intentionally free of Android and ML Kit imports so that it can be
 * exercised in plain JVM unit tests without Robolectric or an emulator.
 *
 * Android result codes are replicated as private constants to avoid the `android.app.Activity`
 * import and keep the mapping testable on the JVM.
 */
internal object MlKitResultMapper {

    // Matches Activity.RESULT_OK and Activity.RESULT_CANCELED without importing Activity.
    private const val RESULT_OK = -1
    private const val RESULT_CANCELED = 0

    /**
     * Map a raw activity result into a [ScanResult].
     *
     * An OK result with an empty page list is treated as implicit cancellation because the
     * ML Kit scanner can deliver RESULT_OK with no pages when the user navigates back from
     * the review screen without confirming.
     */
    fun map(resultCode: Int, pageUris: List<String>): ScanResult = when {
        resultCode == RESULT_CANCELED -> ScanResult.Cancelled
        resultCode == RESULT_OK && pageUris.isEmpty() -> ScanResult.Cancelled
        resultCode == RESULT_OK -> ScanResult.Success(
            pageUris.mapIndexed { index, uri ->
                ScannedPage(index = index, imageUri = uri)
            }
        )
        else -> ScanResult.Failure(
            ScanError.Unknown("Unexpected result code: $resultCode")
        )
    }

    /**
     * Map a Play Services / ML Kit initialisation failure to [ScanResult].
     *
     * This covers: device does not meet minimum capability requirements, Play Services
     * unavailable, dynamic-feature download failed, or first-use initialisation timeout.
     */
    fun mapServiceUnavailable(): ScanResult =
        ScanResult.Failure(ScanError.ServiceUnavailable)

    /**
     * Map a mid-session interruption where some pages were already captured.
     *
     * If [capturedUris] is empty the failure is reported as [ScanError.Unknown] because
     * there is no partial result to offer the user.
     */
    fun mapPartialCapture(capturedUris: List<String>, cause: String): ScanResult {
        val pages = capturedUris.mapIndexed { i, uri ->
            ScannedPage(index = i, imageUri = uri)
        }
        return if (pages.isEmpty()) {
            ScanResult.Failure(ScanError.Unknown(cause))
        } else {
            ScanResult.Failure(ScanError.PartialCapture(pages, cause))
        }
    }
}
