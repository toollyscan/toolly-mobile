package com.toolly.shared.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.toolly.shared.model.DocumentListItem
import com.toolly.shared.model.ToollyDestination
import com.toolly.shared.model.ToollyUiActions
import com.toolly.shared.model.ToollyUiState
import com.toolly.shared.resources.Res
import com.toolly.shared.resources.app_name
import com.toolly.shared.resources.back
import com.toolly.shared.resources.discard
import com.toolly.shared.resources.document_page_count
import com.toolly.shared.resources.library_subtitle
import com.toolly.shared.resources.no_documents
import com.toolly.shared.resources.review_page_count
import com.toolly.shared.resources.review_scan
import com.toolly.shared.resources.save
import com.toolly.shared.resources.scan_document
import com.toolly.shared.resources.scanned_document
import com.toolly.shared.resources.working
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ToollyApp(
    state: ToollyUiState,
    actions: ToollyUiActions,
) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            when (state.destination) {
                ToollyDestination.LIBRARY -> LibraryScreen(state, actions)
                ToollyDestination.CAPTURE_REVIEW -> ReviewScreen(state, actions)
                ToollyDestination.DOCUMENT_VIEWER -> ViewerScreen(state, actions)
            }
        }
    }
}

@Composable
private fun LibraryScreen(
    state: ToollyUiState,
    actions: ToollyUiActions,
) {
    AdaptiveContent {
        Text(stringResource(Res.string.app_name), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(Res.string.library_subtitle),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
            onClick = actions::scanDocument,
            enabled = !state.busy,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.busy) {
                val description = stringResource(Res.string.working)
                CircularProgressIndicator(
                    modifier = Modifier.semantics { contentDescription = description },
                    strokeWidth = 2.dp,
                )
            } else {
                Text(stringResource(Res.string.scan_document))
            }
        }
        if (state.documents.isEmpty() && !state.busy) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(Res.string.no_documents))
            }
        } else {
            DocumentList(state.documents, actions)
        }
    }
}

@Composable
private fun DocumentList(
    documents: List<DocumentListItem>,
    actions: ToollyUiActions,
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth().weight(1f),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(documents, key = { it.id.value }) { document ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { actions.openDocument(document.id) },
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(stringResource(Res.string.scanned_document))
                    Text(
                        pluralStringResource(
                            Res.plurals.document_page_count,
                            document.pageCount,
                            document.pageCount,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewScreen(
    state: ToollyUiState,
    actions: ToollyUiActions,
) {
    AdaptiveContent {
        Text(stringResource(Res.string.review_scan), style = MaterialTheme.typography.headlineSmall)
        Text(
            pluralStringResource(
                Res.plurals.review_page_count,
                state.reviewPageCount,
                state.reviewPageCount,
            ),
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
        Box(modifier = Modifier.fillMaxWidth().weight(1f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = actions::discardCapture,
                enabled = !state.busy,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(Res.string.discard))
            }
            Button(
                onClick = actions::saveCapture,
                enabled = !state.busy,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(Res.string.save))
            }
        }
    }
}

@Composable
private fun ViewerScreen(
    state: ToollyUiState,
    actions: ToollyUiActions,
) {
    AdaptiveContent {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(onClick = actions::navigateBack) {
                Text(stringResource(Res.string.back))
            }
            Text(
                stringResource(Res.string.scanned_document),
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        val selected = state.documents.firstOrNull { it.id == state.selectedDocumentId }
        selected?.let {
            Text(
                pluralStringResource(
                    Res.plurals.document_page_count,
                    it.pageCount,
                    it.pageCount,
                ),
            )
        }
        Box(modifier = Modifier.fillMaxWidth().weight(1f))
    }
}

@Composable
private fun AdaptiveContent(
    content: @Composable ColumnScope.() -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        val maximumWidth = if (maxWidth >= 600.dp) 920.dp else 640.dp
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = maximumWidth)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content,
        )
    }
}
