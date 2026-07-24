package com.toolly.spike.capture

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.toolly.domain.contracts.SaveCapturedDocumentCommand
import com.toolly.domain.contracts.SaveCapturedPage
import com.toolly.domain.model.AssetId
import com.toolly.domain.model.DocumentId
import com.toolly.domain.model.OperationId
import com.toolly.domain.model.PageId
import com.toolly.domain.model.TemporaryAssetId
import com.toolly.foundation.ToollyResult
import com.toolly.spike.capture.vault.AppPrivateDocumentRepository
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppPrivateDocumentRepositoryInstrumentedTest {

    @Test
    fun committedDocumentReopensFromANewRepositoryInstance() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val testRoot = File(context.filesDir, "toolly-local-candidate-v1")
        testRoot.deleteRecursively()
        val source = File(context.cacheDir, "tly011-source.jpg")
        source.writeBytes(
            byteArrayOf(
                0xFF.toByte(),
                0xD8.toByte(),
                0x01,
                0x02,
                0xFF.toByte(),
                0xD9.toByte(),
            ),
        )
        val temporaryId = TemporaryAssetId("temporary-asset")
        val resolver = { id: String -> source.takeIf { id == temporaryId.value } }
        val documentId = DocumentId("00000000-0000-4000-8000-000000000011")
        val command = SaveCapturedDocumentCommand(
            operationId = OperationId("00000000-0000-4000-8000-000000000012"),
            documentId = documentId,
            createdAtEpochMillis = 100L,
            pages = listOf(
                SaveCapturedPage(
                    pageId = PageId("00000000-0000-4000-8000-000000000013"),
                    assetId = AssetId("00000000-0000-4000-8000-000000000014"),
                    temporaryAssetId = temporaryId,
                    ordinal = 0,
                    widthPixels = null,
                    heightPixels = null,
                ),
            ),
        )

        val first = AppPrivateDocumentRepository(context, resolver)
        assertTrue(first.saveCapturedDocument(command) is ToollyResult.Success)

        val reopened = AppPrivateDocumentRepository(context) { null }
        val details = reopened.getDocument(documentId)
        assertTrue(details is ToollyResult.Success)
        assertEquals(1, (details as ToollyResult.Success).value.pages.size)
        assertTrue(reopened.resolveAsset(command.pages.single().assetId)?.isFile == true)

        source.delete()
        testRoot.deleteRecursively()
    }
}
