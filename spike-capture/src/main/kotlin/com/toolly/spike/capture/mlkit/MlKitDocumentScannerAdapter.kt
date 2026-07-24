package com.toolly.spike.capture.mlkit

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.toolly.spike.capture.domain.DocumentScanner
import com.toolly.spike.capture.domain.ScanConfig
import com.toolly.spike.capture.domain.ScanResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * [DocumentScanner] adapter backed by Google ML Kit Document Scanner.
 *
 * ## Lifecycle contract
 * The [launcher] must be registered via [androidx.activity.ComponentActivity.registerForActivityResult]
 * before the Activity reaches STARTED. Typical usage:
 *
 * ```kotlin
 * private val adapter = MlKitDocumentScannerAdapter(activity = this)
 * private val launcher = registerForActivityResult(StartIntentSenderForResult()) { result ->
 *     adapter.onActivityResult(result.resultCode, result.data)
 * }
 * // Pass launcher to adapter after construction:
 * // adapter.setLauncher(launcher)
 * ```
 *
 * ## Fallback selection
 * [MlKitDocumentScannerAdapter] is selected when Google Play Services are available and the
 * device meets the minimum ML Kit Document Scanner capability requirement. When either
 * condition is not met, [com.toolly.spike.capture.camerax.CameraXDocumentScannerAdapter]
 * is used instead.
 *
 * ## Privacy note
 * This adapter processes synthetic test-document images during the spike. No production
 * user documents, PII, tokens or credentials flow through this adapter. Temporary JPEG
 * files written by ML Kit are held only until [ScanResult] is delivered, then deleted
 * by the caller.
 */
class MlKitDocumentScannerAdapter(
    private val activity: Activity,
    private var launcher: ActivityResultLauncher<IntentSenderRequest>? = null,
) : DocumentScanner {

    private var pendingResult: CompletableDeferred<Pair<Int, List<String>>>? = null

    companion object {
        private const val TAG = "MlKitDocumentScannerAdapter"
    }

    /** Called by the hosting Activity when [ActivityResultLauncher] is ready. */
    fun setLauncher(launcher: ActivityResultLauncher<IntentSenderRequest>) {
        this.launcher = launcher
    }

    /**
     * Called by the hosting Activity's [ActivityResultLauncher] callback to deliver the
     * scan result. The [resultCode] and [intent] originate from the ML Kit scanner UI.
     *
     * No document pixels, paths or personal data are logged here.
     */
    fun onActivityResult(resultCode: Int, intent: android.content.Intent?) {
        val pageUris: List<String> = if (resultCode == Activity.RESULT_OK && intent != null) {
            GmsDocumentScanningResult.fromActivityResultIntent(intent)
                ?.pages
                ?.mapNotNull { it.imageUri?.toString() }
                .orEmpty()
        } else {
            emptyList()
        }
        pendingResult?.complete(Pair(resultCode, pageUris))
    }

    override suspend fun launch(config: ScanConfig): ScanResult {
        val activeLauncher = launcher
            ?: return MlKitResultMapper.mapServiceUnavailable()

        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(config.galleryImportEnabled)
            .setPageLimit(config.maxPages)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()

        val deferred = CompletableDeferred<Pair<Int, List<String>>>()
        pendingResult = deferred

        return try {
            val client = GmsDocumentScanning.getClient(options)
            val intentSenderResult = withContext(Dispatchers.IO) {
                com.google.android.gms.tasks.Tasks.await(
                    client.getStartScanIntent(activity)
                )
            }
            activeLauncher.launch(
                IntentSenderRequest.Builder(intentSenderResult.intentSender).build()
            )
            val (resultCode, pageUris) = deferred.await()
            MlKitResultMapper.map(resultCode, pageUris)
        } catch (e: Exception) {
            deferred.cancel()
            // Log the exception class name at debug level for diagnostics.
            // The exception message is not logged because it may contain device-specific
            // or Play Services initialization details that could include non-sensitive
            // but unpredictable content. Class name is sufficient to identify the failure
            // category (PlayServices, MlKitException, CancellationException, etc.).
            android.util.Log.d(TAG, "Capture initialization failed: ${e.javaClass.simpleName}")
            MlKitResultMapper.mapServiceUnavailable()
        } finally {
            pendingResult = null
        }
    }
}
