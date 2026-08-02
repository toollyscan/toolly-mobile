package com.toolly.spike.capture

import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.toolly.domain.contracts.SaveCapturedDocumentCommand
import com.toolly.domain.contracts.SaveCapturedPage
import com.toolly.domain.model.AssetId
import com.toolly.domain.model.DocumentId
import com.toolly.domain.model.OperationId
import com.toolly.domain.model.PageId
import com.toolly.domain.model.TemporaryAssetId
import com.toolly.foundation.ToollyErrorCode
import com.toolly.foundation.ToollyResult
import com.toolly.spike.capture.vault.EncryptedDocumentRepository
import com.toolly.spike.capture.vault.VaultSaveStage
import com.toolly.spike.capture.vault.crypto.AndroidAssetCipher
import com.toolly.spike.capture.vault.crypto.AndroidMetadataCipher
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.RandomAccessFile
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EncryptedDocumentRepositoryInstrumentedTest {
    @Test
    fun savedDocumentReopensAndRejectsCiphertextTamper() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val suffix = UUID.randomUUID().toString()
        val rootName = "toolly-test-vault-$suffix"
        val legacyName = "toolly-test-legacy-$suffix"
        val metadataAlias = "com.toolly.test.metadata.$suffix"
        val assetAlias = "com.toolly.test.asset.$suffix"
        val metadataCipher = AndroidMetadataCipher(metadataAlias)
        val assetCipher = AndroidAssetCipher(assetAlias)
        val jpeg = testJpeg()
        val source = File(context.cacheDir, "$suffix.jpg").apply { writeBytes(jpeg) }
        val command = testCommand()
        val resolver = { rawId: String ->
            source.takeIf { rawId == command.pages.single().temporaryAssetId.value }
        }

        try {
            val first = EncryptedDocumentRepository(
                context = context,
                resolveTemporaryAsset = resolver,
                metadataCipher = metadataCipher,
                assetCipher = assetCipher,
                rootDirectoryName = rootName,
                legacyRootDirectoryName = legacyName,
            )
            assertTrue(first.saveCapturedDocument(command) is ToollyResult.Success)

            val encryptedRoot = File(context.noBackupFilesDir, rootName)
            val persistentBytes = ByteArrayOutputStream().use { output ->
                encryptedRoot.walkTopDown()
                    .filter(File::isFile)
                    .forEach { output.write(it.readBytes()) }
                output.toByteArray()
            }
            val plaintextProbe = jpeg.copyOfRange(jpeg.size / 2, jpeg.size / 2 + 512)
            assertFalse(persistentBytes.containsSubsequence(plaintextProbe))

            val reopened = EncryptedDocumentRepository(
                context = context,
                resolveTemporaryAsset = { null },
                metadataCipher = AndroidMetadataCipher(metadataAlias),
                assetCipher = AndroidAssetCipher(assetAlias),
                rootDirectoryName = rootName,
                legacyRootDirectoryName = legacyName,
            )
            val details = reopened.getDocument(command.documentId)
            assertTrue(details is ToollyResult.Success)
            assertEquals(1, (details as ToollyResult.Success).value.pages.size)

            val encryptedAsset = encryptedRoot.walkTopDown()
                .first { it.isFile && it.extension == "tlya" }
            RandomAccessFile(encryptedAsset, "rw").use {
                it.seek(it.length() - 1)
                val value = it.read()
                it.seek(it.length() - 1)
                it.write(value.xor(1))
            }
            assertTrue(reopened.getDocument(command.documentId) is ToollyResult.Failure)
        } finally {
            metadataCipher.deleteWrappingKeyForTesting()
            assetCipher.deleteWrappingKeyForTesting()
            File(context.noBackupFilesDir, rootName).deleteRecursively()
            File(context.filesDir, legacyName).deleteRecursively()
            source.delete()
        }
    }

    @Test
    fun legacyPlaintext_isDeletedOnlyAfterEncryptedReopenSucceeds() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val suffix = UUID.randomUUID().toString()
        val rootName = "toolly-test-vault-$suffix"
        val legacyName = "toolly-test-legacy-$suffix"
        val metadataCipher = AndroidMetadataCipher("com.toolly.test.metadata.$suffix")
        val assetCipher = AndroidAssetCipher("com.toolly.test.asset.$suffix")
        val command = testCommand()
        val legacyDocument = File(
            context.filesDir,
            "$legacyName/documents/${command.documentId.value}",
        )
        check(legacyDocument.mkdirs())
        File(legacyDocument, "${command.pages.single().assetId.value}.jpg").writeBytes(testJpeg())
        File(legacyDocument, "manifest.json").writeText(legacyManifest(command))
        File(legacyDocument, "COMMITTED").writeBytes(byteArrayOf(1))

        try {
            val repository = EncryptedDocumentRepository(
                context = context,
                resolveTemporaryAsset = { null },
                metadataCipher = metadataCipher,
                assetCipher = assetCipher,
                rootDirectoryName = rootName,
                legacyRootDirectoryName = legacyName,
            )
            assertTrue(repository.getDocument(command.documentId) is ToollyResult.Success)
            assertFalse(legacyDocument.exists())
            assertTrue(
                File(
                    context.noBackupFilesDir,
                    "$rootName/documents/${command.documentId.value}/COMMITTED",
                ).isFile,
            )
        } finally {
            metadataCipher.deleteWrappingKeyForTesting()
            assetCipher.deleteWrappingKeyForTesting()
            File(context.noBackupFilesDir, rootName).deleteRecursively()
            File(context.filesDir, legacyName).deleteRecursively()
        }
    }

    @Test
    fun invalidJpeg_reportsAllowlistedStageAndPublishesNothing() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val suffix = UUID.randomUUID().toString()
        val rootName = "toolly-test-vault-$suffix"
        val legacyName = "toolly-test-legacy-$suffix"
        val metadataCipher = AndroidMetadataCipher("com.toolly.test.metadata.$suffix")
        val assetCipher = AndroidAssetCipher("com.toolly.test.asset.$suffix")
        val source = File(context.cacheDir, "$suffix.jpg").apply {
            writeBytes(byteArrayOf(1, 2, 3, 4))
        }
        val command = testCommand()

        try {
            val repository = EncryptedDocumentRepository(
                context = context,
                resolveTemporaryAsset = { source },
                metadataCipher = metadataCipher,
                assetCipher = assetCipher,
                rootDirectoryName = rootName,
                legacyRootDirectoryName = legacyName,
            )
            val result = repository.saveCapturedDocument(command)

            assertTrue(result is ToollyResult.Failure)
            val failure = result as ToollyResult.Failure
            assertEquals(ToollyErrorCode.RETRYABLE, failure.error.code)
            assertEquals(VaultSaveStage.VALIDATE_SOURCE.name, failure.error.safeMessage)
            assertFalse(
                File(context.noBackupFilesDir, "$rootName/documents")
                    .walkTopDown()
                    .any { it.name == "COMMITTED" },
            )
        } finally {
            metadataCipher.deleteWrappingKeyForTesting()
            assetCipher.deleteWrappingKeyForTesting()
            File(context.noBackupFilesDir, rootName).deleteRecursively()
            File(context.filesDir, legacyName).deleteRecursively()
            source.delete()
        }
    }

    @Test
    fun missingVaultScope_withCommittedDocument_failsClosed() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val suffix = UUID.randomUUID().toString()
        val rootName = "toolly-test-vault-$suffix"
        val legacyName = "toolly-test-legacy-$suffix"
        val metadataCipher = AndroidMetadataCipher("com.toolly.test.metadata.$suffix")
        val assetCipher = AndroidAssetCipher("com.toolly.test.asset.$suffix")
        val source = File(context.cacheDir, "$suffix.jpg").apply { writeBytes(testJpeg()) }
        val command = testCommand()

        try {
            val repository = EncryptedDocumentRepository(
                context = context,
                resolveTemporaryAsset = { source },
                metadataCipher = metadataCipher,
                assetCipher = assetCipher,
                rootDirectoryName = rootName,
                legacyRootDirectoryName = legacyName,
            )
            assertTrue(repository.saveCapturedDocument(command) is ToollyResult.Success)
            assertTrue(File(context.noBackupFilesDir, "$rootName/vault.scope").delete())

            assertThrows(IllegalStateException::class.java) {
                EncryptedDocumentRepository(
                    context = context,
                    resolveTemporaryAsset = { null },
                    metadataCipher = metadataCipher,
                    assetCipher = assetCipher,
                    rootDirectoryName = rootName,
                    legacyRootDirectoryName = legacyName,
                )
            }
        } finally {
            metadataCipher.deleteWrappingKeyForTesting()
            assetCipher.deleteWrappingKeyForTesting()
            File(context.noBackupFilesDir, rootName).deleteRecursively()
            File(context.filesDir, legacyName).deleteRecursively()
            source.delete()
        }
    }

    private fun testCommand(): SaveCapturedDocumentCommand {
        val temporaryId = TemporaryAssetId("temporary-${UUID.randomUUID()}")
        return SaveCapturedDocumentCommand(
            operationId = OperationId(UUID.randomUUID().toString()),
            documentId = DocumentId(UUID.randomUUID().toString()),
            createdAtEpochMillis = 100L,
            pages = listOf(
                SaveCapturedPage(
                    pageId = PageId(UUID.randomUUID().toString()),
                    assetId = AssetId(UUID.randomUUID().toString()),
                    temporaryAssetId = temporaryId,
                    ordinal = 0,
                    widthPixels = null,
                    heightPixels = null,
                ),
            ),
        )
    }

    private fun legacyManifest(command: SaveCapturedDocumentCommand): String {
        val page = command.pages.single()
        return JSONObject()
            .put("schemaVersion", 1)
            .put("documentId", command.documentId.value)
            .put("createdAtEpochMillis", command.createdAtEpochMillis)
            .put("updatedAtEpochMillis", command.createdAtEpochMillis)
            .put(
                "pages",
                JSONArray().put(
                    JSONObject()
                        .put("pageId", page.pageId.value)
                        .put("assetId", page.assetId.value)
                        .put("ordinal", page.ordinal)
                        .put("widthPixels", JSONObject.NULL)
                        .put("heightPixels", JSONObject.NULL),
                ),
            )
            .toString()
    }

    private fun ByteArray.containsSubsequence(candidate: ByteArray): Boolean {
        if (candidate.isEmpty()) return true
        if (candidate.size > size) return false
        for (start in 0..size - candidate.size) {
            var matches = true
            for (offset in candidate.indices) {
                if (this[start + offset] != candidate[offset]) {
                    matches = false
                    break
                }
            }
            if (matches) return true
        }
        return false
    }

    private fun testJpeg(): ByteArray {
        val width = 1024
        val height = 1536
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        return try {
            val pixels = IntArray(width * height) { index ->
                val value = index * 1103515245 + 12345
                Color.rgb(
                    value ushr 16 and 0xFF,
                    value ushr 8 and 0xFF,
                    value and 0xFF,
                )
            }
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            ByteArrayOutputStream().use { output ->
                check(bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output))
                output.toByteArray().also { encoded ->
                    check(encoded.size > 256 * 1024)
                }
            }
        } finally {
            bitmap.recycle()
        }
    }
}
