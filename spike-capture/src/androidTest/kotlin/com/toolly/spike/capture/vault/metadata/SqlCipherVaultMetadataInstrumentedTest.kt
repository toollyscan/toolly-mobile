package com.toolly.spike.capture.vault.metadata

import androidx.test.core.app.ApplicationProvider
import java.io.File
import java.util.UUID
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class SqlCipherVaultMetadataInstrumentedTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private lateinit var alias: String
    private lateinit var rootName: String
    private lateinit var protector: AndroidVaultPassphraseProtector
    private lateinit var factory: SqlCipherVaultMetadataFactory

    @Before
    fun setUp() {
        val suffix = UUID.randomUUID().toString()
        alias = "com.toolly.test.sqlcipher.$suffix"
        rootName = "toolly-sqlcipher-test-$suffix"
        protector = AndroidVaultPassphraseProtector(context, alias, rootName)
        factory = SqlCipherVaultMetadataFactory(context, protector, rootName)
    }

    @After
    fun tearDown() {
        protector.deleteKeyForTesting()
        File(context.noBackupFilesDir, rootName).deleteRecursively()
    }

    @Test
    fun encryptedMetadata_reopensWithoutPlaintextSqliteHeader() {
        val document = VaultDocumentEntity(
            documentId = "01JTESTDOCUMENT000000000001",
            createdAtEpochMillis = 1_000,
            updatedAtEpochMillis = 1_000,
            pageCount = 1,
            lifecycle = "ACTIVE",
            schemaVersion = 1,
        )
        val page = VaultPageEntity(
            pageId = "01JTESTPAGE0000000000000001",
            documentId = document.documentId,
            assetId = "01JTESTASSET000000000000001",
            ordinal = 0,
            widthPixels = 1200,
            heightPixels = 1600,
        )

        factory.open().use { database ->
            database.metadataDao().insertDocumentWithPages(document, listOf(page))
        }
        val databaseFile = factory.databaseFileForTesting()
        val header = ByteArray(16)
        databaseFile.inputStream().use { input ->
            assertEquals(header.size, input.read(header))
        }
        assertFalse(header.contentEquals("SQLite format 3\u0000".toByteArray(Charsets.US_ASCII)))

        factory.open().use { reopened ->
            val stored = reopened.metadataDao().getDocument(document.documentId)
            assertNotNull(stored)
            assertEquals(document, stored)
            assertEquals(listOf(page), reopened.metadataDao().getPages(document.documentId))
        }
    }
}
