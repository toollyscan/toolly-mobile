package com.toolly.spike.capture.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.toolly.domain.model.AssetId
import com.toolly.domain.model.DocumentPage
import com.toolly.spike.capture.R
import java.io.File

@Composable
fun DocumentPageGrid(
    pages: List<DocumentPage>,
    resolveAsset: (AssetId) -> File?,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 140.dp),
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(pages, key = { it.id.value }) { page ->
            PrivateFileImage(
                file = resolveAsset(page.sourceAssetId),
                contentDescription = stringResource(
                    R.string.document_page_description,
                    page.ordinal + 1,
                ),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.707f),
            )
        }
    }
}
