package com.toolly.spike.capture

import com.toolly.spike.capture.camerax.CameraXDocumentScannerAdapter
import com.toolly.shared.capture.ScanConfig
import com.toolly.shared.capture.ScanError
import com.toolly.shared.capture.ScanResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [CameraXDocumentScannerAdapter].
 *
 * Verifies that the stub adapter behaves safely (no crash, no unchecked exception)
 * and returns the expected [ScanError.ServiceUnavailable] result for all configurations.
 *
 * These tests run on the JVM without Android or CameraX dependencies because the
 * stub adapter has no Android imports.
 */
class CameraXDocumentScannerAdapterTest {

    @Test
    fun `stub adapter returns Failure with ServiceUnavailable`() = runTest {
        val adapter = CameraXDocumentScannerAdapter()
        val result = adapter.launch(ScanConfig())

        assertTrue("Expected Failure", result is ScanResult.Failure)
        val error = (result as ScanResult.Failure).error
        assertTrue("Expected ServiceUnavailable", error is ScanError.ServiceUnavailable)
    }

    @Test
    fun `stub handles maxPages config without error`() = runTest {
        val adapter = CameraXDocumentScannerAdapter()
        val result = adapter.launch(ScanConfig(maxPages = 1))

        assertTrue("Expected Failure", result is ScanResult.Failure)
    }

    @Test
    fun `stub handles gallery import config without error`() = runTest {
        val adapter = CameraXDocumentScannerAdapter()
        val result = adapter.launch(ScanConfig(galleryImportEnabled = true))

        assertTrue("Expected Failure", result is ScanResult.Failure)
    }

    @Test
    fun `stub is consistent across multiple calls`() = runTest {
        val adapter = CameraXDocumentScannerAdapter()
        repeat(3) {
            val result = adapter.launch(ScanConfig())
            assertTrue("Expected Failure on call $it", result is ScanResult.Failure)
        }
    }
}
