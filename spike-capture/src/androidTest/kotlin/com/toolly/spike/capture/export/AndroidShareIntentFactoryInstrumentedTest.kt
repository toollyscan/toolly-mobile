package com.toolly.spike.capture.export

import android.content.Intent
import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidShareIntentFactoryInstrumentedTest {
    @Test
    fun singlePdf_usesReadOnlyContentGrant() {
        val uri = contentUri("document")

        val intent = AndroidShareIntentFactory.create(listOf(uri), PDF_MIME_TYPE)

        assertEquals(Intent.ACTION_SEND, intent.action)
        assertEquals(PDF_MIME_TYPE, intent.type)
        assertEquals(uri, intent.extras?.get(Intent.EXTRA_STREAM))
        assertEquals(uri, intent.clipData?.getItemAt(0)?.uri)
        assertTrue(intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
    }

    @Test
    fun multipleJpegs_useOneClipItemPerPage() {
        val uris = listOf(contentUri("page-one"), contentUri("page-two"))

        val intent = AndroidShareIntentFactory.create(uris, JPEG_MIME_TYPE)

        assertEquals(Intent.ACTION_SEND_MULTIPLE, intent.action)
        assertEquals(JPEG_MIME_TYPE, intent.type)
        assertEquals(2, intent.clipData?.itemCount)
        assertEquals(uris, intent.extras?.get(Intent.EXTRA_STREAM))
    }

    @Test
    fun nonContentOrEmptyPayload_isRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            AndroidShareIntentFactory.create(emptyList(), PDF_MIME_TYPE)
        }
        assertThrows(IllegalArgumentException::class.java) {
            AndroidShareIntentFactory.create(
                listOf(Uri.parse("file:///private/document.pdf")),
                PDF_MIME_TYPE,
            )
        }
    }

    private fun contentUri(id: String): Uri = Uri.parse("content://com.toolly.test/$id")

    private companion object {
        const val PDF_MIME_TYPE = "application/pdf"
        const val JPEG_MIME_TYPE = "image/jpeg"
    }
}
