package com.toolly.shared.capture

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class CaptureContractsTest {
    @Test
    fun validConfigurationPreservesRequestedValues() {
        val config = ScanConfig(maxPages = 5, galleryImportEnabled = true)

        assertEquals(5, config.maxPages)
        assertEquals(true, config.galleryImportEnabled)
    }

    @Test
    fun galleryImportIsEnabledByDefault() {
        assertEquals(true, ScanConfig().galleryImportEnabled)
    }

    @Test
    fun pageLimitRejectsUnboundedValues() {
        assertFailsWith<IllegalArgumentException> { ScanConfig(maxPages = 0) }
        assertFailsWith<IllegalArgumentException> { ScanConfig(maxPages = 51) }
    }

    @Test
    fun temporaryAssetIdentifierAcceptsOnlyOpaqueToollyFormat() {
        val identifier = TemporaryAssetId("0123456789abcdef0123456789abcdef")

        assertEquals("0123456789abcdef0123456789abcdef", identifier.value)
        assertFailsWith<IllegalArgumentException> {
            TemporaryAssetId("content://provider/document")
        }
        assertFailsWith<IllegalArgumentException> {
            TemporaryAssetId("../document.jpg")
        }
    }

    @Test
    fun resultAndErrorRemainStructuredAndProviderNeutral() {
        val page = ScannedPage(
            index = 0,
            assetId = TemporaryAssetId("abcdef0123456789abcdef0123456789"),
        )
        val result = ScanResult.Failure(
            ScanError.PartialCapture(
                capturedPages = listOf(page),
                reason = PartialCaptureReason.SESSION_INTERRUPTED,
            ),
        )

        val failure = assertIs<ScanResult.Failure>(result)
        val partial = assertIs<ScanError.PartialCapture>(failure.error)
        assertEquals(listOf(page), partial.capturedPages)
        assertEquals(PartialCaptureReason.SESSION_INTERRUPTED, partial.reason)
    }
}
