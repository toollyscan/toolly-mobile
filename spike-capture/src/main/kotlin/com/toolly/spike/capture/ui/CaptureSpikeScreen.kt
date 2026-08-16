package com.toolly.spike.capture.ui

import android.graphics.Bitmap
import android.graphics.Matrix

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.toolly.domain.model.AssetId
import com.toolly.domain.model.DocumentCategory
import com.toolly.domain.model.DocumentDetails
import com.toolly.domain.model.DocumentExportDelivery
import com.toolly.domain.model.DocumentExportFormat
import com.toolly.domain.model.DocumentExportOutcome
import com.toolly.domain.model.DocumentId
import com.toolly.domain.model.DocumentPage
import com.toolly.domain.model.DocumentSummary
import com.toolly.domain.model.PageId
import com.toolly.foundation.ToollyErrorCode
import com.toolly.foundation.ToollyResult
import com.toolly.spike.capture.R
import com.toolly.shared.capture.ScanConfig
import com.toolly.shared.capture.ScanError
import com.toolly.shared.capture.ScanResult
import com.toolly.shared.capture.ScannedPage
import com.toolly.shared.capture.TemporaryAssetId
import com.toolly.shared.edit.CropRegion
import com.toolly.shared.edit.EnhancementMode
import com.toolly.shared.edit.NormalizedPoint
import com.toolly.shared.ui.CropPageScreen
import com.toolly.shared.ui.EnhancePageScreen
import com.toolly.shared.ui.ExportBuilderScreen
import com.toolly.shared.ui.ExportPrivacyCheckScreen
import com.toolly.shared.ui.ToollyBackIcon
import com.toolly.shared.ui.ToollyDocumentIcon
import com.toolly.shared.ui.ToollyExportFormat
import com.toolly.shared.ui.ToollySearchIcon
import java.io.File
import java.text.DateFormat
import java.util.Date

/**
 * First production-shaped Toolly Android walking slice.
 *
 * The UI depends on Toolly models and callback boundaries. It has no filesystem, database,
 * Firebase, ML Kit or provider types in its state.
 */
