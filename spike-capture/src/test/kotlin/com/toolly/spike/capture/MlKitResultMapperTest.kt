package com.toolly.spike.capture

import com.toolly.shared.capture.PartialCaptureReason
import com.toolly.shared.capture.ScanError
import com.toolly.shared.capture.ScanResult
import com.toolly.shared.capture.TemporaryAssetId
import com.toolly.spike.capture.mlkit.MlKitResultMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MlKitResultMapperTest {

    private val resultOk = -1
    private val resultCanceled = 0

    @Test
    fun `success maps validated temporary assets`() {
        val result = MlKitResultMapper.map(
            resultOk,
            listOf(asset("00000000000000000000000000000000")),
        ) as ScanResult.Success

        assertEquals(0, result.pages.single().index)
        assertEquals("00000000000000000000000000000000", result.pages.single().assetId.value)
    }

    @Test
    fun `cancelled and empty successful results map to Cancelled`() {
        assertTrue(MlKitResultMapper.map(resultCanceled, emptyList()) is ScanResult.Cancelled)
        assertTrue(MlKitResultMapper.map(resultOk, emptyList()) is ScanResult.Cancelled)
    }

    @Test
    fun `unexpected result code maps to InvalidResult`() {
        val result = MlKitResultMapper.map(42, emptyList()) as ScanResult.Failure
        assertTrue(result.error is ScanError.InvalidResult)
    }

    @Test
    fun `partial capture preserves only allowlisted reason`() {
        val result = MlKitResultMapper.mapPartialCapture(
            listOf(asset("11111111111111111111111111111111")),
            PartialCaptureReason.STORAGE_WRITE_FAILED,
        ) as ScanResult.Failure

        val error = result.error as ScanError.PartialCapture
        assertEquals(PartialCaptureReason.STORAGE_WRITE_FAILED, error.reason)
        assertEquals(1, error.capturedPages.size)
    }

    @Test
    fun `partial capture without assets maps to StorageFailure`() {
        val result = MlKitResultMapper.mapPartialCapture(
            emptyList(),
            PartialCaptureReason.SOURCE_READ_FAILED,
        ) as ScanResult.Failure
        assertTrue(result.error is ScanError.StorageFailure)
    }

    private fun asset(value: String) = TemporaryAssetId(value)
}
