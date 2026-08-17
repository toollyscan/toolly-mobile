package com.toolly.spike.capture.ui

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.toolly.domain.model.AssetId
import com.toolly.domain.model.CapturedPageDraft
import com.toolly.domain.model.DocumentDetails
import com.toolly.domain.model.DocumentExportDelivery
import com.toolly.domain.model.DocumentExportFormat
import com.toolly.domain.model.DocumentExportOutcome
import com.toolly.domain.model.DocumentId
import com.toolly.domain.model.TemporaryAssetId as DomainTemporaryAssetId
import com.toolly.domain.usecases.ListDocumentsUseCase
import com.toolly.domain.usecases.OpenDocumentUseCase
import com.toolly.domain.usecases.RenameDocumentUseCase
import com.toolly.domain.usecases.SaveCapturedDocumentUseCase
import com.toolly.domain.usecases.TagDocumentUseCase
import com.toolly.foundation.OpaqueIdGenerator
import com.toolly.foundation.ToollyClock
import com.toolly.foundation.ToollyError
import com.toolly.foundation.ToollyErrorCode
import com.toolly.foundation.ToollyResult
import com.toolly.spike.capture.R
import com.toolly.spike.capture.camerax.CameraXDocumentScannerAdapter
import com.toolly.shared.capture.DocumentScanner
import com.toolly.shared.capture.FallbackDocumentScanner
import com.toolly.shared.capture.ScanConfig
import com.toolly.shared.capture.ScanError
import com.toolly.shared.capture.ScanResult
import com.toolly.shared.capture.ScannedPage
import com.toolly.shared.capture.TemporaryAssetId as CaptureTemporaryAssetId
import com.toolly.shared.edit.SignatureStrokes
import com.toolly.spike.capture.export.AndroidDocumentExporter
import com.toolly.spike.capture.export.AndroidShareIntentFactory
import com.toolly.spike.capture.export.ExportQuality
import com.toolly.spike.capture.mlkit.MlKitDocumentScannerAdapter
import com.toolly.spike.capture.mlkit.TemporaryScanStore
import com.toolly.spike.capture.pdfimport.PdfPageRasterizer
import com.toolly.spike.capture.vault.EncryptedDocumentRepository
import java.util.UUID
import java.util.concurrent.CancellationException
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

    private val pdfExportLauncher = registerForActivityResult(CreateDocument(PDF_MIME_TYPE)) { uri ->
        completePdfExport(uri)
    }

    private val jpegExportLauncher = registerForActivityResult(OpenDocumentTree()) { uri ->
        completeJpegExport(uri)
    }

    private val pdfImportLauncher = registerForActivityResult(OpenDocument()) { uri ->
        completePdfImport(uri)
    }

    private lateinit var scanner: DocumentScanner
    private lateinit var temporaryStore: TemporaryScanStore
    private lateinit var documentRepository: EncryptedDocumentRepository
    private lateinit var documentExporter: AndroidDocumentExporter
    private lateinit var pdfPageRasterizer: PdfPageRasterizer
    private lateinit var recentSearchesPreferences: SharedPreferences
    private var pendingExport: PendingExport? = null

    // Only one PDF import can be in flight (the picker is modal), matching how mlKitAdapter's own
    // single in-flight result callback works.
    private var pendingPdfImportResult: ((ScanResult) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        temporaryStore = TemporaryScanStore(applicationContext)
        pdfPageRasterizer = PdfPageRasterizer(applicationContext)
        documentRepository = EncryptedDocumentRepository(
            context = applicationContext,
            resolveTemporaryAsset = { rawId ->
                temporaryStore.resolve(CaptureTemporaryAssetId(rawId))
            },
        )
        documentExporter = AndroidDocumentExporter(
            loadBitmap = documentRepository::loadAssetBitmapForExport,
        )

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
        val renameDocument = RenameDocumentUseCase(
            repository = documentRepository,
            clock = ToollyClock(System::currentTimeMillis),
        )
        val tagDocument = TagDocumentUseCase(
            repository = documentRepository,
            clock = ToollyClock(System::currentTimeMillis),
        )
        recentSearchesPreferences = getSharedPreferences(RECENT_SEARCHES_PREFS_NAME, MODE_PRIVATE)
        val initialRecentSearches = recentSearchesPreferences
            .getString(RECENT_SEARCHES_KEY, null)
            ?.split(RECENT_SEARCHES_DELIMITER)
            ?.filter(String::isNotBlank)
            .orEmpty()

        setContent {
            var recentSearches by remember { mutableStateOf(initialRecentSearches) }
            MaterialTheme {
                AndroidToollyApp(
                    documentsContent = {
                        ToollyDocumentApp(
                            onLaunchCapture = { config, onResult ->
                                lifecycleScope.launch {
                                    onResult(scanner.launch(config))
                                }
                            },
                            onImportPdf = ::launchPdfImport,
                            onMergeDocuments = { documentIds, onResult ->
                                lifecycleScope.launch {
                                    onResult(mergeDocuments(documentIds, openDocument))
                                }
                            },
                            onSplitPages = { assetIds, onResult ->
                                lifecycleScope.launch {
                                    onResult(splitPages(assetIds))
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
                            onExportDocument = ::launchExport,
                            onRenameDocument = { documentId, name, onResult ->
                                lifecycleScope.launch {
                                    onResult(renameDocument(documentId, name))
                                }
                            },
                            onTagDocument = { documentId, category, onResult ->
                                lifecycleScope.launch {
                                    onResult(tagDocument(documentId, category))
                                }
                            },
                            onReplacePageAsset = { documentId, pageId, crop, mode, intensity, rotation, onResult ->
                                lifecycleScope.launch {
                                    onResult(
                                        documentRepository.replacePageWithEdit(
                                            documentId = documentId,
                                            pageId = pageId,
                                            crop = crop,
                                            mode = mode,
                                            intensity = intensity,
                                            updatedAtEpochMillis = System.currentTimeMillis(),
                                            rotationQuarterTurns = rotation,
                                        ),
                                    )
                                }
                            },
                            resolveTemporaryAsset = temporaryStore::resolve,
                            loadDocumentAssetBitmap = documentRepository::loadAssetBitmap,
                            onReleaseAssets = temporaryStore::release,
                        )
                    },
                    searchContent = {
                        SearchDocumentsScreen(
                            onLoadDocuments = { onResult ->
                                lifecycleScope.launch {
                                    onResult(listDocuments())
                                }
                            },
                            onOpenDocument = { documentId, onResult ->
                                lifecycleScope.launch {
                                    onResult(openDocument(documentId))
                                }
                            },
                            onExportDocument = ::launchExport,
                            onRenameDocument = { documentId, name, onResult ->
                                lifecycleScope.launch {
                                    onResult(renameDocument(documentId, name))
                                }
                            },
                            onTagDocument = { documentId, category, onResult ->
                                lifecycleScope.launch {
                                    onResult(tagDocument(documentId, category))
                                }
                            },
                            onReplacePageAsset = { documentId, pageId, crop, mode, intensity, rotation, onResult ->
                                lifecycleScope.launch {
                                    onResult(
                                        documentRepository.replacePageWithEdit(
                                            documentId = documentId,
                                            pageId = pageId,
                                            crop = crop,
                                            mode = mode,
                                            intensity = intensity,
                                            updatedAtEpochMillis = System.currentTimeMillis(),
                                            rotationQuarterTurns = rotation,
                                        ),
                                    )
                                }
                            },
                            loadDocumentAssetBitmap = documentRepository::loadAssetBitmap,
                            recentSearches = recentSearches,
                            onRecentSearchesChanged = { updated ->
                                recentSearches = updated
                                recentSearchesPreferences.edit()
                                    .putString(RECENT_SEARCHES_KEY, updated.joinToString(RECENT_SEARCHES_DELIMITER))
                                    .apply()
                            },
                        )
                    },
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

    private fun launchExport(
        document: DocumentDetails,
        format: DocumentExportFormat,
        quality: ExportQuality,
        watermarkText: String?,
        signature: SignatureStrokes,
        delivery: DocumentExportDelivery,
        onResult: (DocumentExportOutcome) -> Unit,
    ) {
        if (pendingExport != null) {
            onResult(DocumentExportOutcome.Failure(ToollyErrorCode.CONFLICT))
            return
        }
        pendingExport = PendingExport(document, quality, watermarkText, signature, delivery, onResult)
        when (format) {
            DocumentExportFormat.PDF -> {
                pdfExportLauncher.launch(getString(R.string.export_pdf_file_name))
            }
            DocumentExportFormat.JPEG -> jpegExportLauncher.launch(null)
        }
    }

    private fun completePdfExport(destination: Uri?) {
        val request = pendingExport ?: return
        pendingExport = null
        if (destination == null) {
            request.onResult(DocumentExportOutcome.Cancelled)
            return
        }
        lifecycleScope.launch {
            val result = try {
                val descriptor = contentResolver.openFileDescriptor(destination, WRITE_TRUNCATE_MODE)
                if (descriptor == null) {
                    retryableToollyFailure()
                } else {
                    ParcelFileDescriptor.AutoCloseOutputStream(descriptor).use { output ->
                        documentExporter.writePdf(
                            request.document,
                            output,
                            request.quality,
                            request.watermarkText,
                            request.signature,
                        )
                    }
                }
            } catch (cancelled: CancellationException) {
                deleteDocumentQuietly(destination)
                throw cancelled
            } catch (_: Exception) {
                retryableToollyFailure()
            }
            if (result is ToollyResult.Failure) deleteDocumentQuietly(destination)
            val outcome = result.toExportOutcome()
            request.onResult(
                if (
                    outcome == DocumentExportOutcome.Success &&
                    request.delivery == DocumentExportDelivery.SHARE
                ) {
                    shareDocuments(listOf(destination), PDF_MIME_TYPE)
                } else {
                    outcome
                },
            )
        }
    }

    private fun completeJpegExport(destinationTree: Uri?) {
        val request = pendingExport ?: return
        pendingExport = null
        if (destinationTree == null) {
            request.onResult(DocumentExportOutcome.Cancelled)
            return
        }
        lifecycleScope.launch {
            val exported = exportJpegPages(
                request.document,
                destinationTree,
                request.quality,
                request.watermarkText,
                request.signature,
            )
            request.onResult(
                if (
                    exported.outcome == DocumentExportOutcome.Success &&
                    request.delivery == DocumentExportDelivery.SHARE
                ) {
                    shareDocuments(exported.documentUris, JPEG_MIME_TYPE)
                } else {
                    exported.outcome
                },
            )
        }
    }

    /**
     * Lets a user import an existing PDF (not a Toolly-built scanner UI -- the system document
     * picker) instead of only capturing live. Once rasterized and staged, an imported PDF's pages
     * are indistinguishable from a live capture's: same [ScanResult]/[TemporaryAssetId]-based
     * pipeline, same review/crop/enhance/save screens.
     */
    private fun launchPdfImport(onResult: (ScanResult) -> Unit) {
        if (pendingPdfImportResult != null) {
            onResult(ScanResult.Failure(ScanError.Busy))
            return
        }
        pendingPdfImportResult = onResult
        pdfImportLauncher.launch(arrayOf(PDF_MIME_TYPE))
    }

    private fun completePdfImport(uri: Uri?) {
        val onResult = pendingPdfImportResult ?: return
        pendingPdfImportResult = null
        if (uri == null) {
            onResult(ScanResult.Cancelled)
            return
        }
        lifecycleScope.launch {
            val rasterized = pdfPageRasterizer.rasterize(uri, ScanConfig.MAX_PAGES)
            when (rasterized) {
                null, is PdfPageRasterizer.RasterizeResult.TooManyPages -> {
                    onResult(ScanResult.Failure(ScanError.InvalidResult))
                }
                is PdfPageRasterizer.RasterizeResult.Pages -> {
                    val outcome = temporaryStore.importBitmaps(rasterized.bitmaps)
                    rasterized.bitmaps.forEach { it.recycle() }
                    onResult(outcome.toScanResult())
                }
            }
        }
    }

    /**
     * Combines the current pages of [documentIds] (in that order) into one new document, by
     * decrypting each source page and re-staging it through the exact same
     * [TemporaryScanStore]/[ScanResult] pipeline a live capture or PDF import already uses --
     * landing on the same review screen (reorder/discard before saving), not a blind merge.
     * Documents that fail to open are skipped rather than aborting the whole merge; a document
     * whose pages fail to decrypt contributes nothing, which shows up as fewer pages in review.
     */
    private suspend fun mergeDocuments(
        documentIds: List<DocumentId>,
        openDocument: OpenDocumentUseCase,
    ): ScanResult {
        val bitmaps = mutableListOf<android.graphics.Bitmap>()
        for (documentId in documentIds) {
            val opened = openDocument(documentId)
            if (opened is ToollyResult.Success) {
                for (page in opened.value.pages.sortedBy { it.ordinal }) {
                    documentRepository.loadAssetBitmap(page.sourceAssetId)?.let { bitmaps += it }
                }
            }
        }
        if (bitmaps.isEmpty()) {
            return ScanResult.Failure(ScanError.InvalidResult)
        }
        val outcome = temporaryStore.importBitmaps(bitmaps)
        bitmaps.forEach { it.recycle() }
        return outcome.toScanResult()
    }

    /**
     * Extracts [assetIds] (already known to the caller -- the document detail screen already has
     * the full page list) into a new document via the same decrypt/re-stage pipeline
     * [mergeDocuments] uses. The source document is untouched: this is "copy these pages out",
     * not a destructive split, since there's no page-deletion capability in
     * `EncryptedDocumentRepository` yet to remove them from the original.
     */
    private suspend fun splitPages(assetIds: List<AssetId>): ScanResult {
        val bitmaps = assetIds.mapNotNull { documentRepository.loadAssetBitmap(it) }
        if (bitmaps.isEmpty()) {
            return ScanResult.Failure(ScanError.InvalidResult)
        }
        val outcome = temporaryStore.importBitmaps(bitmaps)
        bitmaps.forEach { it.recycle() }
        return outcome.toScanResult()
    }

    private fun TemporaryScanStore.ImportOutcome.toScanResult(): ScanResult = when (this) {
        is TemporaryScanStore.ImportOutcome.Success ->
            ScanResult.Success(assetIds.mapIndexed { index, assetId -> ScannedPage(index, assetId) })
        is TemporaryScanStore.ImportOutcome.Partial ->
            ScanResult.Success(assetIds.mapIndexed { index, assetId -> ScannedPage(index, assetId) })
        TemporaryScanStore.ImportOutcome.Failure -> ScanResult.Failure(ScanError.StorageFailure)
    }

    private suspend fun exportJpegPages(
        document: DocumentDetails,
        destinationTree: Uri,
        quality: ExportQuality,
        watermarkText: String?,
        signature: SignatureStrokes,
    ): JpegExportResult {
        val createdDocuments = mutableListOf<Uri>()
        val lastOrdinal = document.pages.maxOfOrNull { it.ordinal }
        return try {
            val parent = DocumentsContract.buildDocumentUriUsingTree(
                destinationTree,
                DocumentsContract.getTreeDocumentId(destinationTree),
            )
            for (page in document.pages.sortedBy { it.ordinal }) {
                val child = DocumentsContract.createDocument(
                    contentResolver,
                    parent,
                    JPEG_MIME_TYPE,
                    getString(
                        R.string.export_jpeg_page_file_name,
                        (page.ordinal + 1).toString(),
                    ),
                ) ?: return cleanupFailedJpegExport(
                    createdDocuments,
                    ToollyErrorCode.RETRYABLE,
                )
                createdDocuments += child
                val descriptor = contentResolver.openFileDescriptor(child, WRITE_TRUNCATE_MODE)
                    ?: return cleanupFailedJpegExport(
                        createdDocuments,
                        ToollyErrorCode.RETRYABLE,
                    )
                val result = ParcelFileDescriptor.AutoCloseOutputStream(descriptor).use { output ->
                    val pageSignature = if (page.ordinal == lastOrdinal) signature else emptyList()
                    documentExporter.writeJpeg(page, output, quality, watermarkText, pageSignature)
                }
                if (result is ToollyResult.Failure) {
                    return cleanupFailedJpegExport(createdDocuments, result.error.code)
                }
            }
            JpegExportResult(DocumentExportOutcome.Success, createdDocuments.toList())
        } catch (cancelled: CancellationException) {
            createdDocuments.asReversed().forEach(::deleteDocumentQuietly)
            throw cancelled
        } catch (_: Exception) {
            cleanupFailedJpegExport(createdDocuments, ToollyErrorCode.RETRYABLE)
        }
    }

    private fun cleanupFailedJpegExport(
        createdDocuments: List<Uri>,
        code: ToollyErrorCode,
    ): JpegExportResult {
        createdDocuments.asReversed().forEach(::deleteDocumentQuietly)
        return JpegExportResult(DocumentExportOutcome.Failure(code))
    }

    private fun shareDocuments(
        documentUris: List<Uri>,
        mimeType: String,
    ): DocumentExportOutcome = try {
        val shareIntent = AndroidShareIntentFactory.create(documentUris, mimeType)
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_document)))
        DocumentExportOutcome.Success
    } catch (_: Exception) {
        DocumentExportOutcome.Failure(ToollyErrorCode.UNAVAILABLE)
    }

    private fun deleteDocumentQuietly(uri: Uri) {
        runCatching { DocumentsContract.deleteDocument(contentResolver, uri) }
    }

    private fun retryableToollyFailure(): ToollyResult.Failure = ToollyResult.Failure(
        ToollyError(ToollyErrorCode.RETRYABLE, ToollyErrorCode.RETRYABLE.name),
    )

    private fun ToollyResult<Unit>.toExportOutcome(): DocumentExportOutcome = when (this) {
        is ToollyResult.Success -> DocumentExportOutcome.Success
        is ToollyResult.Failure -> DocumentExportOutcome.Failure(error.code)
    }

    private data class PendingExport(
        val document: DocumentDetails,
        val quality: ExportQuality,
        val watermarkText: String?,
        val signature: SignatureStrokes,
        val delivery: DocumentExportDelivery,
        val onResult: (DocumentExportOutcome) -> Unit,
    )

    private data class JpegExportResult(
        val outcome: DocumentExportOutcome,
        val documentUris: List<Uri> = emptyList(),
    )

    private companion object {
        const val PDF_MIME_TYPE = "application/pdf"
        const val JPEG_MIME_TYPE = "image/jpeg"
        const val WRITE_TRUNCATE_MODE = "rwt"
        const val RECENT_SEARCHES_PREFS_NAME = "toolly_search_preferences"
        const val RECENT_SEARCHES_KEY = "recent_searches"
        // A real single-line search query can never contain a newline (the field is
        // singleLine = true), so this is a safe, simple delimiter without needing a JSON dependency.
        const val RECENT_SEARCHES_DELIMITER = "\n"
    }
}
