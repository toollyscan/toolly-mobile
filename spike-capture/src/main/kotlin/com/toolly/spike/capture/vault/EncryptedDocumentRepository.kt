package com.toolly.spike.capture.vault

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.toolly.domain.contracts.DocumentRepository
import com.toolly.domain.contracts.SaveCapturedDocumentCommand
import com.toolly.domain.model.AssetId
import com.toolly.domain.model.DocumentCategory
import com.toolly.domain.model.DocumentDetails
import com.toolly.domain.model.DocumentId
import com.toolly.domain.model.DocumentLifecycle
import com.toolly.domain.model.DocumentPage
import com.toolly.domain.model.DocumentSummary
import com.toolly.domain.model.MAX_DISPLAY_NAME_LENGTH
import com.toolly.domain.model.PageId
import com.toolly.foundation.ToollyError
import com.toolly.foundation.ToollyErrorCode
import com.toolly.foundation.ToollyResult
import com.toolly.shared.edit.CropRegion
import com.toolly.shared.edit.EnhancementMode
import com.toolly.shared.edit.PageEditError
import com.toolly.shared.edit.PageEditResult
import com.toolly.shared.edit.PixelBuffer
import com.toolly.spike.capture.vault.crypto.AndroidAssetCipher
import com.toolly.spike.capture.vault.crypto.AndroidMetadataCipher
import com.toolly.spike.capture.vault.crypto.AssetAssociatedData
import com.toolly.spike.capture.vault.crypto.AssetObjectKind
import com.toolly.spike.capture.vault.crypto.MetadataAssociatedData
import com.toolly.spike.capture.vault.crypto.RecordKind
import com.toolly.spike.capture.vault.crypto.VaultCryptoException
import com.toolly.spike.capture.vault.edit.AndroidPageEditor
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.RandomAccessFile
import java.util.UUID
import java.util.concurrent.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/** Allowlisted, content-free save diagnostics for device validation. */
internal enum class VaultSaveStage {
    PREPARE_TRANSACTION,
    RESOLVE_SOURCE,
    VALIDATE_SOURCE,
    ENCRYPT_ASSET,
    VERIFY_ASSET,
    WRITE_MANIFEST,
    WRITE_COMMIT_MARKER,
    PROMOTE_TRANSACTION,
    VERIFY_COMMITTED_DOCUMENT,
}

/**
 * Android encrypted repository candidate behind Toolly's platform-neutral document contract.
 *
 * Persistent metadata and assets are encrypted before publication. Plaintext exists only in the
 * bounded scanner/import staging owned by [resolveTemporaryAsset] and in bounded decode memory.
 */
