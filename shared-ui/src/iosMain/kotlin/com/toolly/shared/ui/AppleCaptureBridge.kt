package com.toolly.shared.ui

import com.toolly.shared.capture.DocumentScanner
import com.toolly.shared.capture.ScanConfig
import com.toolly.shared.capture.ScanError
import com.toolly.shared.capture.ScanResult
import com.toolly.shared.capture.ScannedPage
import com.toolly.shared.capture.TemporaryAssetId
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Objective-C-compatible boundary implemented by the first-party Swift host.
 *
 * The host owns VisionKit, UIKit and private temporary files. Only opaque Toolly asset identifiers
 * cross this boundary.
 */
interface AppleCaptureSession {
    fun launch(maxPages: Int, callback: AppleCaptureCallback)

    fun release(temporaryAssetIds: List<String>)
}

/** Terminal callbacks from one Apple capture presentation. Exactly one callback must be delivered. */
interface AppleCaptureCallback {
    fun onSuccess(temporaryAssetIds: List<String>)
    fun onCancelled()
    fun onServiceUnavailable()
    fun onPermissionDenied()
    fun onBusy()
    fun onInvalidResult()
    fun onStorageFailure()
    fun onLifecycleEnded()
}

/** Provider-neutral scanner adapter around the Swift-owned Apple capture session. */
internal class AppleDocumentScanner(
    private val session: AppleCaptureSession,
) : DocumentScanner {
    private var active = false

    override suspend fun launch(config: ScanConfig): ScanResult {
        if (active) return ScanResult.Failure(ScanError.Busy)

        active = true
        return suspendCoroutine { continuation ->
            var completed = false

            fun finish(result: ScanResult) {
                if (completed) return
                completed = true
                active = false
                continuation.resume(result)
            }

            val callback = object : AppleCaptureCallback {
                override fun onSuccess(temporaryAssetIds: List<String>) {
                    val pages = temporaryAssetIds.toValidatedPages(config.maxPages)
                    if (pages == null) {
                        session.release(temporaryAssetIds)
                        finish(ScanResult.Failure(ScanError.InvalidResult))
                    } else {
                        finish(ScanResult.Success(pages))
                    }
                }

                override fun onCancelled() = finish(ScanResult.Cancelled)

                override fun onServiceUnavailable() =
                    finish(ScanResult.Failure(ScanError.ServiceUnavailable))

                override fun onPermissionDenied() =
                    finish(ScanResult.Failure(ScanError.PermissionDenied))

                override fun onBusy() = finish(ScanResult.Failure(ScanError.Busy))

                override fun onInvalidResult() =
                    finish(ScanResult.Failure(ScanError.InvalidResult))

                override fun onStorageFailure() =
                    finish(ScanResult.Failure(ScanError.StorageFailure))

                override fun onLifecycleEnded() =
                    finish(ScanResult.Failure(ScanError.LifecycleEnded))
            }

            try {
                session.launch(config.maxPages, callback)
            } catch (_: Throwable) {
                finish(ScanResult.Failure(ScanError.LifecycleEnded))
            }
        }
    }

    fun release(pages: List<ScannedPage>) {
        session.release(pages.map { it.assetId.value })
    }
}

private fun List<String>.toValidatedPages(maxPages: Int): List<ScannedPage>? {
    if (isEmpty() || size > maxPages || size != distinct().size) return null

    return try {
        mapIndexed { index, value ->
            ScannedPage(
                index = index,
                assetId = TemporaryAssetId(value),
            )
        }
    } catch (_: IllegalArgumentException) {
        null
    }
}
