package com.toolly.spike.capture.ui

import android.graphics.Bitmap

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.toolly.domain.model.AssetId
import com.toolly.domain.model.DocumentDetails
import com.toolly.domain.model.DocumentExportDelivery
import com.toolly.domain.model.DocumentExportFormat
import com.toolly.domain.model.DocumentExportOutcome
import com.toolly.domain.model.DocumentId
import com.toolly.domain.model.DocumentSummary
import com.toolly.foundation.ToollyErrorCode
import com.toolly.foundation.ToollyResult
import com.toolly.spike.capture.R
import com.toolly.shared.capture.ScanConfig
import com.toolly.shared.capture.ScanError
import com.toolly.shared.capture.ScanResult
import com.toolly.shared.capture.ScannedPage
import com.toolly.shared.capture.TemporaryAssetId
import com.toolly.shared.ui.ExportBuilderScreen
import com.toolly.shared.ui.ExportPrivacyCheckScreen
import com.toolly.shared.ui.ToollyExportFormat
import java.io.File

/**
 * First production-shaped Toolly Android walking slice.
 *
 * The UI depends on Toolly models and callback boundaries. It has no filesystem, database,
 * Firebase, ML Kit or provider types in its state.
 */
@Composable
fun ToollyDocumentApp(
    onLaunchCapture: (ScanConfig, onResult: (ScanResult) -> Unit) -> Unit,
    onLoadDocuments: (onResult: (ToollyResult<List<DocumentSummary>>) -> Unit) -> Unit,
    onSavePages: (List<ScannedPage>, onResult: (ToollyResult<DocumentDetails>) -> Unit) -> Unit,
    onOpenDocument: (DocumentId, onResult: (ToollyResult<DocumentDetails>) -> Unit) -> Unit,
    onExportDocument: (
        DocumentDetails,
        DocumentExportFormat,
        DocumentExportDelivery,
        onResult: (DocumentExportOutcome) -> Unit,
    ) -> Unit,
    resolveTemporaryAsset: (TemporaryAssetId) -> File?,
    loadDocumentAssetBitmap: suspend (AssetId) -> Bitmap?,
    onReleaseAssets: (Collection<TemporaryAssetId>) -> Unit,
) {
    var screen by remember { mutableStateOf<AppScreen>(AppScreen.Library) }
    var documents by remember { mutableStateOf<List<DocumentSummary>>(emptyList()) }
    var isWorking by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<UiMessage?>(null) }
    val captureLaunchRequest = LocalCaptureLaunchRequest.current

    fun refreshLibrary() {
        isWorking = true
        onLoadDocuments { result ->
            isWorking = false
            when (result) {
                is ToollyResult.Success -> {
                    documents = result.value
                    message = null
                }
                is ToollyResult.Failure -> message = UiMessage(toollyErrorMessage(result.error.code))
            }
        }
    }

    fun launchCapture() {
        if (!isWorking && screen == AppScreen.Library) {
            isWorking = true
            message = null
            onLaunchCapture(ScanConfig()) { result ->
                isWorking = false
                when (result) {
                    is ScanResult.Success -> {
                        screen = AppScreen.CapturePreview(result.pages)
                    }
                    ScanResult.Cancelled -> {
                        message = UiMessage(R.string.capture_cancelled)
                    }
                    is ScanResult.Failure -> {
                        val error = result.error
                        message = UiMessage(captureErrorMessage(error))
                        if (error is ScanError.PartialCapture) {
                            screen = AppScreen.CapturePreview(error.capturedPages)
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshLibrary()
    }
    LaunchedEffect(captureLaunchRequest.requested, isWorking, screen) {
        if (
            captureLaunchRequest.requested &&
            !isWorking &&
            screen == AppScreen.Library
        ) {
            captureLaunchRequest.consume()
            launchCapture()
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        when (val current = screen) {
            AppScreen.Library -> LibraryScreen(
                documents = documents,
                isWorking = isWorking,
                message = message,
                onScan = ::launchCapture,
                onOpen = { documentId ->
                    isWorking = true
                    onOpenDocument(documentId) { result ->
                        isWorking = false
                        when (result) {
                            is ToollyResult.Success -> screen = AppScreen.Document(result.value)
                            is ToollyResult.Failure -> {
                                message = UiMessage(toollyErrorMessage(result.error.code))
                            }
                        }
                    }
                },
            )

            is AppScreen.CapturePreview -> CapturePreviewScreen(
                pages = current.pages,
                isSaving = isWorking,
                message = message,
                resolveAsset = resolveTemporaryAsset,
                onDiscard = {
                    onReleaseAssets(current.pages.map { it.assetId })
                    message = null
                    screen = AppScreen.Library
                },
                onSave = {
                    isWorking = true
                    message = null
                    onSavePages(current.pages) { result ->
                        isWorking = false
                        when (result) {
                            is ToollyResult.Success -> {
                                onReleaseAssets(current.pages.map { it.assetId })
                                message = UiMessage(R.string.document_saved)
                                screen = AppScreen.Library
                                refreshLibrary()
                            }
                            is ToollyResult.Failure -> {
                                message = UiMessage(toollyErrorMessage(result.error.code))
                            }
                        }
                    }
                },
            )

            is AppScreen.Document -> DocumentScreen(
                document = current.details,
                message = message,
                loadAssetBitmap = loadDocumentAssetBitmap,
                onStartExport = {
                    message = null
                    screen = AppScreen.ExportBuilder(current.details)
                },
                onBack = {
                    message = null
                    screen = AppScreen.Library
                },
            )

            is AppScreen.ExportBuilder -> {
                var format by remember(current.details) { mutableStateOf(ToollyExportFormat.PDF) }
                ExportBuilderScreen(
                    format = format,
                    onFormatChange = { format = it },
                    onContinue = {
                        screen = AppScreen.ExportPrivacyCheck(current.details, format)
                    },
                    onBack = {
                        message = null
                        screen = AppScreen.Document(current.details)
                    },
                    previewContent = {
                        DocumentPageGrid(
                            pages = current.details.pages,
                            loadAssetBitmap = loadDocumentAssetBitmap,
                            modifier = Modifier.heightIn(max = 240.dp),
                        )
                    },
                )
            }

            is AppScreen.ExportPrivacyCheck -> {
                val domainFormat = current.format.toDomainFormat()
                ExportPrivacyCheckScreen(
                    busy = isWorking,
                    onSaveToDevice = {
                        if (!isWorking) {
                            isWorking = true
                            message = null
                            onExportDocument(current.details, domainFormat, DocumentExportDelivery.SAVE) { outcome ->
                                isWorking = false
                                message = UiMessage(exportOutcomeMessage(outcome))
                                if (outcome is DocumentExportOutcome.Success) {
                                    screen = AppScreen.Document(current.details)
                                }
                            }
                        }
                    },
                    onShare = {
                        if (!isWorking) {
                            isWorking = true
                            message = null
                            onExportDocument(current.details, domainFormat, DocumentExportDelivery.SHARE) { outcome ->
                                isWorking = false
                                message = UiMessage(exportOutcomeMessage(outcome))
                                if (outcome is DocumentExportOutcome.Success) {
                                    screen = AppScreen.Document(current.details)
                                }
                            }
                        }
                    },
                    onBack = {
                        screen = AppScreen.ExportBuilder(current.details)
                    },
                )
            }
        }
    }
}

private fun ToollyExportFormat.toDomainFormat(): DocumentExportFormat = when (this) {
    ToollyExportFormat.PDF -> DocumentExportFormat.PDF
    ToollyExportFormat.JPEG -> DocumentExportFormat.JPEG
}

@Composable
private fun LibraryScreen(
    documents: List<DocumentSummary>,
    isWorking: Boolean,
    message: UiMessage?,
    onScan: () -> Unit,
    onOpen: (DocumentId) -> Unit,
) {
    val expanded = LocalConfiguration.current.screenWidthDp >= 600
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = if (expanded) 920.dp else 640.dp)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium)
            Text(
                stringResource(R.string.library_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = onScan,
                enabled = !isWorking,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isWorking) {
                    val workingDescription = stringResource(R.string.working)
                    CircularProgressIndicator(
                        modifier = Modifier.semantics {
                            contentDescription = workingDescription
                        },
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(stringResource(R.string.scan_document))
                }
            }
            StatusMessage(message)
            if (documents.isEmpty() && !isWorking) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.no_documents),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(documents, key = { it.id.value }) { document ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpen(document.id) },
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    stringResource(R.string.scanned_document),
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    pluralStringResource(
                                        R.plurals.page_count,
                                        document.pageCount,
                                        document.pageCount,
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CapturePreviewScreen(
    pages: List<ScannedPage>,
    isSaving: Boolean,
    message: UiMessage?,
    resolveAsset: (TemporaryAssetId) -> File?,
    onDiscard: () -> Unit,
    onSave: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.review_scan), style = MaterialTheme.typography.headlineSmall)
        Text(
            pluralStringResource(R.plurals.page_count, pages.size, pages.size),
            style = MaterialTheme.typography.bodyMedium,
        )
        ThumbnailGrid(
            pages = pages,
            resolveAsset = resolveAsset,
            modifier = Modifier.weight(1f),
        )
        StatusMessage(message)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onDiscard,
                enabled = !isSaving,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.discard))
            }
            Button(
                onClick = onSave,
                enabled = !isSaving,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(if (isSaving) R.string.saving else R.string.save))
            }
        }
    }
}