internal class EncryptedDocumentRepository(
    context: Context,
    private val resolveTemporaryAsset: (String) -> File?,
    private val metadataCipher: AndroidMetadataCipher = AndroidMetadataCipher(),
    private val assetCipher: AndroidAssetCipher = AndroidAssetCipher(),
    rootDirectoryName: String = ROOT_DIRECTORY,
    legacyRootDirectoryName: String = LEGACY_ROOT_DIRECTORY,
) : DocumentRepository {

    private val applicationContext = context.applicationContext
    private val root = File(applicationContext.noBackupFilesDir, rootDirectoryName)
    private val staging = File(root, STAGING_DIRECTORY)
    private val documents = File(root, DOCUMENTS_DIRECTORY)
    private val legacyRoot = File(applicationContext.filesDir, legacyRootDirectoryName)
    private val lock = Any()
    private val vaultScopeId: String
    private var legacyMigrationBlocked = false

    // Only replacePageWithEdit's applyToBuffer() call is used -- resolveAsset is irrelevant here
    // (the source is decrypted straight to memory below, never staged as a file), so it's a no-op.
    private val pageEditor = AndroidPageEditor(applicationContext) { null }

    init {
        check(root.mkdirs() || root.isDirectory)
        check(staging.mkdirs() || staging.isDirectory)
        check(documents.mkdirs() || documents.isDirectory)
        vaultScopeId = loadOrCreateVaultScope()
        recoverInterruptedWrites()
        migrateLegacyDocuments()
    }

    override suspend fun listDocuments(): ToollyResult<List<DocumentSummary>> =
        withContext(Dispatchers.IO) {
            synchronized(lock) {
                if (legacyMigrationBlocked) return@withContext migrationFailure()
                runSafely {
                    documents.listFiles()
                        .orEmpty()
                        .asSequence()
                        .filter { it.isDirectory && File(it, COMMITTED_MARKER).isFile }
                        .map { directory -> readDocument(directory).getOrThrow().summary }
                        .sortedByDescending { it.updatedAtEpochMillis }
                        .toList()
                }
            }
        }

    override suspend fun getDocument(
        documentId: DocumentId,
    ): ToollyResult<DocumentDetails> = withContext(Dispatchers.IO) {
        synchronized(lock) {
            val directory = documentDirectory(documentId)
            if (!directory.isDirectory || !File(directory, COMMITTED_MARKER).isFile) {
                return@withContext unavailableFailure()
            }
            readDocument(directory).fold(
                onSuccess = { ToollyResult.Success(it) },
                onFailure = { failure -> cryptoOrCorruptFailure(failure) },
            )
        }
    }

    override suspend fun saveCapturedDocument(
        command: SaveCapturedDocumentCommand,
    ): ToollyResult<DocumentDetails> = withContext(Dispatchers.IO) {
        synchronized(lock) {
            if (legacyMigrationBlocked) return@withContext migrationFailure()
            val destination = documentDirectory(command.documentId)
            if (destination.isDirectory && File(destination, COMMITTED_MARKER).isFile) {
                return@withContext readDocument(destination).fold(
                    onSuccess = { ToollyResult.Success(it) },
                    onFailure = { failure ->
                        failure.toToollyFailure(VaultSaveStage.VERIFY_COMMITTED_DOCUMENT)
                    },
                )
            }

            var saveStage = VaultSaveStage.PREPARE_TRANSACTION
            val transaction = File(staging, command.operationId.value)
            transaction.deleteRecursivelySafely()
            if (!transaction.mkdirs()) return@withContext storageFailure(saveStage)

            try {
                for (page in command.pages.sortedBy { it.ordinal }) {
                    saveStage = VaultSaveStage.RESOLVE_SOURCE
                    val source = resolveTemporaryAsset(page.temporaryAssetId.value)
                        ?: throw IOException()
                    saveStage = VaultSaveStage.VALIDATE_SOURCE
                    requireCompleteJpeg(source)
                    val target = File(transaction, assetFileName(page.assetId))
                    val associatedData = assetAssociatedData(page.assetId)
                    saveStage = VaultSaveStage.ENCRYPT_ASSET
                    assetCipher.encrypt(source, target, associatedData)
                    saveStage = VaultSaveStage.VERIFY_ASSET
                    assetCipher.verify(target, associatedData)
                }
                saveStage = VaultSaveStage.WRITE_MANIFEST
                writeManifest(transaction, manifestFrom(command))
                saveStage = VaultSaveStage.WRITE_COMMIT_MARKER
                writeAndSync(File(transaction, COMMITTED_MARKER), MARKER_BYTES)

                saveStage = VaultSaveStage.PROMOTE_TRANSACTION
                if (!transaction.renameTo(destination)) throw IOException()
                saveStage = VaultSaveStage.VERIFY_COMMITTED_DOCUMENT
                readDocument(destination).fold(
                    onSuccess = { ToollyResult.Success(it) },
                    onFailure = { failure ->
                        destination.deleteRecursivelySafely()
                        failure.toToollyFailure(saveStage)
                    },
                )
            } catch (cancelled: CancellationException) {
                transaction.deleteRecursivelySafely()
                throw cancelled
            } catch (failure: Exception) {
                transaction.deleteRecursivelySafely()
                failure.toToollyFailure(saveStage)
            }
        }
    }

    override suspend fun renameDocument(
        documentId: DocumentId,
        displayName: String?,
        updatedAtEpochMillis: Long,
    ): ToollyResult<DocumentDetails> = updateMetadata(documentId) { existing ->
        require(displayName == null || displayName.isNotBlank())
        require(displayName == null || displayName.length <= MAX_DISPLAY_NAME_LENGTH)
        existing.copy(displayName = displayName, updatedAtEpochMillis = updatedAtEpochMillis)
    }

    override suspend fun tagDocument(
        documentId: DocumentId,
        category: DocumentCategory?,
        updatedAtEpochMillis: Long,
    ): ToollyResult<DocumentDetails> = updateMetadata(documentId) { existing ->
        existing.copy(category = category, updatedAtEpochMillis = updatedAtEpochMillis)
    }

    /**
     * Re-crops/enhances one page of an already-saved document and re-persists the result, per
     * wireframe `3.1 Manual corners`'s "fix a page after the fact" intent -- distinct from, and
     * deliberately not wired into, the live ML Kit capture path (issue #52: "without copying
     * Google scanner UI"; ML Kit's own scanner already handles crop for capture and gallery
     * import). [pageId] must belong to [documentId].
     *
     * The source page is decrypted straight to memory (same plaintext-stays-in-memory boundary
     * `loadAssetBitmap` already uses -- never staged as a plaintext file), warped/adjusted with
     * the shared `commonMain` pixel math via [AndroidPageEditor.applyToBuffer], then its JPEG
     * output (staged only in that class's own bounded cache directory, mirroring how newly
     * captured pages are staged before their first encryption) is encrypted into the vault under
     * a *new* [AssetId] -- the original encrypted asset file is never overwritten in place, so a
     * crash mid-write can only orphan a stray file, never corrupt a readable one. The manifest
     * swap itself reuses [updateCommittedManifest]'s same decrypt-verify-in-memory-before-disk,
     * same-directory-atomic-rename discipline. The old asset file is deleted only after the
     * manifest promotion that stops referencing it succeeds, and failure to delete it is not
     * treated as a save failure -- it would just be wasted ciphertext, never a correctness issue.
     */
    suspend fun replacePageWithEdit(
        documentId: DocumentId,
        pageId: PageId,
        crop: CropRegion?,
        mode: EnhancementMode,
        intensity: Float,
        updatedAtEpochMillis: Long,
    ): ToollyResult<DocumentDetails> = withContext(Dispatchers.IO) {
        synchronized(lock) {
            if (legacyMigrationBlocked) return@withContext migrationFailure()
            val destination = documentDirectory(documentId)
            if (!destination.isDirectory || !File(destination, COMMITTED_MARKER).isFile) {
                return@withContext unavailableFailure()
            }
            runSafely {
                val manifestFile = File(destination, MANIFEST_FILE)
                if (!manifestFile.isFile || manifestFile.length() !in 1..MAX_MANIFEST_ENVELOPE_BYTES) {
                    throw IOException()
                }
                val existingPlaintext = metadataCipher.decrypt(
                    manifestFile.readBytes(),
                    metadataAssociatedData(documentId),
                )
                val existingRecord = try {
                    parseManifestPlaintext(existingPlaintext, documentId)
                } finally {
                    existingPlaintext.fill(0)
                }
                require(updatedAtEpochMillis >= existingRecord.createdAtEpochMillis)
                val existingPage = existingRecord.pages.find { it.pageId == pageId } ?: throw IOException()
                val oldAssetFile = File(destination, assetFileName(existingPage.assetId))
                val oldAssociatedData = assetAssociatedData(existingPage.assetId)
                assetCipher.verify(oldAssetFile, oldAssociatedData)

                val sourceBuffer = assetCipher.openDecrypted(oldAssetFile, oldAssociatedData).use { input ->
                    val bitmap = BitmapFactory.decodeStream(input) ?: throw IOException()
                    try {
                        val pixels = IntArray(bitmap.width * bitmap.height)
                        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
                        PixelBuffer(bitmap.width, bitmap.height, pixels)
                    } finally {
                        bitmap.recycle()
                    }
                }
                val editResult = pageEditor.applyToBuffer(sourceBuffer, crop, mode, intensity)
                val editedAssetId = when (editResult) {
                    is PageEditResult.Success -> editResult.assetId
                    is PageEditResult.Failure -> throw when (editResult.error) {
                        PageEditError.InvalidRegion -> IllegalArgumentException()
                        PageEditError.ProcessingFailed, PageEditError.StorageFailure -> IOException()
                    }
                }
                val editedFile = pageEditor.resolveEditedAsset(editedAssetId) ?: throw IOException()
                try {
                    requireCompleteJpeg(editedFile)
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeFile(editedFile.path, bounds)
                    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) throw IOException()

                    val newAssetId = AssetId(UUID.randomUUID().toString())
                    val newAssetTarget = File(destination, assetFileName(newAssetId))
                    val newAssociatedData = assetAssociatedData(newAssetId)
                    assetCipher.encrypt(editedFile, newAssetTarget, newAssociatedData)
                    try {
                        assetCipher.verify(newAssetTarget, newAssociatedData)
                    } catch (failure: Exception) {
                        newAssetTarget.delete()
                        throw failure
                    }

                    val updatedPages = existingRecord.pages.map { page ->
                        if (page.pageId == pageId) {
                            page.copy(
                                assetId = newAssetId,
                                widthPixels = bounds.outWidth,
                                heightPixels = bounds.outHeight,
                            )
                        } else {
                            page
                        }
                    }
                    updateCommittedManifest(
                        destination,
                        existingRecord.copy(pages = updatedPages, updatedAtEpochMillis = updatedAtEpochMillis),
                    )
                    oldAssetFile.delete()
                    readDocument(destination).getOrThrow()
                } finally {
                    pageEditor.release(listOf(editedAssetId))
                }
            }
        }
    }

    /**
     * Rewrites an already-committed document's manifest (name/category only; pages are untouched)
     * without ever risking the working manifest.
     *
     * The replacement ciphertext is decrypted and re-parsed in memory *before* it ever touches
     * disk, so a bug here can only fail closed -- it can never swap a healthy manifest for a
     * broken one. The on-disk swap itself is a same-directory atomic rename over a synced temp
     * file, mirroring the vault-scope write below.
     */
    private suspend fun updateMetadata(
        documentId: DocumentId,
        transform: (ManifestRecord) -> ManifestRecord,
    ): ToollyResult<DocumentDetails> = withContext(Dispatchers.IO) {
        synchronized(lock) {
            if (legacyMigrationBlocked) return@withContext migrationFailure()
            val destination = documentDirectory(documentId)
            if (!destination.isDirectory || !File(destination, COMMITTED_MARKER).isFile) {
                return@withContext unavailableFailure()
            }
            runSafely {
                val manifestFile = File(destination, MANIFEST_FILE)
                if (!manifestFile.isFile || manifestFile.length() !in 1..MAX_MANIFEST_ENVELOPE_BYTES) {
                    throw IOException()
                }
                val existingPlaintext = metadataCipher.decrypt(
                    manifestFile.readBytes(),
                    metadataAssociatedData(documentId),
                )
                val existingRecord = try {
                    parseManifestPlaintext(existingPlaintext, documentId)
                } finally {
                    existingPlaintext.fill(0)
                }
                val updatedRecord = transform(existingRecord)
                require(updatedRecord.updatedAtEpochMillis >= existingRecord.createdAtEpochMillis)
                require(updatedRecord.pages == existingRecord.pages)
                updateCommittedManifest(destination, updatedRecord)
                readDocument(destination).getOrThrow()
            }
        }
    }

    private fun updateCommittedManifest(destination: File, record: ManifestRecord) {
        val encrypted = encryptManifest(record)
        val verifyPlaintext = metadataCipher.decrypt(encrypted, metadataAssociatedData(record.documentId))
        try {
            val reparsed = parseManifestPlaintext(verifyPlaintext, record.documentId)
            check(reparsed == record)
        } finally {
            verifyPlaintext.fill(0)
        }
        val tmp = File(destination, "$MANIFEST_FILE.tmp")
        writeAndSync(tmp, encrypted)
        if (!tmp.renameTo(File(destination, MANIFEST_FILE))) {
            tmp.delete()
            throw IOException()
        }
    }

    suspend fun loadAssetBitmap(assetId: AssetId): Bitmap? = withContext(Dispatchers.IO) {
        synchronized(lock) {
            val encryptedFile = findAssetFile(assetId) ?: return@withContext null
            decodeBoundedBitmap(
                encryptedFile,
                assetAssociatedData(assetId),
                MAX_VIEW_DECODE_DIMENSION,
            )
        }
    }

    suspend fun loadAssetBitmapForExport(assetId: AssetId): ToollyResult<Bitmap> =
        withContext(Dispatchers.IO) {
            synchronized(lock) {
                val encryptedFile = findAssetFile(assetId)
                    ?: return@withContext unavailableFailure()
                runSafely {
                    decodeBoundedBitmap(
                        encryptedFile,
                        assetAssociatedData(assetId),
                        MAX_EXPORT_DECODE_DIMENSION,
                    ) ?: throw IOException()
                }
            }
        }

    private fun recoverInterruptedWrites() {
        synchronized(lock) {
            for (candidate in staging.listFiles().orEmpty()) {
                candidate.deleteRecursivelySafely()
            }
            for (candidate in documents.listFiles().orEmpty()) {
                if (candidate.isDirectory && !File(candidate, COMMITTED_MARKER).isFile) {
                    candidate.deleteRecursivelySafely()
                }
            }
        }
    }

    /**
     * Resumable one-time migration from the earlier app-private plaintext walking slice.
     *
     * A legacy document is deleted only after its encrypted replacement is reopened and fully
     * authenticated. Any failure preserves the legacy bytes and blocks mixed old/new writes.
     */
    private fun migrateLegacyDocuments() {
        synchronized(lock) {
            if (!legacyRoot.isDirectory) return
            File(legacyRoot, LEGACY_STAGING_DIRECTORY).deleteRecursivelyLegacySafely()
            val legacyDocuments = File(legacyRoot, LEGACY_DOCUMENTS_DIRECTORY)
            for (legacyDirectory in legacyDocuments.listFiles().orEmpty()) {
                if (
                    !legacyDirectory.isDirectory ||
                    !File(legacyDirectory, LEGACY_COMMITTED_MARKER).isFile
                ) {
                    legacyDirectory.deleteRecursivelyLegacySafely()
                    continue
                }
                val transaction = File(staging, "migration-${legacyDirectory.name}")
                try {
                    val record = readLegacyManifest(legacyDirectory)
                    val destination = documentDirectory(record.documentId)
                    if (
                        destination.isDirectory &&
                        !File(destination, COMMITTED_MARKER).isFile
                    ) {
                        destination.deleteRecursivelySafely()
                    }
                    if (!destination.isDirectory) {
                        transaction.deleteRecursivelySafely()
                        if (!transaction.mkdirs()) throw IOException()
                        for (page in record.pages) {
                            val source = File(legacyDirectory, legacyAssetFileName(page.assetId))
                            requireCompleteJpeg(source)
                            val target = File(transaction, assetFileName(page.assetId))
                            val associatedData = assetAssociatedData(page.assetId)
                            assetCipher.encrypt(source, target, associatedData)
                            assetCipher.verify(target, associatedData)
                        }
                        writeManifest(transaction, record)
                        writeAndSync(File(transaction, COMMITTED_MARKER), MARKER_BYTES)
                        if (!transaction.renameTo(destination)) throw IOException()
                    }
                    readDocument(destination).getOrThrow()
                    legacyDirectory.deleteRecursivelyLegacySafely()
                } catch (_: Exception) {
                    transaction.deleteRecursivelySafely()
                    legacyMigrationBlocked = true
                }
            }
            if (legacyDocuments.listFiles().orEmpty().none { it.isDirectory }) {
                legacyRoot.deleteRecursivelyLegacySafely()
            }
        }
    }

    private fun loadOrCreateVaultScope(): String {
        val scopeFile = File(root, VAULT_SCOPE_FILE)
        if (scopeFile.isFile) {
            if (scopeFile.length() !in 1..MAX_SCOPE_BYTES) throw IllegalStateException()
            return canonicalUuid(scopeFile.readText(Charsets.UTF_8))
        }
        if (
            documents.listFiles().orEmpty().isNotEmpty() ||
            staging.listFiles().orEmpty().isNotEmpty()
        ) {
            throw IllegalStateException()
        }
        val scope = UUID.randomUUID().toString().lowercase()
        val temporary = File(root, "$VAULT_SCOPE_FILE.tmp")
        writeAndSync(temporary, scope.toByteArray(Charsets.UTF_8))
        if (!temporary.renameTo(scopeFile)) {
            temporary.delete()
            throw IllegalStateException()
        }
        return scope
    }

    private fun writeManifest(directory: File, record: ManifestRecord) {
        writeAndSync(File(directory, MANIFEST_FILE), encryptManifest(record))
    }

    private fun encryptManifest(record: ManifestRecord): ByteArray {
        val pages = JSONArray()
        for (page in record.pages.sortedBy { it.ordinal }) {
            pages.put(
                JSONObject()
                    .put(KEY_PAGE_ID, page.pageId.value)
                    .put(KEY_ASSET_ID, page.assetId.value)
                    .put(KEY_ORDINAL, page.ordinal)
                    .put(KEY_WIDTH, page.widthPixels ?: JSONObject.NULL)
                    .put(KEY_HEIGHT, page.heightPixels ?: JSONObject.NULL),
            )
        }
        val plaintext = JSONObject()
            .put(KEY_SCHEMA_VERSION, SCHEMA_VERSION)
            .put(KEY_DOCUMENT_ID, record.documentId.value)
            .put(KEY_CREATED_AT, record.createdAtEpochMillis)
            .put(KEY_UPDATED_AT, record.updatedAtEpochMillis)
            .put(KEY_DISPLAY_NAME, record.displayName ?: JSONObject.NULL)
            .put(KEY_CATEGORY, record.category?.name ?: JSONObject.NULL)
            .put(KEY_PAGES, pages)
            .toString()
            .toByteArray(Charsets.UTF_8)
        return try {
            metadataCipher.encrypt(plaintext, metadataAssociatedData(record.documentId))
        } finally {
            plaintext.fill(0)
        }
    }

    /**
     * Parses a decrypted manifest payload into a [ManifestRecord].
     *
     * [KEY_DISPLAY_NAME] and [KEY_CATEGORY] are read as optional: manifests written before this
     * field existed have neither key, and must keep opening exactly as they did before (name and
     * category simply read back as `null`) rather than being rejected as corrupt.
     */
    private fun parseManifestPlaintext(plaintext: ByteArray, documentId: DocumentId): ManifestRecord {
        val json = JSONObject(plaintext.toString(Charsets.UTF_8))
        if (
            json.getInt(KEY_SCHEMA_VERSION) != SCHEMA_VERSION ||
            json.getString(KEY_DOCUMENT_ID) != documentId.value
        ) {
            throw IOException()
        }
        val pagesJson = json.getJSONArray(KEY_PAGES)
        val pages = buildList {
            for (index in 0 until pagesJson.length()) {
                val page = pagesJson.getJSONObject(index)
                add(
                    ManifestPage(
                        pageId = PageId(canonicalUuid(page.getString(KEY_PAGE_ID))),
                        assetId = AssetId(canonicalUuid(page.getString(KEY_ASSET_ID))),
                        ordinal = page.getInt(KEY_ORDINAL),
                        widthPixels = page.optionalPositiveInt(KEY_WIDTH),
                        heightPixels = page.optionalPositiveInt(KEY_HEIGHT),
                    ),
                )
            }
        }
        return ManifestRecord(
            documentId = documentId,
            createdAtEpochMillis = json.getLong(KEY_CREATED_AT),
            updatedAtEpochMillis = json.getLong(KEY_UPDATED_AT),
            displayName = json.optionalDisplayName(),
            category = json.optionalCategory(),
            pages = pages,
        )
    }

    private fun JSONObject.optionalDisplayName(): String? {
        if (!has(KEY_DISPLAY_NAME) || isNull(KEY_DISPLAY_NAME)) return null
        val value = getString(KEY_DISPLAY_NAME)
        if (value.isBlank() || value.length > MAX_DISPLAY_NAME_LENGTH) throw IOException()
        return value
    }

    private fun JSONObject.optionalCategory(): DocumentCategory? {
        if (!has(KEY_CATEGORY) || isNull(KEY_CATEGORY)) return null
        return try {
            DocumentCategory.valueOf(getString(KEY_CATEGORY))
        } catch (malformed: IllegalArgumentException) {
            throw IOException()
        }
    }

    private fun readDocument(directory: File): Result<DocumentDetails> = runCatching {
        val documentId = DocumentId(canonicalUuid(directory.name))
        val manifestFile = File(directory, MANIFEST_FILE)
        if (!manifestFile.isFile || manifestFile.length() !in 1..MAX_MANIFEST_ENVELOPE_BYTES) {
            throw IOException()
        }
        val plaintext = metadataCipher.decrypt(
            manifestFile.readBytes(),
            metadataAssociatedData(documentId),
        )
        val record = try {
            parseManifestPlaintext(plaintext, documentId)
        } finally {
            plaintext.fill(0)
        }
        val pages = record.pages.map { page ->
            val encryptedAsset = File(directory, assetFileName(page.assetId))
            assetCipher.verify(encryptedAsset, assetAssociatedData(page.assetId))
            DocumentPage(
                id = page.pageId,
                sourceAssetId = page.assetId,
                ordinal = page.ordinal,
                widthPixels = page.widthPixels,
                heightPixels = page.heightPixels,
            )
        }
        DocumentDetails(
            summary = DocumentSummary(
                id = documentId,
                pageCount = pages.size,
                createdAtEpochMillis = record.createdAtEpochMillis,
                updatedAtEpochMillis = record.updatedAtEpochMillis,
                lifecycle = DocumentLifecycle.ACTIVE,
                displayName = record.displayName,
                category = record.category,
            ),
            pages = pages.sortedBy { it.ordinal },
        )
    }

    private fun readLegacyManifest(directory: File): ManifestRecord {
        val manifestFile = File(directory, LEGACY_MANIFEST_FILE)
        if (!manifestFile.isFile || manifestFile.length() !in 1..MAX_LEGACY_MANIFEST_BYTES) {
            throw IOException()
        }
        val json = JSONObject(manifestFile.readText(Charsets.UTF_8))
        if (json.getInt(KEY_SCHEMA_VERSION) != LEGACY_SCHEMA_VERSION) throw IOException()
        val documentId = DocumentId(canonicalUuid(json.getString(KEY_DOCUMENT_ID)))
        if (documentId.value != directory.name) throw IOException()
        val pagesJson = json.getJSONArray(KEY_PAGES)
        val pages = buildList {
            for (index in 0 until pagesJson.length()) {
                val page = pagesJson.getJSONObject(index)
                add(
                    ManifestPage(
                        pageId = PageId(canonicalUuid(page.getString(KEY_PAGE_ID))),
                        assetId = AssetId(canonicalUuid(page.getString(KEY_ASSET_ID))),
                        ordinal = page.getInt(KEY_ORDINAL),
                        widthPixels = page.optionalPositiveInt(KEY_WIDTH),
                        heightPixels = page.optionalPositiveInt(KEY_HEIGHT),
                    ),
                )
            }
        }
        return ManifestRecord(
            documentId = documentId,
            createdAtEpochMillis = json.getLong(KEY_CREATED_AT),
            updatedAtEpochMillis = json.getLong(KEY_UPDATED_AT),
            displayName = null,
            category = null,
            pages = pages,
        )
    }

    private fun manifestFrom(command: SaveCapturedDocumentCommand): ManifestRecord =
        ManifestRecord(
            documentId = command.documentId,
            createdAtEpochMillis = command.createdAtEpochMillis,
            updatedAtEpochMillis = command.createdAtEpochMillis,
            displayName = null,
            category = null,
            pages = command.pages.map {
                ManifestPage(
                    pageId = it.pageId,
                    assetId = it.assetId,
                    ordinal = it.ordinal,
                    widthPixels = it.widthPixels,
                    heightPixels = it.heightPixels,
                )
            },
        )

    private fun decodeBoundedBitmap(
        encryptedFile: File,
        associatedData: AssetAssociatedData,
        maximumDimension: Int,
    ): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        assetCipher.openDecrypted(encryptedFile, associatedData).use { input ->
            BitmapFactory.decodeStream(input, null, bounds)
            input.drainAuthenticated()
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sampleSize = 1
        while (
            bounds.outWidth / sampleSize > maximumDimension ||
            bounds.outHeight / sampleSize > maximumDimension
        ) {
            sampleSize *= 2
        }
        return assetCipher.openDecrypted(encryptedFile, associatedData).use { input ->
            val bitmap = BitmapFactory.decodeStream(
                input,
                null,
                BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                },
            )
            try {
                input.drainAuthenticated()
                bitmap
            } catch (failure: Exception) {
                bitmap?.recycle()
                throw failure
            }
        }
    }

    private fun InputStream.drainAuthenticated() {
        val buffer = ByteArray(DECODE_DRAIN_BUFFER_BYTES)
        while (read(buffer) != -1) {
            // Reading to EOF verifies the complete chunk sequence and rejects appended data.
        }
    }

    private fun requireCompleteJpeg(file: File) {
        if (
            !file.isFile ||
            file.length() !in MIN_JPEG_BYTES..MAX_PAGE_BYTES ||
            !isCompleteJpeg(file)
        ) {
            throw IOException()
        }
    }

    private fun isCompleteJpeg(file: File): Boolean =
        RandomAccessFile(file, "r").use { source ->
            val startsWithSoi = source.readUnsignedByte() == JPEG_MARKER &&
                source.readUnsignedByte() == JPEG_SOI
            source.seek(source.length() - 2)
            val endsWithEoi = source.readUnsignedByte() == JPEG_MARKER &&
                source.readUnsignedByte() == JPEG_EOI
            startsWithSoi && endsWithEoi
        }

    private fun findAssetFile(assetId: AssetId): File? =
        documents.listFiles()
            .orEmpty()
            .asSequence()
            .filter { it.isDirectory && File(it, COMMITTED_MARKER).isFile }
            .map { File(it, assetFileName(assetId)) }
            .firstOrNull { it.isFile && it.parentFile?.parentFile == documents }

    private fun documentDirectory(id: DocumentId): File = File(documents, id.value)

    private fun metadataAssociatedData(documentId: DocumentId) = MetadataAssociatedData(
        vaultScopeId = vaultScopeId,
        recordId = documentId.value,
        recordKind = RecordKind.DOCUMENT,
        schemaVersion = SCHEMA_VERSION,
        revision = INITIAL_REVISION,
    )

    private fun assetAssociatedData(assetId: AssetId) = AssetAssociatedData(
        vaultScopeId = vaultScopeId,
        assetId = assetId.value,
        objectKind = AssetObjectKind.SOURCE_IMAGE,
    )

    private fun assetFileName(assetId: AssetId): String = "${assetId.value}.tlya"

    private fun legacyAssetFileName(assetId: AssetId): String = "${assetId.value}.jpg"

    private fun JSONObject.optionalPositiveInt(key: String): Int? =
        if (isNull(key)) null else getInt(key).also { require(it > 0) }

    private fun canonicalUuid(raw: String): String {
        val canonical = UUID.fromString(raw).toString().lowercase()
        if (canonical != raw) throw IllegalArgumentException()
        return canonical
    }

    private fun writeAndSync(file: File, bytes: ByteArray) {
        FileOutputStream(file).use { output ->
            output.write(bytes)
            output.fd.sync()
        }
    }

    private fun <T> runSafely(block: () -> T): ToollyResult<T> = try {
        ToollyResult.Success(block())
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (failure: Exception) {
        failure.toToollyFailure()
    }

    private fun <T> Throwable.toToollyFailure(
        saveStage: VaultSaveStage? = null,
    ): ToollyResult<T> {
        val code = when (this) {
            is VaultCryptoException.KeyUnavailable -> ToollyErrorCode.UNAUTHORIZED
            is VaultCryptoException,
            is JSONException,
            is IllegalArgumentException -> ToollyErrorCode.CORRUPT
            else -> ToollyErrorCode.RETRYABLE
        }
        return ToollyResult.Failure(
            ToollyError(code, saveStage?.name ?: code.name),
        )
    }

    private fun <T> cryptoOrCorruptFailure(failure: Throwable): ToollyResult<T> =
        failure.toToollyFailure()

    private fun <T> storageFailure(
        saveStage: VaultSaveStage? = null,
    ): ToollyResult<T> = ToollyResult.Failure(
        ToollyError(
            ToollyErrorCode.RETRYABLE,
            saveStage?.name ?: ToollyErrorCode.RETRYABLE.name,
        ),
    )

    private fun <T> migrationFailure(): ToollyResult<T> = ToollyResult.Failure(
        ToollyError(ToollyErrorCode.RETRYABLE, ToollyErrorCode.RETRYABLE.name),
    )

    private fun <T> unavailableFailure(): ToollyResult<T> = ToollyResult.Failure(
        ToollyError(ToollyErrorCode.UNAVAILABLE, ToollyErrorCode.UNAVAILABLE.name),
    )

    private fun File.deleteRecursivelySafely() {
        runCatching {
            val rootPrefix = root.canonicalPath + File.separator
            if (exists() && canonicalPath.startsWith(rootPrefix)) deleteRecursively()
        }
    }

    private fun File.deleteRecursivelyLegacySafely() {
        runCatching {
            val legacyPrefix = legacyRoot.canonicalPath + File.separator
            if (
                exists() &&
                (canonicalPath == legacyRoot.canonicalPath || canonicalPath.startsWith(legacyPrefix))
            ) {
                deleteRecursively()
            }
        }
    }

    private data class ManifestRecord(
        val documentId: DocumentId,
        val createdAtEpochMillis: Long,
        val updatedAtEpochMillis: Long,
        val displayName: String?,
        val category: DocumentCategory?,
        val pages: List<ManifestPage>,
    )

    private data class ManifestPage(
        val pageId: PageId,
        val assetId: AssetId,
        val ordinal: Int,
        val widthPixels: Int?,
        val heightPixels: Int?,
    )

    private companion object {
        const val ROOT_DIRECTORY = "toolly-encrypted-vault-v1"
        const val LEGACY_ROOT_DIRECTORY = "toolly-local-candidate-v1"
        const val STAGING_DIRECTORY = ".staging"
        const val DOCUMENTS_DIRECTORY = "documents"
        const val VAULT_SCOPE_FILE = "vault.scope"
        const val MANIFEST_FILE = "manifest.tlym"
        const val COMMITTED_MARKER = "COMMITTED"
        const val LEGACY_STAGING_DIRECTORY = ".staging"
        const val LEGACY_DOCUMENTS_DIRECTORY = "documents"
        const val LEGACY_MANIFEST_FILE = "manifest.json"
        const val LEGACY_COMMITTED_MARKER = "COMMITTED"
        const val SCHEMA_VERSION = 1
        const val LEGACY_SCHEMA_VERSION = 1
        const val INITIAL_REVISION = 0L
        const val MAX_PAGE_BYTES = 25L * 1024L * 1024L
        const val MAX_MANIFEST_ENVELOPE_BYTES = 300L * 1024L
        const val MAX_LEGACY_MANIFEST_BYTES = 256L * 1024L
        const val MAX_SCOPE_BYTES = 64L
        const val MIN_JPEG_BYTES = 4L
        const val JPEG_MARKER = 0xFF
        const val JPEG_SOI = 0xD8
        const val JPEG_EOI = 0xD9
        const val MAX_VIEW_DECODE_DIMENSION = 2048
        const val MAX_EXPORT_DECODE_DIMENSION = 3508
        const val DECODE_DRAIN_BUFFER_BYTES = 64 * 1024
        val MARKER_BYTES = byteArrayOf(1)
        const val KEY_SCHEMA_VERSION = "schemaVersion"
        const val KEY_DOCUMENT_ID = "documentId"
        const val KEY_CREATED_AT = "createdAtEpochMillis"
        const val KEY_UPDATED_AT = "updatedAtEpochMillis"
        const val KEY_DISPLAY_NAME = "displayName"
        const val KEY_CATEGORY = "category"
        const val KEY_PAGES = "pages"
        const val KEY_PAGE_ID = "pageId"
        const val KEY_ASSET_ID = "assetId"
        const val KEY_ORDINAL = "ordinal"
        const val KEY_WIDTH = "widthPixels"
        const val KEY_HEIGHT = "heightPixels"
    }
}
