package com.toolly.spike.capture.mlkit

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.toolly.spike.capture.domain.DocumentScanner
import com.toolly.spike.capture.domain.ScanConfig
import com.toolly.spike.capture.domain.ScanError
import com.toolly.spike.capture.domain.ScanResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/** Android adapter for Google ML Kit Document Scanner. */
class MlKitDocumentScannerAdapter(
    private val activity: Activity,
    private val temporaryStore: TemporaryScanStore,
    private var launcher: ActivityResultLauncher<IntentSenderRequest>? = null,
) : DocumentScanner, AutoCloseable {

    private val active = AtomicBoolean(false)
    private val closed = AtomicBoolean(false)
    private val pendingResult = AtomicReference<CompletableDeferred<RawScanResult>?>(null)

    fun setLauncher(launcher: ActivityResultLauncher<IntentSenderRequest>) {
        check(!closed.get()) { "Adapter is closed" }
        this.launcher = launcher
    }

    /** Activity-result callback. Provider URIs remain inside the Android adapter. */
    fun onActivityResult(resultCode: Int, data: Intent?) {
        val pageUris = if (resultCode == Activity.RESULT_OK && data != null) {
            GmsDocumentScanningResult.fromActivityResultIntent(data)
                ?.pages
                ?.mapNotNull { it.imageUri }
                .orEmpty()
        } else {
            emptyList()
        }
        pendingResult.getAndSet(null)?.complete(RawScanResult(resultCode, pageUris))
    }

    override suspend fun launch(config: ScanConfig): ScanResult {
        if (closed.get()) return ScanResult.Failure(ScanError.LifecycleEnded)
        if (!active.compareAndSet(false, true)) {
            return ScanResult.Failure(ScanError.Busy)
        }

        val activeLauncher = launcher
        if (activeLauncher == null) {
            active.set(false)
            return MlKitResultMapper.mapServiceUnavailable()
        }

        val deferred = CompletableDeferred<RawScanResult>()
        if (!pendingResult.compareAndSet(null, deferred)) {
            active.set(false)
            return ScanResult.Failure(ScanError.Busy)
        }

        return try {
            val options = GmsDocumentScannerOptions.Builder()
                .setGalleryImportAllowed(config.galleryImportEnabled)
                .setPageLimit(config.maxPages)
                .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
                .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
                .build()
            val client = GmsDocumentScanning.getClient(options)
            val intentSender = withContext(Dispatchers.IO) {
                Tasks.await(client.getStartScanIntent(activity))
            }
            activeLauncher.launch(IntentSenderRequest.Builder(intentSender).build())

            val rawResult = deferred.await()
            if (rawResult.resultCode != Activity.RESULT_OK || rawResult.pageUris.isEmpty()) {
                MlKitResultMapper.map(rawResult.resultCode, emptyList())
            } else {
                withContext(Dispatchers.IO) {
                    when (val imported = temporaryStore.importPages(rawResult.pageUris)) {
                        is TemporaryScanStore.ImportOutcome.Success ->
                            MlKitResultMapper.map(rawResult.resultCode, imported.assetIds)
                        is TemporaryScanStore.ImportOutcome.Partial ->
                            MlKitResultMapper.mapPartialCapture(
                                imported.assetIds,
                                imported.reason,
                            )
                        TemporaryScanStore.ImportOutcome.Failure ->
                            ScanResult.Failure(ScanError.StorageFailure)
                    }
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            MlKitResultMapper.mapServiceUnavailable()
        } finally {
            pendingResult.compareAndSet(deferred, null)
            active.set(false)
        }
    }

    override fun close() {
        if (closed.compareAndSet(false, true)) {
            pendingResult.getAndSet(null)?.cancel(
                CancellationException("Capture host lifecycle ended"),
            )
            active.set(false)
            launcher = null
        }
    }

    private data class RawScanResult(
        val resultCode: Int,
        val pageUris: List<Uri>,
    )
}
