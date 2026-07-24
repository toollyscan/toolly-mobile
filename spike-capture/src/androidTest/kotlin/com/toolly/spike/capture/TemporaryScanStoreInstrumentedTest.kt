package com.toolly.spike.capture

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.toolly.spike.capture.mlkit.TemporaryScanStore
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TemporaryScanStoreInstrumentedTest {

    @Test
    fun importCopiesValidatedJpegAndCloseDeletesIt() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
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

    @Test
    fun rejectsNonJpegAndLeavesNoOwnedAsset() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val source = File(context.cacheDir, "synthetic-invalid.bin")
        source.writeBytes(byteArrayOf(0x01, 0x02, 0x03, 0x04))
        val store = TemporaryScanStore(context)

        val outcome = store.importPages(listOf(Uri.fromFile(source)))
        assertTrue(outcome is TemporaryScanStore.ImportOutcome.Failure)

        store.close()
        source.delete()
    }
}
