package com.toolly.spike.capture.vault

import android.content.Context
import com.toolly.domain.contracts.DocumentRepository
import com.toolly.domain.contracts.SaveCapturedDocumentCommand
import com.toolly.domain.model.AssetId
import com.toolly.domain.model.DocumentDetails
import com.toolly.domain.model.DocumentId
import com.toolly.domain.model.DocumentLifecycle
import com.toolly.domain.model.DocumentPage
import com.toolly.domain.model.DocumentSummary
import com.toolly.domain.model.PageId
import com.toolly.foundation.ToollyError
import com.toolly.foundation.ToollyErrorCode
import com.toolly.foundation.ToollyResult
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Recoverable app-private persistence candidate for the TLY-011 walking slice.
 *
 * This adapter deliberately sits behind [DocumentRepository]. It proves staging, atomic
 * publication, process recreation and library behavior without coupling product code to files.
 * It is not the final encrypted-vault adapter: document bytes remain restricted to the app
 * sandbox, and TLY-006F replaces this implementation after SQLCipher/key-envelope evidence.
 */
class AppPrivateDocumentRepository(
    context: Context,
    private val resolveTemporaryAsset: (String) -> File?,
) : DocumentRepository {

    private val root = File(context.applicationContext.filesDir, ROOT_DIRECTORY)
    private val staging = File(root, STAGING_DIRECTORY)
    private val documents = File(root, DOCUMENTS_DIRECTORY)
    private val lock = Any()

    init {
        check(root.mkdirs() || root.isDirectory)
        check(staging.mkdirs() || staging.isDirectory)
        check(documents.mkdirs() || documents.isDirectory)
        recoverInterruptedWrites()
    }

    override suspend fun listDocuments(): ToollyResult<List<DocumentSummary>> =
        withContext(Dispatchers.IO) {
            synchronized(lock) {
                runSafely {
                    documents.listFiles()
                        .orEmpty()
                        .asSequence()
                        .filter { it.isDirectory && File(it, COMMITTED_MARKER).isFile }
                        .mapNotNull { directory -> readDocument(directory).getOrNull()?.summary }
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
                return@withContext ToollyResult.Failure(
                    ToollyError(ToollyErrorCode.UNAVAILABLE, "Document is not available"),
                )
            }
            readDocument(directory).fold(
                onSuccess = { ToollyResult.Success(it) },
                onFailure = {
                    ToollyResult.Failure(
                        ToollyError(ToollyErrorCode.CORRUPT, "Document metadata is invalid"),
                    )
                },
            )
        }
    }

    override suspend fun saveCapturedDocument(
        command: SaveCapturedDocumentCommand,
    ): ToollyResult<DocumentDetails> = withContext(Dispatchers.IO) {
        synchronized(lock) {
            val destination = documentDirectory(command.documentId)
            if (destination.isDirectory && File(destination, COMMITTED_MARKER).isFile) {
                return@withContext readDocument(destination).fold(
                    onSuccess = { ToollyResult.Success(it) },
                    onFailure = {
                        ToollyResult.Failure(
                            ToollyError(ToollyErrorCode.CORRUPT, "Saved document is invalid"),
                        )
                    },
                )
            }

            val transaction = File(staging, command.operationId.value)
            transaction.deleteRecursivelySafely()
            if (!transaction.mkdirs()) {
                return@withContext storageFailure()
            }

            try {
                for (page in command.pages) {
                    val source = resolveTemporaryAsset(page.temporaryAssetId.value)
                        ?: throw IOException("Temporary source unavailable")
                    val target = File(transaction, assetFileName(page.assetId))
                    copyBoundedJpeg(source, target)
                }
                writeManifest(transaction, command)
                writeAndSync(File(transaction, COMMITTED_MARKER), MARKER_BYTES)

                if (!transaction.renameTo(destination)) {
                    throw IOException("Atomic document publication failed")
                }

                readDocument(destination).fold(
                    onSuccess = { ToollyResult.Success(it) },
                    onFailure = {
                        destination.deleteRecursivelySafely()
                        storageFailure()
                    },
                )
            } catch (_: IOException) {
                transaction.deleteRecursivelySafely()
                storageFailure()
            } catch (_: JSONException) {
                transaction.deleteRecursivelySafely()
                storageFailure()
            }
        }
    }

    fun resolveAsset(assetId: AssetId): File? = synchronized(lock) {
        documents.listFiles()
            .orEmpty()
            .asSequence()
            .filter { it.isDirectory && File(it, COMMITTED_MARKER).isFile }
            .map { File(it, assetFileName(assetId)) }
            .firstOrNull { it.isFile && it.parentFile?.parentFile == documents }
    }

    private fun recoverInterruptedWrites() {
        synchronized(lock) {
            for (candidate in staging.listFiles().orEmpty()) {
                candidate.deleteRecursivelySafely()
            }
        }
    }

    private fun documentDirectory(id: DocumentId): File = File(documents, id.value)

    private fun writeManifest(
        transaction: File,
        command: SaveCapturedDocumentCommand,
    ) {
        val pages = JSONArray()
        for (page in command.pages.sortedBy { it.ordinal }) {
            pages.put(
                JSONObject()
                    .put(KEY_PAGE_ID, page.pageId.value)
                    .put(KEY_ASSET_ID, page.assetId.value)
                    .put(KEY_ORDINAL, page.ordinal)
                    .put(KEY_WIDTH, page.widthPixels ?: JSONObject.NULL)
                    .put(KEY_HEIGHT, page.heightPixels ?: JSONObject.NULL),
            )
        }
        val manifest = JSONObject()
            .put(KEY_SCHEMA_VERSION, SCHEMA_VERSION)
            .put(KEY_DOCUMENT_ID, command.documentId.value)
            .put(KEY_CREATED_AT, command.createdAtEpochMillis)
            .put(KEY_UPDATED_AT, command.createdAtEpochMillis)
            .put(KEY_PAGES, pages)
            .toString()
            .toByteArray(Charsets.UTF_8)
        writeAndSync(File(transaction, MANIFEST_FILE), manifest)
    }

    private fun readDocument(directory: File): Result<DocumentDetails> = runCatching {
        val manifestFile = File(directory, MANIFEST_FILE)
        if (!manifestFile.isFile || manifestFile.length() !in 1..MAX_MANIFEST_BYTES) {
            throw IOException("Manifest unavailable")
        }
        val json = JSONObject(manifestFile.readText(Charsets.UTF_8))
        if (json.getInt(KEY_SCHEMA_VERSION) != SCHEMA_VERSION) {
            throw IOException("Unsupported schema")
        }
        val pagesJson = json.getJSONArray(KEY_PAGES)
        val pages = buildList {
            for (index in 0 until pagesJson.length()) {
                val page = pagesJson.getJSONObject(index)
                val assetId = AssetId(page.getString(KEY_ASSET_ID))
                val assetFile = File(directory, assetFileName(assetId))
                if (!assetFile.isFile || !isCompleteJpeg(assetFile)) {
                    throw IOException("Asset unavailable")
                }
                add(
                    DocumentPage(
                        id = PageId(page.getString(KEY_PAGE_ID)),
                        sourceAssetId = assetId,
                        ordinal = page.getInt(KEY_ORDINAL),
                        widthPixels = page.optionalPositiveInt(KEY_WIDTH),
                        heightPixels = page.optionalPositiveInt(KEY_HEIGHT),
                    ),
                )
            }
        }
        val createdAt = json.getLong(KEY_CREATED_AT)
        DocumentDetails(
            summary = DocumentSummary(
                id = DocumentId(json.getString(KEY_DOCUMENT_ID)),
                pageCount = pages.size,
                createdAtEpochMillis = createdAt,
                updatedAtEpochMillis = json.getLong(KEY_UPDATED_AT),
                lifecycle = DocumentLifecycle.ACTIVE,
            ),
            pages = pages.sortedBy { it.ordinal },
        )
    }

    private fun copyBoundedJpeg(source: File, target: File) {
        if (!source.isFile || source.length() !in MIN_JPEG_BYTES..MAX_PAGE_BYTES) {
            throw IOException("Invalid source size")
        }
        source.inputStream().use { input ->
            FileOutputStream(target).use { output ->
                val buffer = ByteArray(COPY_BUFFER_BYTES)
                var total = 0L
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    total += count
                    if (total > MAX_PAGE_BYTES) throw IOException("Page exceeds limit")
                    output.write(buffer, 0, count)
                }
                output.fd.sync()
            }
        }
        if (!isCompleteJpeg(target)) {
            target.delete()
            throw IOException("Incomplete JPEG")
        }
    }

    private fun isCompleteJpeg(file: File): Boolean =
        file.length() >= MIN_JPEG_BYTES &&
            RandomAccessFile(file, "r").use { source ->
                val startsWithSoi = source.readUnsignedByte() == JPEG_MARKER &&
                    source.readUnsignedByte() == JPEG_SOI
                source.seek(source.length() - 2)
                val endsWithEoi = source.readUnsignedByte() == JPEG_MARKER &&
                    source.readUnsignedByte() == JPEG_EOI
                startsWithSoi && endsWithEoi
            }

    private fun writeAndSync(file: File, bytes: ByteArray) {
        FileOutputStream(file).use { output ->
            output.write(bytes)
            output.fd.sync()
        }
    }

    private fun assetFileName(assetId: AssetId): String = "${assetId.value}.jpg"

    private fun JSONObject.optionalPositiveInt(key: String): Int? =
        if (isNull(key)) null else getInt(key).also { require(it > 0) }

    private fun <T> runSafely(block: () -> T): ToollyResult<T> = try {
        ToollyResult.Success(block())
    } catch (_: IOException) {
        storageFailure()
    } catch (_: JSONException) {
        ToollyResult.Failure(
            ToollyError(ToollyErrorCode.CORRUPT, "Document metadata is invalid"),
        )
    }

    private fun <T> storageFailure(): ToollyResult<T> =
        ToollyResult.Failure(
            ToollyError(ToollyErrorCode.RETRYABLE, "Document could not be stored safely"),
        )

    private fun File.deleteRecursivelySafely() {
        runCatching {
            val rootPrefix = root.canonicalPath + File.separator
            if (exists() && canonicalPath.startsWith(rootPrefix)) deleteRecursively()
        }
    }

    private companion object {
        const val ROOT_DIRECTORY = "toolly-local-candidate-v1"
        const val STAGING_DIRECTORY = ".staging"
        const val DOCUMENTS_DIRECTORY = "documents"
        const val MANIFEST_FILE = "manifest.json"
        const val COMMITTED_MARKER = "COMMITTED"
        const val SCHEMA_VERSION = 1
        const val COPY_BUFFER_BYTES = 16 * 1024
        const val MAX_PAGE_BYTES = 25L * 1024L * 1024L
        const val MAX_MANIFEST_BYTES = 256L * 1024L
        const val MIN_JPEG_BYTES = 4L
        const val JPEG_MARKER = 0xFF
        const val JPEG_SOI = 0xD8
        const val JPEG_EOI = 0xD9
        val MARKER_BYTES = byteArrayOf(1)
        const val KEY_SCHEMA_VERSION = "schemaVersion"
        const val KEY_DOCUMENT_ID = "documentId"
        const val KEY_CREATED_AT = "createdAtEpochMillis"
        const val KEY_UPDATED_AT = "updatedAtEpochMillis"
        const val KEY_PAGES = "pages"
        const val KEY_PAGE_ID = "pageId"
        const val KEY_ASSET_ID = "assetId"
        const val KEY_ORDINAL = "ordinal"
        const val KEY_WIDTH = "widthPixels"
        const val KEY_HEIGHT = "heightPixels"
    }
}
