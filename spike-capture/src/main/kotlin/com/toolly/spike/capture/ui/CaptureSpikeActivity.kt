package com.toolly.spike.capture.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.toolly.domain.model.CapturedPageDraft
import com.toolly.domain.model.TemporaryAssetId as DomainTemporaryAssetId
import com.toolly.domain.usecases.ListDocumentsUseCase
import com.toolly.domain.usecases.OpenDocumentUseCase
import com.toolly.domain.usecases.SaveCapturedDocumentUseCase
import com.toolly.foundation.OpaqueIdGenerator
import com.toolly.foundation.ToollyClock
import com.toolly.spike.capture.camerax.CameraXDocumentScannerAdapter
import com.toolly.spike.capture.domain.DocumentScanner
import com.toolly.spike.capture.domain.FallbackDocumentScanner
import com.toolly.spike.capture.domain.TemporaryAssetId as CaptureTemporaryAssetId
import com.toolly.spike.capture.mlkit.MlKitDocumentScannerAdapter
import com.toolly.spike.capture.mlkit.TemporaryScanStore
import com.toolly.spike.capture.vault.AppPrivateDocumentRepository
import java.util.UUID
import kotlinx.coroutines.launch

/**
 * Android composition root for the TLY-011 capture-to-library walking slice.
 *
 * No document pixels, paths, OCR text, filenames or PII are logged at any level.
 */
class CaptureSpikeActivity : ComponentActivity() {

    private var mlKitAdapter: MlKitDocumentScannerAdapter? = null

    private val scanLauncher = registerForActivityResult(StartIntentSenderForResult()) { result ->
        mlKitAdapter?.onActivityResult(result.resultCode, result.data)
    }

    private lateinit var scanner: DocumentScanner
    private lateinit var temporaryStore: TemporaryScanStore
    private lateinit var documentRepository: AppPrivateDocumentRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        temporaryStore = TemporaryScanStore(applicationContext)
        documentRepository = AppPrivateDocumentRepository(applicationContext) { rawId ->
            temporaryStore.resolve(CaptureTemporaryAssetId(rawId))
        }

        scanner = if (isPlayServicesAvailable()) {
            val primary = MlKitDocumentScannerAdapter(
                activity = this,
                temporaryStore = temporaryStore,
            ).also { adapter ->
                adapter.setLauncher(scanLauncher)
                mlKitAdapter = adapter
            }
            FallbackDocumentScanner(
                primary = primary,
                fallback = CameraXDocumentScannerAdapter(),
            )
        } else {
            CameraXDocumentScannerAdapter()
        }

        val saveDocument = SaveCapturedDocumentUseCase(
            repository = documentRepository,
            clock = ToollyClock(System::currentTimeMillis),
            idGenerator = OpaqueIdGenerator {
                UUID.randomUUID().toString().lowercase()
            },
        )
        val listDocuments = ListDocumentsUseCase(documentRepository)
        val openDocument = OpenDocumentUseCase(documentRepository)

        setContent {
            MaterialTheme {
                ToollyDocumentApp(
                    onLaunchCapture = { config, onResult ->
                        lifecycleScope.launch {
                            onResult(scanner.launch(config))
                        }
                    },
                    onLoadDocuments = { onResult ->
                        lifecycleScope.launch {
                            onResult(listDocuments())
                        }
                    },
                    onSavePages = { pages, onResult ->
                        lifecycleScope.launch {
                            val drafts = pages.sortedBy { it.index }.mapIndexed { index, page ->
                                CapturedPageDraft(
                                    temporaryAssetId = DomainTemporaryAssetId(page.assetId.value),
                                    ordinal = index,
                                    widthPixels = null,
                                    heightPixels = null,
                                )
                            }
                            onResult(saveDocument(drafts))
                        }
                    },
                    onOpenDocument = { documentId, onResult ->
                        lifecycleScope.launch {
                            onResult(openDocument(documentId))
                        }
                    },
                    resolveTemporaryAsset = temporaryStore::resolve,
                    resolveDocumentAsset = documentRepository::resolveAsset,
                    onReleaseAssets = temporaryStore::release,
                )
            }
        }
    }

    override fun onDestroy() {
        mlKitAdapter?.close()
        if (::temporaryStore.isInitialized) temporaryStore.close()
        super.onDestroy()
    }

    private fun isPlayServicesAvailable(): Boolean =
        GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS
}
