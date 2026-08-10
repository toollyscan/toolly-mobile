package com.toolly.spike.capture.ui

import android.graphics.Bitmap

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.toolly.domain.model.AssetId
import com.toolly.domain.model.DocumentPage
import com.toolly.spike.capture.R

@Composable
fun DocumentPageGrid(
    pages: List<DocumentPage>,
    loadAssetBitmap: suspend (AssetId) -> Bitmap?,
    modifier: Modifier = Modifier,
    selectedOrdinals: Set<Int>? = null,
    onToggle: ((DocumentPage) -> Unit)? = null,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 140.dp),
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(pages, key = { it.id.value }) { page ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.707f)
                    .let {
                        if (onToggle != null) it.clickable { onToggle(page) } else it
                    },
            ) {
                PrivateBitmapImage(
                    sourceKey = page.sourceAssetId.value,
                    loadBitmap = { loadAssetBitmap(page.sourceAssetId) },
                    contentDescription = stringResource(
                        R.string.document_page_description,
                        page.ordinal + 1,
                    ),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth().aspectRatio(0.707f),
                )
                if (selectedOrdinals != null) {
                    Checkbox(
                        checked = page.ordinal in selectedOrdinals,
                        onCheckedChange = { if (onToggle != null) onToggle(page) },
                        colors = CheckboxDefaults.colors(),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.85f)),
                    )
                }
            }
        }
    }
}
