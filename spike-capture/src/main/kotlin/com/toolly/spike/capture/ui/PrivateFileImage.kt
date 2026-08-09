package com.toolly.spike.capture.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Renders a bounded bitmap and releases its native pixels when the source changes or leaves UI. */
@Composable
internal fun PrivateBitmapImage(
    sourceKey: String?,
    loadBitmap: suspend () -> Bitmap?,
    contentDescription: String,
    contentScale: ContentScale,
    modifier: Modifier = Modifier,
) {
    var bitmap by remember(sourceKey) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(sourceKey) {
        bitmap = loadBitmap()
    }
    DisposableEffect(bitmap) {
        val bitmapToRecycle = bitmap
        onDispose { bitmapToRecycle?.recycle() }
    }
    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier,
        )
    }
}

/** Decodes a scanner-owned temporary file without a third-party loader or disk cache. */
@Composable
internal fun PrivateFileImage(
    file: File?,
    contentDescription: String,
    contentScale: ContentScale,
    modifier: Modifier = Modifier,
) {
    PrivateBitmapImage(
        sourceKey = file?.absolutePath,
        loadBitmap = {
            withContext(Dispatchers.IO) {
                file?.takeIf(File::isFile)?.let(::decodeBoundedBitmap)
            }
        },
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier,
    )
}

/**
 * Decodes a scanner-owned temporary file to a Compose Multiplatform [ImageBitmap] -- the common
 * type (not an Android-specific one) that [com.toolly.shared.ui.CropOverlay] and the crop/enhance
 * screens accept, so the same decoded bitmap can be reused for both the main preview and the
 * precision loupe without decoding twice.
 *
 * On Android, `Bitmap.asImageBitmap()` wraps the same [Bitmap] instance rather than copying it, so
 * the underlying bitmap must stay alive for as long as the returned [ImageBitmap] is used. Callers
 * own that lifetime and must release it via `imageBitmap.asAndroidBitmap().recycle()` (e.g. in a
 * `DisposableEffect`) once the page-edit session using it ends.
 */
internal suspend fun decodeBoundedImageBitmap(file: File): ImageBitmap? =
    withContext(Dispatchers.IO) {
        file.takeIf(File::isFile)?.let(::decodeBoundedBitmap)?.asImageBitmap()
    }

private fun decodeBoundedBitmap(file: File): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    var sampleSize = 1
    while (
        bounds.outWidth / sampleSize > MAX_DECODE_DIMENSION ||
        bounds.outHeight / sampleSize > MAX_DECODE_DIMENSION
    ) {
        sampleSize *= 2
    }
    return BitmapFactory.decodeFile(
        file.absolutePath,
        BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        },
    )
}

private const val MAX_DECODE_DIMENSION = 2048
