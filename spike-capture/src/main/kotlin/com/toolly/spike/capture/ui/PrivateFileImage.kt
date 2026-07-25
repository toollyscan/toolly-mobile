package com.toolly.spike.capture.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Decodes an app-private image without a third-party loader or persistent plaintext cache. */
@Composable
internal fun PrivateFileImage(
    file: File?,
    contentDescription: String,
    contentScale: ContentScale,
    modifier: Modifier = Modifier,
) {
    val bitmap by produceState<Bitmap?>(initialValue = null, key1 = file?.absolutePath) {
        val decodedBitmap = withContext(Dispatchers.IO) {
            file?.takeIf(File::isFile)?.let(::decodeBoundedBitmap)
        }
        value = decodedBitmap
    }
    DisposableEffect(bitmap) {
        onDispose { bitmap?.recycle() }
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
