package com.toolly.spike.capture.export

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import com.toolly.domain.model.AssetId
import com.toolly.domain.model.DocumentDetails
import com.toolly.domain.model.DocumentPage
import com.toolly.foundation.ToollyError
import com.toolly.foundation.ToollyErrorCode
import com.toolly.foundation.ToollyResult
import com.toolly.shared.edit.SignatureStrokes
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.util.concurrent.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/**
 * Export quality tiers (wireframe `5.1`'s Small/Balanced/Best chips). Each tier re-encodes every
 * page as a JPEG at [jpegQuality] before embedding it -- [PdfDocument]'s canvas draws bitmaps
 * uncompressed otherwise, so this is the only way "quality" actually changes the output file size.
 * [downscale] additionally shrinks pixel dimensions for the smaller tiers.
 */
enum class ExportQuality(val downscale: Float, val jpegQuality: Int) {
    SMALL(downscale = 0.5f, jpegQuality = 50),
    BALANCED(downscale = 0.75f, jpegQuality = 75),
    BEST(downscale = 1f, jpegQuality = 92),
}

/**
 * Android platform-only PDF/JPEG writer.
 *
 * Callers own the user-selected destination. This class never creates an app-private plaintext
 * export file and keeps at most one bounded page bitmap in memory.
 */
