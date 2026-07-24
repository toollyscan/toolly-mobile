package com.toolly.spike.capture

import android.net.Uri
import android.test.InstrumentationTestCase
import com.toolly.spike.capture.mlkit.TemporaryScanStore
import java.io.File

class TemporaryScanStoreInstrumentedTest : InstrumentationTestCase() {

    fun testImportCopiesValidatedJpegAndCloseDeletesIt() {
        val context = instrumentation.targetContext
        val source = File(context.cacheDir, "synthetic-source.jpg")
        source.writeBytes(
            byteArrayOf(
                0xFF.toByte(),
                0xD8.toByte(),
                0x00,
                0x00,
                0xFF.toByte(),
                0xD9.toByte(),
            ),
        )
        val store = TemporaryScanStore(context)

        val outcome = store.importPages(listOf(Uri.fromFile(source)))
        assertTrue(outcome is TemporaryScanStore.ImportOutcome.Success)
        val assetId = (outcome as TemporaryScanStore.ImportOutcome.Success).assetIds.single()
        val copied = store.resolve(assetId)
        assertNotNull(copied)
        assertTrue(copied!!.isFile)
        assertFalse(copied.absolutePath.contains(source.name))

        store.close()
        assertNull(store.resolve(assetId))
        source.delete()
    }

    fun testRejectsNonJpegAndLeavesNoOwnedAsset() {
        val context = instrumentation.targetContext
        val source = File(context.cacheDir, "synthetic-invalid.bin")
        source.writeBytes(byteArrayOf(0x01, 0x02, 0x03, 0x04))
        val store = TemporaryScanStore(context)

        val outcome = store.importPages(listOf(Uri.fromFile(source)))
        assertTrue(outcome is TemporaryScanStore.ImportOutcome.Failure)

        store.close()
        source.delete()
    }
}
