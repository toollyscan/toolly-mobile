package com.toolly.spike.capture.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.toolly.domain.model.AssetId
import com.toolly.domain.model.DocumentDetails
import com.toolly.domain.model.DocumentId
import com.toolly.domain.model.DocumentSummary
import com.toolly.foundation.ToollyResult
import com.toolly.spike.capture.domain.ScanConfig
import com.toolly.spike.capture.domain.ScanError
import com.toolly.spike.capture.domain.ScanResult
import com.toolly.spike.capture.domain.ScannedPage
import com.toolly.spike.capture.domain.TemporaryAssetId
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
    resolveTemporaryAsset: (TemporaryAssetId) -> File?,
    resolveDocumentAsset: (AssetId) -> File?,
    onReleaseAssets: (Collection<TemporaryAssetId>) -> Unit,
) {
    var screen by remember { mutableStateOf<AppScreen>(AppScreen.Library) }
    var documents by remember { mutableStateOf<List<DocumentSummary>>(emptyList()) }
    var isWorking by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    fun refreshLibrary() {
        isWorking = true
        onLoadDocuments { result ->
            isWorking = false
            when (result) {
                is ToollyResult.Success -> {
                    documents = result.value
                    message = null
                }
                is ToollyResult.Failure -> message = result.error.safeMessage
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshLibrary()
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        when (val current = screen) {
            AppScreen.Library -> LibraryScreen(
                documents = documents,
                isWorking = isWorking,
                message = message,
                onScan = {
                    if (!isWorking) {
                        isWorking = true
                        message = null
                        onLaunchCapture(ScanConfig()) { result ->
                            isWorking = false
                            when (result) {
                                is ScanResult.Success -> {
                                    screen = AppScreen.CapturePreview(result.pages)
                                }
                                ScanResult.Cancelled -> message = "Capture cancelled"
                                is ScanResult.Failure -> {
                                    message = safeCaptureMessage(result.error)
                                    if (result.error is ScanError.PartialCapture) {
                                        screen = AppScreen.CapturePreview(
                                            result.error.capturedPages,
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                onOpen = { documentId ->
                    isWorking = true
                    onOpenDocument(documentId) { result ->
                        isWorking = false
                        when (result) {
                            is ToollyResult.Success -> screen = AppScreen.Document(result.value)
                            is ToollyResult.Failure -> message = result.error.safeMessage
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
                                message = "Document saved"
                                screen = AppScreen.Library
                                refreshLibrary()
                            }
                            is ToollyResult.Failure -> message = result.error.safeMessage
                        }
                    }
                },
            )

            is AppScreen.Document -> DocumentScreen(
                document = current.details,
                resolveAsset = resolveDocumentAsset,
                onBack = {
                    message = null
                    screen = AppScreen.Library
                },
            )
        }
    }
}

@Composable
private fun LibraryScreen(
    documents: List<DocumentSummary>,
    isWorking: Boolean,
    message: String?,
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
            Text("Toolly", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Your documents stay available offline.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = onScan,
                enabled = !isWorking,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isWorking) {
                    CircularProgressIndicator(
                        modifier = Modifier.semantics {
                            contentDescription = "Working"
                        },
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Scan document")
                }
            }
            StatusMessage(message)
            if (documents.isEmpty() && !isWorking) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No documents yet",
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
                                    "Scanned document",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    "${document.pageCount} page(s)",
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
    message: String?,
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
        Text("Review scan", style = MaterialTheme.typography.headlineSmall)
        Text("${pages.size} page(s)", style = MaterialTheme.typography.bodyMedium)
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
                Text("Discard")
            }
            Button(
                onClick = onSave,
                enabled = !isSaving,
                modifier = Modifier.weight(1f),
            ) {
                Text(if (isSaving) "Saving…" else "Save")
            }
        }
    }
}

@Composable
private fun DocumentScreen(
    document: DocumentDetails,
    resolveAsset: (AssetId) -> File?,
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
                Text("Back")
            }
            Text("Scanned document", style = MaterialTheme.typography.headlineSmall)
        }
        Text(
            "${document.summary.pageCount} page(s)",
            style = MaterialTheme.typography.bodyMedium,
        )
        DocumentPageGrid(
            pages = document.pages,
            resolveAsset = resolveAsset,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatusMessage(message: String?) {
    message?.let {
        Text(
            text = it,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.semantics {
                liveRegion = LiveRegionMode.Polite
            },
        )
    }
}

private fun safeCaptureMessage(error: ScanError): String = when (error) {
    is ScanError.ServiceUnavailable -> "Scanner unavailable on this device"
    is ScanError.PermissionDenied -> "Camera permission is required"
    is ScanError.PartialCapture -> "Some captured pages are available"
    is ScanError.Busy -> "A capture is already in progress"
    is ScanError.InvalidResult -> "The scanner returned an invalid result"
    is ScanError.StorageFailure -> "Captured pages could not be stored safely"
    is ScanError.LifecycleEnded -> "Capture stopped because this screen closed"
    else -> "Capture failed"
}

private sealed interface AppScreen {
    data object Library : AppScreen
    data class CapturePreview(val pages: List<ScannedPage>) : AppScreen
    data class Document(val details: DocumentDetails) : AppScreen
}
