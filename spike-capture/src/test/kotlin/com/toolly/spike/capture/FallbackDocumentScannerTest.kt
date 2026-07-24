package com.toolly.spike.capture

import com.toolly.spike.capture.domain.DocumentScanner
import com.toolly.spike.capture.domain.FallbackDocumentScanner
import com.toolly.spike.capture.domain.ScanConfig
import com.toolly.spike.capture.domain.ScanError
import com.toolly.spike.capture.domain.ScanResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class FallbackDocumentScannerTest {

    @Test
    fun `service unavailable invokes fallback`() = runTest {
        var fallbackCalls = 0
        val expected = ScanResult.Cancelled
        val scanner = FallbackDocumentScanner(
            primary = fixed(ScanResult.Failure(ScanError.ServiceUnavailable)),
            fallback = object : DocumentScanner {
                override suspend fun launch(config: ScanConfig): ScanResult {
                    fallbackCalls += 1
                    return expected
                }
            },
        )

        assertSame(expected, scanner.launch(ScanConfig()))
        assertEquals(1, fallbackCalls)
    }

    @Test
    fun `non availability failures do not invoke fallback`() = runTest {
        var fallbackCalls = 0
        val expected = ScanResult.Failure(ScanError.StorageFailure)
        val scanner = FallbackDocumentScanner(
            primary = fixed(expected),
            fallback = object : DocumentScanner {
                override suspend fun launch(config: ScanConfig): ScanResult {
                    fallbackCalls += 1
                    return ScanResult.Cancelled
                }
            },
        )

        assertSame(expected, scanner.launch(ScanConfig()))
        assertEquals(0, fallbackCalls)
    }

    private fun fixed(result: ScanResult) = object : DocumentScanner {
        override suspend fun launch(config: ScanConfig): ScanResult = result
    }
}
