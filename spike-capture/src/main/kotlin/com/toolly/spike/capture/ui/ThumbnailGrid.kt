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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.toolly.spike.capture.R
import com.toolly.spike.capture.domain.ScannedPage
import com.toolly.spike.capture.domain.TemporaryAssetId
import java.io.File

/** Displays app-private temporary pages without placing document pixels in Coil caches. */
@Composable
fun ThumbnailGrid(
    pages: List<ScannedPage>,
    resolveAsset: (TemporaryAssetId) -> File?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 100.dp),
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(pages, key = { it.assetId.value }) { page ->
            val request = ImageRequest.Builder(context)
                .data(resolveAsset(page.assetId))
                .diskCachePolicy(CachePolicy.DISABLED)
                .memoryCachePolicy(CachePolicy.DISABLED)
                .build()
            AsyncImage(
                model = request,
                contentDescription = stringResource(
                    R.string.scanned_page_description,
                    page.index + 1,
                ),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.707f),
            )
        }
    }
}
