package com.toolly.spike.capture

import com.toolly.spike.capture.domain.ScanError
import com.toolly.spike.capture.domain.ScanResult
import com.toolly.spike.capture.mlkit.MlKitResultMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [MlKitResultMapper].
 *
 * These tests run on the JVM without Android or ML Kit dependencies.
 * All inputs use constants rather than Android Activity.RESULT_OK / RESULT_CANCELED
 * to keep the tests self-contained.
 */
class MlKitResultMapperTest {

    // Mirror of Activity.RESULT_OK and Activity.RESULT_CANCELED
    private val resultOk = -1
    private val resultCanceled = 0

    @Test
    fun `success with pages maps to Success`() {
        val uris = listOf(
            "file:///data/user/0/com.toolly.spike.capture/cache/page0.jpg",
            "file:///data/user/0/com.toolly.spike.capture/cache/page1.jpg",
        )
        val result = MlKitResultMapper.map(resultOk, uris)

        assertTrue(result is ScanResult.Success)
        val success = result as ScanResult.Success
        assertEquals(2, success.pages.size)
        assertEquals(0, success.pages[0].index)
        assertEquals(1, success.pages[1].index)
    }

    @Test
    fun `cancelled result code maps to Cancelled`() {
        val result = MlKitResultMapper.map(resultCanceled, emptyList())
        assertTrue(result is ScanResult.Cancelled)
    }

    @Test
    fun `ok result with empty pages maps to Cancelled`() {
        val result = MlKitResultMapper.map(resultOk, emptyList())
        assertTrue(result is ScanResult.Cancelled)
    }

    @Test
    fun `unexpected result code maps to Failure Unknown`() {
        val result = MlKitResultMapper.map(42, emptyList())
        assertTrue(result is ScanResult.Failure)
        assertTrue((result as ScanResult.Failure).error is ScanError.Unknown)
    }

    @Test
    fun `mapServiceUnavailable returns Failure with ServiceUnavailable`() {
        val result = MlKitResultMapper.mapServiceUnavailable()
        assertTrue(result is ScanResult.Failure)
        assertTrue((result as ScanResult.Failure).error is ScanError.ServiceUnavailable)
    }

    @Test
    fun `mapPartialCapture with pages returns PartialCapture error`() {
        val uris = listOf("file:///data/user/0/com.toolly.spike.capture/cache/page0.jpg")
        val result = MlKitResultMapper.mapPartialCapture(uris, "Session interrupted")

        assertTrue(result is ScanResult.Failure)
        val error = (result as ScanResult.Failure).error
        assertTrue(error is ScanError.PartialCapture)
        val partial = error as ScanError.PartialCapture
        assertEquals(1, partial.capturedPages.size)
        assertEquals("Session interrupted", partial.cause)
    }

    @Test
    fun `mapPartialCapture with no pages returns Unknown error`() {
        val result = MlKitResultMapper.mapPartialCapture(emptyList(), "Aborted before capture")
        assertTrue(result is ScanResult.Failure)
        assertTrue((result as ScanResult.Failure).error is ScanError.Unknown)
    }

    @Test
    fun `page index sequence is zero-based and contiguous`() {
        val uris = listOf("uri://a", "uri://b", "uri://c")
        val result = MlKitResultMapper.map(resultOk, uris) as ScanResult.Success
        val indices = result.pages.map { it.index }
        assertEquals(listOf(0, 1, 2), indices)
    }

    @Test
    fun `imageUri is preserved in mapped page`() {
        val uri = "file:///data/user/0/com.toolly.spike.capture/cache/page0.jpg"
        val result = MlKitResultMapper.map(resultOk, listOf(uri)) as ScanResult.Success
        assertEquals(uri, result.pages[0].imageUri)
    }
}
