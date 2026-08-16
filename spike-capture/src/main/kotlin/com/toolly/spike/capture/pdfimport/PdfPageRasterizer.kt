package com.toolly.spike.capture.pdfimport

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor

/**
 * Rasterizes an existing PDF file (picked via the system document picker, never a Toolly-built
 * scanner UI) into one [Bitmap] per page, so an imported PDF can be treated exactly like a live
 * capture from that point on -- fed into [com.toolly.spike.capture.mlkit.TemporaryScanStore] and
 * the same review/crop/enhance/save pipeline every other page already goes through.
 *
 * Uses the platform's own `android.graphics.pdf.PdfRenderer` -- no third-party PDF library, no new
 * dependency. Rendered at [RENDER_SCALE]x the page's declared point size (72pt/inch) so imported
 * pages come out at a comparable resolution to a phone camera capture rather than a blurry 72dpi
 * render.
 */
internal class PdfPageRasterizer(context: Context) {

    private val appContext = context.applicationContext

    /** Returns null if [uri] can't be opened or isn't a valid PDF; empty only for a 0-page file. */
    fun rasterize(uri: Uri, maxPages: Int): RasterizeResult? {
        val descriptor = try {
            appContext.contentResolver.openFileDescriptor(uri, "r")
        } catch (_: SecurityException) {
            null
        } catch (_: java.io.FileNotFoundException) {
            null
        } ?: return null

        return descriptor.use { pfd -> renderAllPages(pfd, maxPages) }
    }

    private fun renderAllPages(pfd: ParcelFileDescriptor, maxPages: Int): RasterizeResult? {
        val renderer = try {
            PdfRenderer(pfd)
        } catch (_: java.io.IOException) {
            return null
        } catch (_: SecurityException) {
            return null
        }
        return renderer.use { open ->
            if (open.pageCount > maxPages) return RasterizeResult.TooManyPages(open.pageCount)
            val bitmaps = ArrayList<Bitmap>(open.pageCount)
            try {
                for (index in 0 until open.pageCount) {
                    open.openPage(index).use { page ->
                        val width = (page.width * RENDER_SCALE).toInt().coerceAtLeast(1)
                        val height = (page.height * RENDER_SCALE).toInt().coerceAtLeast(1)
                        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        bitmap.eraseColor(Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        bitmaps += bitmap
                    }
                }
                RasterizeResult.Pages(bitmaps)
            } catch (failure: RuntimeException) {
                bitmaps.forEach { it.recycle() }
                null
            }
        }
    }

    sealed interface RasterizeResult {
        data class Pages(val bitmaps: List<Bitmap>) : RasterizeResult
        data class TooManyPages(val actualPageCount: Int) : RasterizeResult
    }

    private companion object {
        const val RENDER_SCALE = 2f
    }
}