@Composable
private fun DocumentScreen(
    document: DocumentDetails,
    message: UiMessage?,
    loadAssetBitmap: suspend (AssetId) -> Bitmap?,
    onStartExport: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(onClick = onBack) {
                Text(stringResource(R.string.back))
            }
            Text(
                stringResource(R.string.scanned_document),
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        Text(
            pluralStringResource(
                R.plurals.page_count,
                document.summary.pageCount,
                document.summary.pageCount,
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
        DocumentPageGrid(
            pages = document.pages,
            loadAssetBitmap = loadAssetBitmap,
            modifier = Modifier.weight(1f),
        )
        StatusMessage(message)
        Button(
            onClick = onStartExport,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.export_document))
        }
    }
}

@Composable
private fun StatusMessage(message: UiMessage?) {
    message?.let {
        Text(
            text = stringResource(it.resourceId),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.semantics {
                liveRegion = LiveRegionMode.Polite
            },
        )
    }
}

@StringRes
private fun exportOutcomeMessage(outcome: DocumentExportOutcome): Int = when (outcome) {
    DocumentExportOutcome.Success -> R.string.document_exported
    DocumentExportOutcome.Cancelled -> R.string.export_cancelled
    is DocumentExportOutcome.Failure -> toollyErrorMessage(outcome.code)
}

