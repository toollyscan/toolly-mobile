package com.toolly.spike.capture.export

import android.graphics.Bitmap
import com.toolly.domain.model.AssetId
import com.toolly.domain.model.DocumentDetails
import com.toolly.domain.model.DocumentId
import com.toolly.domain.model.DocumentLifecycle
import com.toolly.domain.model.DocumentPage
import com.toolly.domain.model.DocumentSummary
import com.toolly.domain.model.PageId
import com.toolly.foundation.ToollyError
import com.toolly.foundation.ToollyErrorCode
import com.toolly.foundation.ToollyResult
import java.io.ByteArrayOutputStream
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidDocumentExporterInstrumentedTest {
    @Test
    fun multiPagePdf_writesPlatformPdfAndRecyclesEachBitmap() = runBlocking {
        val recycled = mutableListOf<Bitmap>()
        val exporter = AndroidDocumentExporter(
            loadBitmap = {
                assertTrue(recycled.lastOrNull()?.isRecycled != false)
                ToollyResult.Success(
                    Bitmap.createBitmap(64, 96, Bitmap.Config.ARGB_8888).also(recycled::add),
                )
            },
        )
        val output = ByteArrayOutputStream()

        val result = exporter.writePdf(testDocument(pageCount = 2), output)

        assertTrue(result is ToollyResult.Success)
        assertArrayEquals(
            "%PDF-".toByteArray(),
            output.toByteArray().copyOfRange(0, 5),
        )
        assertTrue(recycled.all { it.isRecycled })
    }

    @Test
    fun jpegExport_reencodesPixelsWithoutSourceMetadata() = runBlocking {
        lateinit var source: Bitmap
        val exporter = AndroidDocumentExporter(
            loadBitmap = {
                ToollyResult.Success(
                    Bitmap.createBitmap(64, 96, Bitmap.Config.ARGB_8888).also { source = it },
                )
            },
        )
        val output = ByteArrayOutputStream()

        val result = exporter.writeJpeg(testDocument(1).pages.single(), output)

        assertTrue(result is ToollyResult.Success)
        val encoded = output.toByteArray()
        assertTrue(encoded.size > 4)
        assertArrayEquals(byteArrayOf(0xFF.toByte(), 0xD8.toByte()), encoded.copyOfRange(0, 2))
        assertArrayEquals(
            byteArrayOf(0xFF.toByte(), 0xD9.toByte()),
            encoded.copyOfRange(encoded.size - 2, encoded.size),
        )
        assertTrue(source.isRecycled)
    }

    @Test
    fun missingAuthenticatedBitmap_failsClosedWithoutPdfBytes() = runBlocking {
        val exporter = AndroidDocumentExporter(
            loadBitmap = { corruptFailure() },
        )
        val output = ByteArrayOutputStream()

        val result = exporter.writePdf(testDocument(1), output)

        assertTrue(result is ToollyResult.Failure)
        assertTrue(output.size() == 0)
    }

    private fun testDocument(pageCount: Int): DocumentDetails {
        val pages = List(pageCount) { index ->
            DocumentPage(
                id = PageId(UUID.randomUUID().toString()),
                sourceAssetId = AssetId(UUID.randomUUID().toString()),
                ordinal = index,
                widthPixels = null,
                heightPixels = null,
            )
        }
        return DocumentDetails(
            summary = DocumentSummary(
                id = DocumentId(UUID.randomUUID().toString()),
                pageCount = pageCount,
                createdAtEpochMillis = 1L,
                updatedAtEpochMillis = 1L,
                lifecycle = DocumentLifecycle.ACTIVE,
            ),
            pages = pages,
        )
    }

    private fun corruptFailure(): ToollyResult.Failure = ToollyResult.Failure(
        ToollyError(ToollyErrorCode.CORRUPT, ToollyErrorCode.CORRUPT.name),
    )
}