internal class AndroidDocumentExporter(
    private val loadBitmap: suspend (AssetId) -> ToollyResult<Bitmap>,
) {
    suspend fun writePdf(
        document: DocumentDetails,
        destination: OutputStream,
        quality: ExportQuality = ExportQuality.BEST,
        watermarkText: String? = null,
        signature: SignatureStrokes = emptyList(),
    ): ToollyResult<Unit> = withContext(Dispatchers.IO) {
        val pdf = PdfDocument()
        val lastOrdinal = document.pages.maxOfOrNull { it.ordinal }
        try {
            for (page in document.pages.sortedBy { it.ordinal }) {
                coroutineContext.ensureActive()
                val loadedBitmap = when (val loaded = loadBitmap(page.sourceAssetId)) {
                    is ToollyResult.Success -> loaded.value
                    is ToollyResult.Failure -> return@withContext loaded
                }
                val bitmap = recompress(loadedBitmap, quality)
                try {
                    val pageSize = pageSize(bitmap)
                    val pdfPage = pdf.startPage(
                        PdfDocument.PageInfo.Builder(
                            pageSize.width,
                            pageSize.height,
                            page.ordinal + 1,
                        ).create(),
                    )
                    try {
                        pdfPage.canvas.drawColor(Color.WHITE)
                        pdfPage.canvas.drawBitmap(
                            bitmap,
                            null,
                            fittedDestination(bitmap, pageSize),
                            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG),
                        )
                        if (!watermarkText.isNullOrBlank()) {
                            drawWatermark(
                                pdfPage.canvas,
                                watermarkText,
                                pageSize.width.toFloat(),
                                pageSize.height.toFloat(),
                            )
                        }
                        if (signature.isNotEmpty() && page.ordinal == lastOrdinal) {
                            drawSignature(
                                pdfPage.canvas,
                                signature,
                                pageSize.width.toFloat(),
                                pageSize.height.toFloat(),
                            )
                        }
                    } finally {
                        pdf.finishPage(pdfPage)
                    }
                } finally {
                    bitmap.recycle()
                }
            }
            coroutineContext.ensureActive()
            pdf.writeTo(destination)
            destination.flush()
            ToollyResult.Success(Unit)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            retryableFailure()
        } finally {
            pdf.close()
        }
    }

    /**
     * Downscales (if [ExportQuality.downscale] &lt; 1) then round-trips through a real JPEG
     * encode/decode at [ExportQuality.jpegQuality] -- genuine lossy compression, not a cosmetic
     * label. Recycles [source]; the returned bitmap is always a new instance the caller must
     * recycle itself.
     */
    private fun recompress(source: Bitmap, quality: ExportQuality): Bitmap {
        val scaled = if (quality.downscale < 1f) {
            val width = (source.width * quality.downscale).toInt().coerceAtLeast(1)
            val height = (source.height * quality.downscale).toInt().coerceAtLeast(1)
            Bitmap.createScaledBitmap(source, width, height, true).also {
                if (it !== source) source.recycle()
            }
        } else {
            source
        }
        val encoded = ByteArrayOutputStream().use { buffer ->
            scaled.compress(Bitmap.CompressFormat.JPEG, quality.jpegQuality, buffer)
            buffer.toByteArray()
        }
        val recompressed = BitmapFactory.decodeByteArray(encoded, 0, encoded.size)
        return if (recompressed != null) {
            scaled.recycle()
            recompressed
        } else {
            scaled
        }
    }

    suspend fun writeJpeg(
        page: DocumentPage,
        destination: OutputStream,
        quality: ExportQuality = ExportQuality.BEST,
        watermarkText: String? = null,
        signature: SignatureStrokes = emptyList(),
    ): ToollyResult<Unit> = withContext(Dispatchers.IO) {
        val loadedBitmap = when (val loaded = loadBitmap(page.sourceAssetId)) {
            is ToollyResult.Success -> loaded.value
            is ToollyResult.Failure -> return@withContext loaded
        }
        val scaled = if (quality.downscale < 1f) {
            val width = (loadedBitmap.width * quality.downscale).toInt().coerceAtLeast(1)
            val height = (loadedBitmap.height * quality.downscale).toInt().coerceAtLeast(1)
            Bitmap.createScaledBitmap(loadedBitmap, width, height, true).also {
                if (it !== loadedBitmap) loadedBitmap.recycle()
            }
        } else {
            loadedBitmap
        }
        val bitmap = if (watermarkText.isNullOrBlank() && signature.isEmpty()) {
            scaled
        } else {
            val mutable = scaled.copy(Bitmap.Config.ARGB_8888, true)
            scaled.recycle()
            if (!watermarkText.isNullOrBlank()) {
                drawWatermark(Canvas(mutable), watermarkText, mutable.width.toFloat(), mutable.height.toFloat())
            }
            if (signature.isNotEmpty()) {
                drawSignature(Canvas(mutable), signature, mutable.width.toFloat(), mutable.height.toFloat())
            }
            mutable
        }
        try {
            coroutineContext.ensureActive()
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, quality.jpegQuality, destination)) {
                return@withContext retryableFailure()
            }
            destination.flush()
            ToollyResult.Success(Unit)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            retryableFailure()
        } finally {
            bitmap.recycle()
        }
    }

    /**
     * Draws [text] once, centered, rotated 45 degrees, at low opacity -- the common
     * "CONFIDENTIAL"/"DRAFT"-style diagonal stamp. Font size scales to the page so it reads at
     * roughly the same relative size regardless of source resolution.
     */
    private fun drawWatermark(canvas: Canvas, text: String, width: Float, height: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(WATERMARK_ALPHA, 0, 0, 0)
            textSize = width * WATERMARK_TEXT_SIZE_FRACTION
            textAlign = Paint.Align.CENTER
        }
        canvas.save()
        canvas.rotate(WATERMARK_ROTATION_DEGREES, width / 2f, height / 2f)
        canvas.drawText(text, width / 2f, height / 2f, paint)
        canvas.restore()
    }

    /**
     * Draws [strokes] (each stroke a list of 0..1-normalized points from [SignaturePadScreen][
     * com.toolly.shared.ui.SignaturePadScreen]) into a fixed anchor box in the bottom-right corner
     * of the page -- simple visual stamp, no identity verification or audit trail. The pad's own
     * 0..1 space is mapped directly onto the anchor box (stretched, not letterboxed); adequate for
     * a small signature where slight aspect distortion isn't noticeable.
     */
    private fun drawSignature(canvas: Canvas, strokes: SignatureStrokes, pageWidth: Float, pageHeight: Float) {
        val boxWidth = pageWidth * SIGNATURE_BOX_WIDTH_FRACTION
        val boxHeight = pageHeight * SIGNATURE_BOX_HEIGHT_FRACTION
        val left = pageWidth - boxWidth - PAGE_MARGIN_POINTS
        val top = pageHeight - boxHeight - PAGE_MARGIN_POINTS
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = SIGNATURE_STROKE_WIDTH
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        for (stroke in strokes) {
            if (stroke.size < 2) continue
            val path = android.graphics.Path()
            path.moveTo(left + stroke[0].x * boxWidth, top + stroke[0].y * boxHeight)
            for (index in 1 until stroke.size) {
                val point = stroke[index]
                path.lineTo(left + point.x * boxWidth, top + point.y * boxHeight)
            }
            canvas.drawPath(path, paint)
        }
    }

    private fun pageSize(bitmap: Bitmap): PdfPageSize =
        if (bitmap.width > bitmap.height) {
            PdfPageSize(width = A4_LONG_EDGE_POINTS, height = A4_SHORT_EDGE_POINTS)
        } else {
            PdfPageSize(width = A4_SHORT_EDGE_POINTS, height = A4_LONG_EDGE_POINTS)
        }

    private fun fittedDestination(
        bitmap: Bitmap,
        pageSize: PdfPageSize,
    ): RectF {
        val contentWidth = pageSize.width - 2f * PAGE_MARGIN_POINTS
        val contentHeight = pageSize.height - 2f * PAGE_MARGIN_POINTS
        val scale = minOf(contentWidth / bitmap.width, contentHeight / bitmap.height)
        val width = bitmap.width * scale
        val height = bitmap.height * scale
        val left = (pageSize.width - width) / 2f
        val top = (pageSize.height - height) / 2f
        return RectF(left, top, left + width, top + height)
    }

    private fun retryableFailure(): ToollyResult.Failure = ToollyResult.Failure(
        ToollyError(ToollyErrorCode.RETRYABLE, ToollyErrorCode.RETRYABLE.name),
    )

    private data class PdfPageSize(
        val width: Int,
        val height: Int,
    )

    private companion object {
        const val A4_SHORT_EDGE_POINTS = 595
        const val A4_LONG_EDGE_POINTS = 842
        const val PAGE_MARGIN_POINTS = 24f
        const val WATERMARK_ALPHA = 70
        const val WATERMARK_TEXT_SIZE_FRACTION = 0.09f
        const val WATERMARK_ROTATION_DEGREES = -45f
        const val SIGNATURE_BOX_WIDTH_FRACTION = 0.32f
        const val SIGNATURE_BOX_HEIGHT_FRACTION = 0.12f
        const val SIGNATURE_STROKE_WIDTH = 3f
    }
}
