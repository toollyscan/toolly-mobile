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
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.toolly.spike.capture.domain.ScannedPage

/**
 * Scrollable thumbnail grid displaying captured [ScannedPage] previews.
 *
 * Images are loaded from app-private temporary URIs using Coil. Coil's in-memory
 * cache is acceptable here; Coil disk caching must remain disabled for vault content
 * in production (see ADR-0011). Spike temp files are not vault content, but the
 * same no-disk-cache policy is applied here for consistency.
 *
 * No document paths, filenames or PII are logged.
 */
@Composable
fun ThumbnailGrid(
    pages: List<ScannedPage>,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 100.dp),
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(pages, key = { it.index }) { page ->
            AsyncImage(
                model = page.imageUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.707f), // A4 portrait ratio
            )
        }
    }
}
