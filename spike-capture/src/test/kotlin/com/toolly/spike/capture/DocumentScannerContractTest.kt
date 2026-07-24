package com.toolly.spike.capture

import com.toolly.spike.capture.domain.DocumentScanner
import com.toolly.spike.capture.domain.PartialCaptureReason
import com.toolly.spike.capture.domain.ScanConfig
import com.toolly.spike.capture.domain.ScanError
import com.toolly.spike.capture.domain.ScanResult
import com.toolly.spike.capture.domain.ScannedPage
import com.toolly.spike.capture.domain.TemporaryAssetId
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentScannerContractTest {

    @Test
    fun `successful capture exposes only Toolly temporary asset identifiers`() = runTest {
        val pages = listOf(
            ScannedPage(0, asset("00000000000000000000000000000000")),
            ScannedPage(1, asset("11111111111111111111111111111111")),
        )
        val result = FakeDocumentScanner(ScanResult.Success(pages)).launch(ScanConfig())

        assertTrue(result is ScanResult.Success)
        assertEquals(2, (result as ScanResult.Success).pages.size)
    }

    @Test
    fun `partial capture uses allowlisted reason`() = runTest {
        val partial = ScanError.PartialCapture(
            capturedPages = listOf(
                ScannedPage(0, asset("22222222222222222222222222222222")),
            ),
            reason = PartialCaptureReason.SOURCE_READ_FAILED,
        )
        val result = FakeDocumentScanner(ScanResult.Failure(partial)).launch(ScanConfig())

        assertEquals(
            PartialCaptureReason.SOURCE_READ_FAILED,
            ((result as ScanResult.Failure).error as ScanError.PartialCapture).reason,
        )
    }

    @Test
    fun `config is passed through without modification`() = runTest {
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

    @Test
    fun `maxPages rejects values outside bounded invariant`() {
        assertThrows(IllegalArgumentException::class.java) { ScanConfig(maxPages = 0) }
        assertThrows(IllegalArgumentException::class.java) { ScanConfig(maxPages = 51) }
    }

    @Test
    fun `temporary asset identifiers reject paths and provider URIs`() {
        assertThrows(IllegalArgumentException::class.java) {
            TemporaryAssetId("content://provider/page")
        }
        assertThrows(IllegalArgumentException::class.java) {
            TemporaryAssetId("../page.jpg")
        }
    }

    private fun asset(value: String) = TemporaryAssetId(value)
}

private class FakeDocumentScanner(private val result: ScanResult) : DocumentScanner {
    override suspend fun launch(config: ScanConfig): ScanResult = result
}
