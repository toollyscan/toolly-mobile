package com.toolly.spike.capture.mlkit

import com.toolly.shared.capture.PartialCaptureReason
import com.toolly.shared.capture.ScanError
import com.toolly.shared.capture.ScanResult
import com.toolly.shared.capture.ScannedPage
import com.toolly.shared.capture.TemporaryAssetId

/** Pure mapping from validated Toolly temporary assets to the domain result contract. */
internal object MlKitResultMapper {

    private const val RESULT_OK = -1
    private const val RESULT_CANCELED = 0

    fun map(resultCode: Int, assetIds: List<TemporaryAssetId>): ScanResult = when {
        resultCode == RESULT_CANCELED -> ScanResult.Cancelled
        resultCode == RESULT_OK && assetIds.isEmpty() -> ScanResult.Cancelled
        resultCode == RESULT_OK -> ScanResult.Success(
            assetIds.mapIndexed { index, assetId ->
                ScannedPage(index = index, assetId = assetId)
            },
        )
        else -> ScanResult.Failure(ScanError.InvalidResult)
    }

    fun mapServiceUnavailable(): ScanResult =
        ScanResult.Failure(ScanError.ServiceUnavailable)

    fun mapPartialCapture(
        assetIds: List<TemporaryAssetId>,
        reason: PartialCaptureReason,
    ): ScanResult {
        val pages = assetIds.mapIndexed { index, assetId ->
            ScannedPage(index = index, assetId = assetId)
        }
        return if (pages.isEmpty()) {
            ScanResult.Failure(ScanError.StorageFailure)
        } else {
            ScanResult.Failure(ScanError.PartialCapture(pages, reason))
        }
    }
}