@Composable
fun ToollyDocumentApp(
    onLaunchCapture: (ScanConfig, onResult: (ScanResult) -> Unit) -> Unit,
    onImportPdf: (onResult: (ScanResult) -> Unit) -> Unit,
    onLoadDocuments: (onResult: (ToollyResult<List<DocumentSummary>>) -> Unit) -> Unit,
    onSavePages: (List<ScannedPage>, onResult: (ToollyResult<DocumentDetails>) -> Unit) -> Unit,
    onOpenDocument: (DocumentId, onResult: (ToollyResult<DocumentDetails>) -> Unit) -> Unit,
    onExportDocument: (
        DocumentDetails,
        DocumentExportFormat,
        DocumentExportDelivery,
        onResult: (DocumentExportOutcome) -> Unit,
    ) -> Unit,
    onRenameDocument: (DocumentId, String?, onResult: (ToollyResult<DocumentDetails>) -> Unit) -> Unit,
    onTagDocument: (DocumentId, DocumentCategory?, onResult: (ToollyResult<DocumentDetails>) -> Unit) -> Unit,
    onReplacePageAsset: (
        DocumentId,
        PageId,
        CropRegion?,
        EnhancementMode,
        Float,
        Int,
        onResult: (ToollyResult<DocumentDetails>) -> Unit,
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

    fun launchPdfImport() {
        if (!isWorking && screen == AppScreen.Library) {
            isWorking = true
            message = null
            onImportPdf { result ->
                isWorking = false
                when (result) {
                    is ScanResult.Success -> screen = AppScreen.CapturePreview(result.pages)
                    ScanResult.Cancelled -> { /* user backed out of the file picker */ }
                    is ScanResult.Failure -> message = UiMessage(pdfImportErrorMessage(result.error))
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
                onImportPdf = ::launchPdfImport,
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
                onRename = { name ->
                    onRenameDocument(current.details.summary.id, name) { result ->
                        when (result) {
                            is ToollyResult.Success -> {
                                screen = AppScreen.Document(result.value)
                                message = null
                                refreshLibrary()
                            }
                            is ToollyResult.Failure ->
                                message = UiMessage(toollyErrorMessage(result.error.code))
                        }
                    }
                },
                onTag = { category ->
                    onTagDocument(current.details.summary.id, category) { result ->
                        when (result) {
                            is ToollyResult.Success -> {
                                screen = AppScreen.Document(result.value)
                                message = null
                                refreshLibrary()
                            }
                            is ToollyResult.Failure ->
                                message = UiMessage(toollyErrorMessage(result.error.code))
                        }
                    }
                },
                onStartExport = {
                    message = null
                    screen = AppScreen.ExportBuilder(current.details)
                },
                onReplacePage = { pageId, crop, mode, intensity, rotationQuarterTurns ->
                    onReplacePageAsset(
                        current.details.summary.id,
                        pageId,
                        crop,
                        mode,
                        intensity,
                        rotationQuarterTurns,
                    ) { result ->
                        when (result) {
                            is ToollyResult.Success -> {
                                screen = AppScreen.Document(result.value)
                                message = null
                                refreshLibrary()
                            }
                            is ToollyResult.Failure ->
                                message = UiMessage(toollyErrorMessage(result.error.code))
                        }
                    }
                },
                onBack = {
                    message = null
                    screen = AppScreen.Library
                },
            )

            is AppScreen.ExportBuilder -> {
                var format by remember(current.details) { mutableStateOf(ToollyExportFormat.PDF) }
                var selectedOrdinals by remember(current.details) {
                    mutableStateOf(current.details.pages.map { it.ordinal }.toSet())
                }
                ExportBuilderScreen(
                    format = format,
                    onFormatChange = { format = it },
                    onContinue = {
                        screen = AppScreen.ExportPrivacyCheck(
                            current.details.filteredToPages(selectedOrdinals),
                            format,
                        )
                    },
                    continueEnabled = selectedOrdinals.isNotEmpty(),
                    onBack = {
                        message = null
                        screen = AppScreen.Document(current.details)
                    },
                    previewContent = {
                        DocumentPageGrid(
                            pages = current.details.pages,
                            loadAssetBitmap = loadDocumentAssetBitmap,
                            modifier = Modifier.heightIn(max = 240.dp),
                            selectedOrdinals = selectedOrdinals,
                            onToggle = { page ->
                                selectedOrdinals = if (page.ordinal in selectedOrdinals) {
                                    selectedOrdinals - page.ordinal
                                } else {
                                    selectedOrdinals + page.ordinal
                                }
                            },
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

/**
 * Real Search (wireframe 4.2/4.4) -- title-only matching against [DocumentSummary.displayName]
 * (see `USER_FLOW_MATRIX.md` PT-04: recognized-text/OCR matching is premium/deferred, there is no
 * OCR pipeline to match against). Loads its own copy of the document list independently of
 * [ToollyDocumentApp]'s Library tab -- a second cheap local-disk read on switching to this tab,
 * traded for not hoisting document/navigation state across both tabs' composables.
 */
@Composable
fun SearchDocumentsScreen(
    onLoadDocuments: (onResult: (ToollyResult<List<DocumentSummary>>) -> Unit) -> Unit,
    onOpenDocument: (DocumentId, onResult: (ToollyResult<DocumentDetails>) -> Unit) -> Unit,
    onExportDocument: (
        DocumentDetails,
        DocumentExportFormat,
        DocumentExportDelivery,
        onResult: (DocumentExportOutcome) -> Unit,
    ) -> Unit,
    onRenameDocument: (DocumentId, String?, onResult: (ToollyResult<DocumentDetails>) -> Unit) -> Unit,
    onTagDocument: (DocumentId, DocumentCategory?, onResult: (ToollyResult<DocumentDetails>) -> Unit) -> Unit,
    onReplacePageAsset: (
        DocumentId,
        PageId,
        CropRegion?,
        EnhancementMode,
        Float,
        Int,
        onResult: (ToollyResult<DocumentDetails>) -> Unit,
    ) -> Unit,
    loadDocumentAssetBitmap: suspend (AssetId) -> Bitmap?,
    recentSearches: List<String>,
    onRecentSearchesChanged: (List<String>) -> Unit,
) {
    var documents by remember { mutableStateOf<List<DocumentSummary>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var isWorking by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<UiMessage?>(null) }
    var screen by remember { mutableStateOf<SearchResultScreen>(SearchResultScreen.Results) }

    LaunchedEffect(Unit) {
        onLoadDocuments { result ->
            if (result is ToollyResult.Success) documents = result.value
        }
    }

    val results = remember(documents, query) {
        if (query.isBlank()) {
            emptyList()
        } else {
            documents.filter { it.displayName?.contains(query, ignoreCase = true) == true }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        when (val current = screen) {
            SearchResultScreen.Results -> SearchResultsScreen(
                query = query,
                onQueryChange = { query = it },
                results = results,
                message = message,
                recentSearches = recentSearches,
                onRecentSearchSelected = { query = it },
                onClearRecentSearches = { onRecentSearchesChanged(emptyList()) },
                onOpen = { documentId ->
                    isWorking = true
                    onOpenDocument(documentId) { result ->
                        isWorking = false
                        when (result) {
                            is ToollyResult.Success -> {
                                screen = SearchResultScreen.Document(result.value)
                                // A search that actually led somewhere is worth remembering --
                                // matches saving on submit/navigate rather than every keystroke,
                                // since this search is live-filter-as-you-type with no separate
                                // submit action.
                                val trimmed = query.trim()
                                if (trimmed.isNotEmpty()) {
                                    val updated = (listOf(trimmed) + recentSearches.filter { it != trimmed })
                                        .take(MAX_RECENT_SEARCHES)
                                    onRecentSearchesChanged(updated)
                                }
                            }
                            is ToollyResult.Failure ->
                                message = UiMessage(toollyErrorMessage(result.error.code))
                        }
                    }
                },
            )

            is SearchResultScreen.Document -> DocumentScreen(
                document = current.details,
                message = message,
                loadAssetBitmap = loadDocumentAssetBitmap,
                onRename = { name ->
                    onRenameDocument(current.details.summary.id, name) { result ->
                        when (result) {
                            is ToollyResult.Success -> {
                                screen = SearchResultScreen.Document(result.value)
                                message = null
                            }
                            is ToollyResult.Failure ->
                                message = UiMessage(toollyErrorMessage(result.error.code))
                        }
                    }
                },
                onTag = { category ->
                    onTagDocument(current.details.summary.id, category) { result ->
                        when (result) {
                            is ToollyResult.Success -> {
                                screen = SearchResultScreen.Document(result.value)
                                message = null
                            }
                            is ToollyResult.Failure ->
                                message = UiMessage(toollyErrorMessage(result.error.code))
                        }
                    }
                },
                onStartExport = {
                    message = null
                    screen = SearchResultScreen.ExportBuilder(current.details)
                },
                onReplacePage = { pageId, crop, mode, intensity, rotationQuarterTurns ->
                    onReplacePageAsset(
                        current.details.summary.id,
                        pageId,
                        crop,
                        mode,
                        intensity,
                        rotationQuarterTurns,
                    ) { result ->
                        when (result) {
                            is ToollyResult.Success -> {
                                screen = SearchResultScreen.Document(result.value)
                                message = null
                            }
                            is ToollyResult.Failure ->
                                message = UiMessage(toollyErrorMessage(result.error.code))
                        }
                    }
                },
                onBack = {
                    message = null
                    screen = SearchResultScreen.Results
                },
            )

            is SearchResultScreen.ExportBuilder -> {
                var format by remember(current.details) { mutableStateOf(ToollyExportFormat.PDF) }
                var selectedOrdinals by remember(current.details) {
                    mutableStateOf(current.details.pages.map { it.ordinal }.toSet())
                }
                ExportBuilderScreen(
                    format = format,
                    onFormatChange = { format = it },
                    onContinue = {
                        screen = SearchResultScreen.ExportPrivacyCheck(
                            current.details.filteredToPages(selectedOrdinals),
                            format,
                        )
                    },
                    continueEnabled = selectedOrdinals.isNotEmpty(),
                    onBack = {
                        message = null
                        screen = SearchResultScreen.Document(current.details)
                    },
                    previewContent = {
                        DocumentPageGrid(
                            pages = current.details.pages,
                            loadAssetBitmap = loadDocumentAssetBitmap,
                            modifier = Modifier.heightIn(max = 240.dp),
                            selectedOrdinals = selectedOrdinals,
                            onToggle = { page ->
                                selectedOrdinals = if (page.ordinal in selectedOrdinals) {
                                    selectedOrdinals - page.ordinal
                                } else {
                                    selectedOrdinals + page.ordinal
                                }
                            },
                        )
                    },
                )
            }

            is SearchResultScreen.ExportPrivacyCheck -> {
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
                                    screen = SearchResultScreen.Document(current.details)
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
                                    screen = SearchResultScreen.Document(current.details)
                                }
                            }
                        }
                    },
                    onBack = {
                        screen = SearchResultScreen.ExportBuilder(current.details)
                    },
                )
            }
        }
    }
}

@Composable
private fun SearchResultsScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    results: List<DocumentSummary>,
    message: UiMessage?,
    recentSearches: List<String>,
    onRecentSearchSelected: (String) -> Unit,
    onClearRecentSearches: () -> Unit,
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
                stringResource(R.string.search_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                label = { Text(stringResource(R.string.library_search_hint)) },
                leadingIcon = { ToollySearchIcon() },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            StatusMessage(message)
            when {
                // No "Suggested" section here (unlike the wireframe): there's no real signal to
                // suggest from (no query popularity/trending data exists anywhere) -- showing one
                // would mean fabricating it. "Search in: Documents/Tags/Dates/Places" facets are
                // omitted the same way -- only title matching is real; Tags/Dates/Places aren't
                // tracked metadata at all.
                query.isBlank() && recentSearches.isNotEmpty() -> Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(R.string.recent_searches_label),
                            style = MaterialTheme.typography.labelLarge,
                        )
                        TextButton(onClick = onClearRecentSearches) {
                            Text(stringResource(R.string.clear))
                        }
                    }
                    for (recent in recentSearches) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable { onRecentSearchSelected(recent) },
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                ToollySearchIcon(iconSize = 18.dp)
                                Text(recent)
                            }
                        }
                    }
                }
                query.isBlank() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.search_prompt), style = MaterialTheme.typography.titleMedium)
                }
                results.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_search_results), style = MaterialTheme.typography.titleMedium)
                }
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(results, key = { it.id.value }) { document ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onOpen(document.id) },
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                ToollyDocumentIcon(
                                    iconSize = 28.dp,
                                    modifier = Modifier.padding(top = 2.dp),
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        document.displayName ?: stringResource(R.string.scanned_document),
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                    Text(
                                        stringResource(R.string.search_result_matched_title),
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
}

private sealed interface SearchResultScreen {
    data object Results : SearchResultScreen
    data class Document(val details: DocumentDetails) : SearchResultScreen
    data class ExportBuilder(val details: DocumentDetails) : SearchResultScreen
    data class ExportPrivacyCheck(val details: DocumentDetails, val format: ToollyExportFormat) : SearchResultScreen
}

private fun ToollyExportFormat.toDomainFormat(): DocumentExportFormat = when (this) {
    ToollyExportFormat.PDF -> DocumentExportFormat.PDF
    ToollyExportFormat.JPEG -> DocumentExportFormat.JPEG
}

/**
 * A page-range-selected copy of [this] document, matching wireframe 5.1's page-selection grid.
 * Pages are re-numbered to a contiguous 0-based ordinal (required by [DocumentDetails]'s own
 * invariant) -- the export step never sees or persists the original ordinals of skipped pages.
 * Export-only: nothing here touches the vault, [sourceAssetId]s still point at the same real
 * encrypted assets.
 */
private fun DocumentDetails.filteredToPages(selectedOrdinals: Set<Int>): DocumentDetails {
    val kept = pages.filter { it.ordinal in selectedOrdinals }.sortedBy { it.ordinal }
    val renumbered = kept.mapIndexed { index, page -> page.copy(ordinal = index) }
    return DocumentDetails(
        summary = summary.copy(pageCount = renumbered.size),
        pages = renumbered,
    )
}

/**
 * Library's Receipts/IDs/Other chips filter on [DocumentSummary.category]; Untagged surfaces
 * documents nobody has categorized yet. This is a fixed, closed set (mirrors [DocumentCategory]
 * plus an explicit "no category" bucket) rather than the wireframe's "Offline" chip, which would
 * have been misleading here -- every document in this vault is already local-only, so an
 * "Offline" filter would just duplicate "All".
 */
private enum class LibraryFilter { ALL, RECEIPTS, IDS, OTHER, UNTAGGED }

private fun DocumentSummary.matchesFilter(filter: LibraryFilter): Boolean = when (filter) {
    LibraryFilter.ALL -> true
    LibraryFilter.RECEIPTS -> category == DocumentCategory.RECEIPT
    LibraryFilter.IDS -> category == DocumentCategory.IDENTIFICATION
    LibraryFilter.OTHER -> category == DocumentCategory.OTHER
    LibraryFilter.UNTAGGED -> category == null
}

/**
 * "N pages · Saved today/yesterday/N days ago" (wireframe 4.1's card subtitle). Uses
 * [document]'s own `updatedAtEpochMillis` against wall-clock time at composition -- a plain
 * display label, not something that needs to tick live, so recomposing only when the card itself
 * recomposes is fine.
 */
@Composable
private fun documentCardSubtitle(document: DocumentSummary): String {
    val pageCount = pluralStringResource(R.plurals.page_count, document.pageCount, document.pageCount)
    val elapsedDays = ((System.currentTimeMillis() - document.updatedAtEpochMillis) / DAY_MILLIS)
        .coerceAtLeast(0L)
    val saved = when (elapsedDays) {
        0L -> stringResource(R.string.saved_today)
        1L -> stringResource(R.string.saved_yesterday)
        else -> stringResource(R.string.saved_days_ago, elapsedDays)
    }
    return stringResource(R.string.document_card_subtitle, pageCount, saved)
}

private const val DAY_MILLIS = 24L * 60L * 60L * 1000L
private const val MAX_RECENT_SEARCHES = 5

@StringRes
private fun LibraryFilter.labelRes(): Int = when (this) {
    LibraryFilter.ALL -> R.string.category_all
    LibraryFilter.RECEIPTS -> R.string.category_receipts
    LibraryFilter.IDS -> R.string.category_ids
    LibraryFilter.OTHER -> R.string.category_other
    LibraryFilter.UNTAGGED -> R.string.category_untagged
}

@Composable
private fun LibraryScreen(
    documents: List<DocumentSummary>,
    isWorking: Boolean,
    message: UiMessage?,
    onScan: () -> Unit,
    onImportPdf: () -> Unit,
    onOpen: (DocumentId) -> Unit,
) {
    val expanded = LocalConfiguration.current.screenWidthDp >= 600
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(LibraryFilter.ALL) }
    val filteredDocuments = remember(documents, query, filter) {
        documents.filter { document ->
            document.matchesFilter(filter) &&
                (query.isBlank() || document.displayName?.contains(query, ignoreCase = true) == true)
        }
    }
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
            OutlinedButton(
                onClick = onImportPdf,
                enabled = !isWorking,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.import_pdf))
            }
            StatusMessage(message)
            if (documents.isNotEmpty()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(stringResource(R.string.library_search_hint)) },
                    leadingIcon = { ToollySearchIcon() },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                ) {
                    for (option in LibraryFilter.entries) {
                        FilterChip(
                            selected = filter == option,
                            onClick = { filter = option },
                            label = { Text(stringResource(option.labelRes())) },
                        )
                    }
                }
            }
            if (documents.isEmpty() && !isWorking) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.no_documents),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            } else if (documents.isNotEmpty() && filteredDocuments.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.no_search_results),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredDocuments, key = { it.id.value }) { document ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpen(document.id) },
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                ToollyDocumentIcon(
                                    iconSize = 28.dp,
                                    modifier = Modifier.padding(top = 2.dp),
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        document.displayName ?: stringResource(R.string.scanned_document),
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                    Text(
                                        documentCardSubtitle(document),
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
    onRename: (String?) -> Unit,
    onTag: (DocumentCategory?) -> Unit,
    onStartExport: () -> Unit,
    onReplacePage: (PageId, CropRegion?, EnhancementMode, Float, Int) -> Unit,
    onBack: () -> Unit,
) {
    var renaming by remember { mutableStateOf(false) }
    var editingPage by remember { mutableStateOf<DocumentPage?>(null) }

    val page = editingPage
    if (page != null) {
        PageEditFlow(
            page = page,
            loadAssetBitmap = loadAssetBitmap,
            onCancel = { editingPage = null },
            onSave = { crop, mode, intensity, rotationQuarterTurns ->
                editingPage = null
                onReplacePage(page.id, crop, mode, intensity, rotationQuarterTurns)
            },
        )
        return
    }

    var menuExpanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                ToollyBackIcon(iconSize = 18.dp)
            }
            Text(
                stringResource(R.string.document_detail_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
            Box {
                val optionsDescription = stringResource(R.string.document_options_description)
                IconButton(onClick = { menuExpanded = true }) {
                    Text(
                        "⋮",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.semantics { contentDescription = optionsDescription },
                    )
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.rename_document)) },
                        onClick = {
                            menuExpanded = false
                            renaming = true
                        },
                    )
                }
            }
        }

        val firstPage = document.pages.minByOrNull { it.ordinal }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp)),
        ) {
            if (firstPage != null) {
                PrivateBitmapImage(
                    sourceKey = firstPage.sourceAssetId.value,
                    loadBitmap = { loadAssetBitmap(firstPage.sourceAssetId) },
                    contentDescription = document.summary.displayName
                        ?: stringResource(R.string.scanned_document),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f)),
                        ),
                    )
                    .padding(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        document.summary.displayName ?: stringResource(R.string.scanned_document),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                    )
                    Text(
                        stringResource(
                            R.string.scanned_caption,
                            DateFormat.getDateInstance(DateFormat.MEDIUM)
                                .format(Date(document.summary.createdAtEpochMillis)),
                        ),
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(
                    stringResource(R.string.date_scanned_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    DateFormat.getDateInstance(DateFormat.MEDIUM)
                        .format(Date(document.summary.createdAtEpochMillis)),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    stringResource(R.string.pages_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(document.summary.pageCount.toString(), style = MaterialTheme.typography.titleMedium)
            }
        }
        // File size isn't tracked anywhere in the domain model yet (no byte-size field on
        // DocumentSummary/DocumentPage) -- omitted rather than fabricated. A real fix needs a new
        // repository read path, scoped separately from this screen's own change.

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                stringResource(R.string.document_category_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState()),
            ) {
                for (option in DocumentCategory.entries) {
                    FilterChip(
                        selected = document.summary.category == option,
                        onClick = {
                            onTag(if (document.summary.category == option) null else option)
                        },
                        label = { Text(stringResource(option.labelRes())) },
                    )
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Share and Export both enter the same real export flow (format -> privacy check ->
            // save/share delivery choice) -- there's no separate one-tap share path today, so
            // this doesn't invent a second one just to give the buttons different behavior.
            OutlinedButton(onClick = onStartExport, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.share))
            }
            Button(onClick = onStartExport, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.export_document))
            }
        }
        // No delete affordance: EncryptedDocumentRepository has no delete capability at all yet
        // (no tombstone/cleanup path per VAULT_AND_PROCESSING_CONTRACTS.md). Not shown rather
        // than shown-and-broken; a real implementation is its own scoped, vault-touching change.

        if (document.pages.size > 1) {
            Text(
                stringResource(R.string.tap_page_to_edit),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            DocumentPageGrid(
                pages = document.pages,
                loadAssetBitmap = loadAssetBitmap,
                modifier = Modifier.weight(1f),
                onToggle = { tapped -> editingPage = tapped },
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
        StatusMessage(message)
    }
    if (renaming) {
        RenameDocumentDialog(
            initialName = document.summary.displayName.orEmpty(),
            onConfirm = { name ->
                renaming = false
                onRename(name)
            },
            onDismiss = { renaming = false },
        )
    }
}

/** Steps of the "re-crop/enhance an already-saved page" flow (wireframe `3.1`/`1.4`). */
private enum class PageEditStep { CROP, ENHANCE }

private val FULL_PAGE_CROP_REGION = CropRegion(
    topLeft = NormalizedPoint(0f, 0f),
    topRight = NormalizedPoint(1f, 0f),
    bottomRight = NormalizedPoint(1f, 1f),
    bottomLeft = NormalizedPoint(0f, 1f),
)

/**
 * Crop then enhance a single already-saved page, mirroring wireframes `3.1 Manual corners`/
 * `1.4 Clean and save`. Deliberately reachable only from a saved document's own page grid, not
 * inserted into live capture -- ML Kit's own scanner UI already covers crop during capture and
 * gallery import (issue #52: "without copying Google scanner UI").
 *
 * [onSave] fires optimistically (same fire-and-forget pattern as [DocumentScreen]'s onRename/
 * onTag); the caller performs the actual vault write and surfaces any failure through the shared
 * `message`/[StatusMessage] mechanism, not through this composable's own state.
 *
 * Rotation rotates the *displayed* bitmap (a cheap, display-only [Bitmap] transform, matching
 * `1.3`/`3.1`'s Rotate action) and resets [region] to full-frame each time -- corner positions
 * from before a rotation don't mean anything in the new orientation, and asking the user to
 * re-drag four corners after every rotation is the honest cost of not having a corner-remapping
 * algorithm, not a shortcut. The actual pixel rotation (shared `ImageRotation.rotate90`, applied
 * before crop) only runs once, in [AndroidPageEditor.applyToBuffer], on save.
 */
@Composable
private fun PageEditFlow(
    page: DocumentPage,
    loadAssetBitmap: suspend (AssetId) -> Bitmap?,
    onCancel: () -> Unit,
    onSave: (CropRegion?, EnhancementMode, Float, Int) -> Unit,
) {
    var step by remember(page.id) { mutableStateOf(PageEditStep.CROP) }
    var region by remember(page.id) { mutableStateOf(FULL_PAGE_CROP_REGION) }
    var mode by remember(page.id) { mutableStateOf(EnhancementMode.AUTO) }
    var intensity by remember(page.id) { mutableFloatStateOf(0.5f) }
    var rotationQuarterTurns by remember(page.id) { mutableIntStateOf(0) }
    var sourceBitmap by remember(page.id) { mutableStateOf<Bitmap?>(null) }
    var image by remember(page.id) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(page.id) {
        sourceBitmap = loadAssetBitmap(page.sourceAssetId)
        image = sourceBitmap?.asImageBitmap()
    }

    when (step) {
        PageEditStep.CROP -> CropPageScreen(
            image = image,
            region = region,
            onRegionChange = { region = it },
            onAutoCrop = { region = FULL_PAGE_CROP_REGION },
            onRotate = {
                val newRotation = (rotationQuarterTurns + 1) % 4
                rotationQuarterTurns = newRotation
                region = FULL_PAGE_CROP_REGION
                // Always rotate from the untouched original, by the full accumulated angle -- not
                // the already-displayed bitmap -- so repeated taps don't compound past 90 degrees
                // per tap (createBitmap+Matrix is cheap enough to redo from source each time).
                sourceBitmap?.let { original ->
                    val matrix = Matrix().apply { postRotate(newRotation * 90f) }
                    image = Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
                        .asImageBitmap()
                }
            },
            onRetake = onCancel,
            onContinue = { step = PageEditStep.ENHANCE },
        )
        PageEditStep.ENHANCE -> EnhancePageScreen(
            image = image,
            mode = mode,
            intensity = intensity,
            onModeChange = { mode = it },
            onIntensityChange = { intensity = it },
            // Only send a crop if the user actually moved a corner -- an untouched full-frame
            // region is "accept the page as-is" per PageEditRequest's own doc, so this skips an
            // unnecessary resample (and matching quality loss) rather than warping a no-op shape.
            onSave = {
                onSave(region.takeIf { it != FULL_PAGE_CROP_REGION }, mode, intensity, rotationQuarterTurns)
            },
        )
    }
}

@Composable
private fun RenameDocumentDialog(
    initialName: String,
    onConfirm: (String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.rename_document_title)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                label = { Text(stringResource(R.string.rename_document_hint)) },
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name.trim().ifEmpty { null }) }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        },
    )
}

@StringRes
private fun DocumentCategory.labelRes(): Int = when (this) {
    DocumentCategory.RECEIPT -> R.string.category_receipts
    DocumentCategory.IDENTIFICATION -> R.string.category_ids
    DocumentCategory.OTHER -> R.string.category_other
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

/**
 * Deliberately collapses every [ScanResult.Failure] cause (bad file, too many pages, storage
 * failure) into one message -- matching [captureErrorMessage]'s own coarseness -- rather than
 * inventing per-cause copy for a picker flow with no equivalent wireframe to match.
 */
@StringRes
private fun pdfImportErrorMessage(error: ScanError): Int = when (error) {
    is ScanError.StorageFailure -> R.string.captured_pages_storage_failed
    else -> R.string.pdf_import_failed
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
