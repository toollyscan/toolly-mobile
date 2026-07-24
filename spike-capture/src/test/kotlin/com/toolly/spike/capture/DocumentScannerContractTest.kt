package com.toolly.spike.capture

import com.toolly.spike.capture.domain.DocumentScanner
import com.toolly.spike.capture.domain.ScanConfig
import com.toolly.spike.capture.domain.ScanError
import com.toolly.spike.capture.domain.ScanResult
import com.toolly.spike.capture.domain.ScannedPage
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Contract tests for [DocumentScanner] implementations.
 *
 * These tests verify that every [DocumentScanner] implementation honours the expected
 * result types for each logical outcome. They use a [FakeDocumentScanner] to validate
 * the contract in isolation.
 *
 * Each real adapter (ML Kit, CameraX) must satisfy the same contract verified here.
 */
class DocumentScannerContractTest {

    @Test
    fun `cancelled session returns Cancelled`() = runTest {
        val scanner = FakeDocumentScanner(ScanResult.Cancelled)
        val result = scanner.launch(ScanConfig())
        assertTrue("Expected Cancelled", result is ScanResult.Cancelled)
    }

    @Test
    fun `successful capture returns Success with correct page count`() = runTest {
        val pages = listOf(
            ScannedPage(0, "file:///tmp/spike/page0.jpg"),
            ScannedPage(1, "file:///tmp/spike/page1.jpg"),
        )
        val scanner = FakeDocumentScanner(ScanResult.Success(pages))
        val result = scanner.launch(ScanConfig())

        assertTrue("Expected Success", result is ScanResult.Success)
        assertEquals(2, (result as ScanResult.Success).pages.size)
    }

    @Test
    fun `service unavailable returns Failure with ServiceUnavailable error`() = runTest {
        val scanner = FakeDocumentScanner(ScanResult.Failure(ScanError.ServiceUnavailable))
        val result = scanner.launch(ScanConfig())

        assertTrue("Expected Failure", result is ScanResult.Failure)
        val error = (result as ScanResult.Failure).error
        assertTrue("Expected ServiceUnavailable", error is ScanError.ServiceUnavailable)
    }

    @Test
    fun `partial capture returns Failure with PartialCapture error and non-empty pages`() = runTest {
        val capturedPages = listOf(ScannedPage(0, "file:///tmp/spike/page0.jpg"))
        val error = ScanError.PartialCapture(capturedPages, "Scan interrupted by user")
        val scanner = FakeDocumentScanner(ScanResult.Failure(error))
        val result = scanner.launch(ScanConfig())

        assertTrue("Expected Failure", result is ScanResult.Failure)
        val resultError = (result as ScanResult.Failure).error
        assertTrue("Expected PartialCapture", resultError is ScanError.PartialCapture)
        val partial = resultError as ScanError.PartialCapture
        assertEquals(1, partial.capturedPages.size)
        assertEquals("Scan interrupted by user", partial.cause)
    }

    @Test
    fun `permission denied returns Failure with PermissionDenied error`() = runTest {
        val scanner = FakeDocumentScanner(ScanResult.Failure(ScanError.PermissionDenied))
        val result = scanner.launch(ScanConfig())

        assertTrue("Expected Failure", result is ScanResult.Failure)
        val error = (result as ScanResult.Failure).error
        assertTrue("Expected PermissionDenied", error is ScanError.PermissionDenied)
    }

    @Test
    fun `unknown error returns Failure with non-blank cause`() = runTest {
        val scanner = FakeDocumentScanner(ScanResult.Failure(ScanError.Unknown("unexpected condition")))
        val result = scanner.launch(ScanConfig())

        assertTrue("Expected Failure", result is ScanResult.Failure)
        val error = (result as ScanResult.Failure).error
        assertTrue("Expected Unknown", error is ScanError.Unknown)
        val unknown = error as ScanError.Unknown
        assertTrue("Cause must not be blank", unknown.cause.isNotBlank())
    }

    @Test
    fun `config maxPages is passed through without modification`() = runTest {
        var receivedConfig: ScanConfig? = null
        val scanner = object : DocumentScanner {
            override suspend fun launch(config: ScanConfig): ScanResult {
                receivedConfig = config
                return ScanResult.Cancelled
            }
        }
        val config = ScanConfig(maxPages = 5, galleryImportEnabled = true)
        scanner.launch(config)

        assertEquals(config, receivedConfig)
    }
}

/** Test double that returns a predetermined [ScanResult]. */
private class FakeDocumentScanner(private val result: ScanResult) : DocumentScanner {
    override suspend fun launch(config: ScanConfig): ScanResult = result
}
