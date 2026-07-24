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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.toolly.domain.model.AssetId
import com.toolly.domain.model.DocumentPage
import java.io.File

@Composable
fun DocumentPageGrid(
    pages: List<DocumentPage>,
    resolveAsset: (AssetId) -> File?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 140.dp),
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(pages, key = { it.id.value }) { page ->
            val request = ImageRequest.Builder(context)
                .data(resolveAsset(page.sourceAssetId))
                .diskCachePolicy(CachePolicy.DISABLED)
                .memoryCachePolicy(CachePolicy.DISABLED)
                .build()
            AsyncImage(
                model = request,
                contentDescription = "Document page ${page.ordinal + 1}",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.707f),
            )
        }
    }
}