@StringRes
private fun captureErrorMessage(error: ScanError): Int = when (error) {
    is ScanError.ServiceUnavailable -> R.string.scanner_unavailable
    is ScanError.PermissionDenied -> R.string.camera_permission_required
    is ScanError.PartialCapture -> R.string.partial_capture_available
    is ScanError.Busy -> R.string.capture_busy
    is ScanError.InvalidResult -> R.string.invalid_scanner_result
    is ScanError.StorageFailure -> R.string.captured_pages_storage_failed
    is ScanError.LifecycleEnded -> R.string.capture_screen_closed
    else -> R.string.capture_failed
}

@StringRes
private fun toollyErrorMessage(code: ToollyErrorCode): Int = when (code) {
    ToollyErrorCode.VALIDATION -> R.string.validation_failed
    ToollyErrorCode.UNAVAILABLE -> R.string.content_unavailable
    ToollyErrorCode.UNAUTHORIZED -> R.string.authorization_required
    ToollyErrorCode.CONFLICT -> R.string.operation_conflict
    ToollyErrorCode.QUOTA -> R.string.quota_reached
    ToollyErrorCode.CORRUPT -> R.string.document_corrupt
    ToollyErrorCode.RETRYABLE -> R.string.operation_retryable
    ToollyErrorCode.PERMANENT,
    ToollyErrorCode.UNKNOWN -> R.string.operation_failed
}

private data class UiMessage(
    @StringRes val resourceId: Int,
)

private sealed interface AppScreen {
    data object Library : AppScreen
    data class CapturePreview(val pages: List<ScannedPage>) : AppScreen
    data class Document(val details: DocumentDetails) : AppScreen
    data class ExportBuilder(val details: DocumentDetails) : AppScreen
    data class ExportPrivacyCheck(val details: DocumentDetails, val format: ToollyExportFormat) : AppScreen
}
