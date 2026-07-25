package com.toolly.spike.capture.export

import android.graphics.Bitmap
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
import java.io.OutputStream
import java.util.concurrent.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

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
    ): ToollyResult<Unit> = withContext(Dispatchers.IO) {
        val pdf = PdfDocument()
        try {
            for (page in document.pages.sortedBy { it.ordinal }) {
                coroutineContext.ensureActive()
                val bitmap = when (val loaded = loadBitmap(page.sourceAssetId)) {
                    is ToollyResult.Success -> loaded.value
                    is ToollyResult.Failure -> return@withContext loaded
                }
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

    suspend fun writeJpeg(
        page: DocumentPage,
        destination: OutputStream,
    ): ToollyResult<Unit> = withContext(Dispatchers.IO) {
        val bitmap = when (val loaded = loadBitmap(page.sourceAssetId)) {
            is ToollyResult.Success -> loaded.value
            is ToollyResult.Failure -> return@withContext loaded
        }
        try {
            coroutineContext.ensureActive()
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, destination)) {
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
        const val JPEG_QUALITY = 95
    }
}
