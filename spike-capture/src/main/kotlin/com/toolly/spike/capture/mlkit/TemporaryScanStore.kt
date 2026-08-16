package com.toolly.spike.capture.mlkit

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.toolly.shared.capture.PartialCaptureReason
import com.toolly.shared.capture.TemporaryAssetId
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile
import java.util.UUID

/**
 * Single owner for plaintext scan artifacts produced during the capture spike.
 *
 * Provider URIs are copied into app-private cache before crossing the adapter boundary.
 * Files are bounded, validated as JPEG, finalized by same-filesystem rename, and removed
 * on explicit release or host destruction.
 */
class TemporaryScanStore(context: Context) : AutoCloseable {

    private val appContext = context.applicationContext
    private val directory = File(appContext.cacheDir, DIRECTORY_NAME)
    private val lock = Any()

    init {
        clear()
        check(directory.mkdirs() || directory.isDirectory) {
            "Unable to initialize temporary capture directory"
        }
    }

    fun importPages(sourceUris: List<Uri>): ImportOutcome {
        val imported = mutableListOf<TemporaryAssetId>()
        for (uri in sourceUris) {
            try {
                imported += importPage(uri)
            } catch (failure: PageImportException) {
                return if (imported.isEmpty()) {
                    ImportOutcome.Failure
                } else {
                    ImportOutcome.Partial(imported, failure.reason)
                }
            }
        }
        return ImportOutcome.Success(imported)
    }

    /**
     * Same staging directory/asset-ID scheme as [importPages], for pages that already exist as
     * decoded [Bitmap]s rather than a content [Uri] to copy -- used by PDF import, which rasterizes
     * each PDF page locally before it can be treated like any other captured page (review, crop,
     * enhance, save all reuse the exact same [TemporaryAssetId]-based pipeline from there).
     */
    fun importBitmaps(bitmaps: List<Bitmap>): ImportOutcome {
        val imported = mutableListOf<TemporaryAssetId>()
        for (bitmap in bitmaps) {
            try {
                imported += importBitmap(bitmap)
            } catch (failure: PageImportException) {
                return if (imported.isEmpty()) {
                    ImportOutcome.Failure
                } else {
                    ImportOutcome.Partial(imported, failure.reason)
                }
            }
        }
        return ImportOutcome.Success(imported)
    }

    private fun importBitmap(bitmap: Bitmap): TemporaryAssetId {
        val assetId = TemporaryAssetId(UUID.randomUUID().toString().replace("-", "").lowercase())
        val pending = File(directory, "${assetId.value}.part")
        val destination = File(directory, "${assetId.value}.jpg")
        try {
            FileOutputStream(pending).use { output ->
                check(bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)) {
                    "JPEG encode failed"
                }
                output.fd.sync()
            }
            if (!pending.renameTo(destination)) {
                throw PageImportException(PartialCaptureReason.STORAGE_WRITE_FAILED)
            }
            return assetId
        } catch (failure: PageImportException) {
            pending.delete()
            destination.delete()
            throw failure
        } catch (failure: IllegalStateException) {
            pending.delete()
            destination.delete()
            throw PageImportException(PartialCaptureReason.STORAGE_WRITE_FAILED)
        } catch (failure: IOException) {
            pending.delete()
            destination.delete()
            throw PageImportException(PartialCaptureReason.STORAGE_WRITE_FAILED)
        }
    }

    fun resolve(assetId: TemporaryAssetId): File? = synchronized(lock) {
        File(directory, "${assetId.value}.jpg")
            .takeIf { it.isFile && it.parentFile == directory }
    }

    fun release(assetIds: Collection<TemporaryAssetId>) {
        synchronized(lock) {
            for (assetId in assetIds) {
                File(directory, "${assetId.value}.jpg").delete()
            }
        }
    }

    fun clear() {
        synchronized(lock) {
            if (directory.exists()) {
                for (file in directory.listFiles().orEmpty()) {
                    if (file.isFile) file.delete()
                }
            }
        }
    }

    override fun close() = clear()

    private fun importPage(sourceUri: Uri): TemporaryAssetId {
        val mimeType = appContext.contentResolver.getType(sourceUri)
        if (mimeType != null && mimeType.lowercase() !in ALLOWED_MIME_TYPES) {
            throw PageImportException(PartialCaptureReason.SOURCE_READ_FAILED)
        }

        val assetId = TemporaryAssetId(
            UUID.randomUUID().toString().replace("-", "").lowercase(),
        )
        val pending = File(directory, "${assetId.value}.part")
        val destination = File(directory, "${assetId.value}.jpg")

        try {
            val input = appContext.contentResolver.openInputStream(sourceUri)
                ?: throw PageImportException(PartialCaptureReason.SOURCE_READ_FAILED)
            input.use { source ->
                FileOutputStream(pending).use { output ->
                    val buffer = ByteArray(COPY_BUFFER_BYTES)
                    var total = 0L
                    while (true) {
                        val count = source.read(buffer)
                        if (count < 0) break
                        total += count
                        if (total > MAX_PAGE_BYTES) {
                            throw PageImportException(PartialCaptureReason.SOURCE_READ_FAILED)
                        }
                        output.write(buffer, 0, count)
                    }
                    output.fd.sync()
                }
            }

            if (!isCompleteJpeg(pending)) {
                throw PageImportException(PartialCaptureReason.SOURCE_READ_FAILED)
            }
            if (!pending.renameTo(destination)) {
                throw PageImportException(PartialCaptureReason.STORAGE_WRITE_FAILED)
            }
            return assetId
        } catch (failure: PageImportException) {
            pending.delete()
            destination.delete()
            throw failure
        } catch (failure: IOException) {
            pending.delete()
            destination.delete()
            throw PageImportException(PartialCaptureReason.STORAGE_WRITE_FAILED)
        }
    }

    private fun isCompleteJpeg(file: File): Boolean {
        if (file.length() < MIN_JPEG_BYTES) return false
        return RandomAccessFile(file, "r").use { source ->
            val startsWithSoi = source.readUnsignedByte() == JPEG_MARKER &&
                source.readUnsignedByte() == JPEG_SOI
            source.seek(source.length() - 2)
            val endsWithEoi = source.readUnsignedByte() == JPEG_MARKER &&
                source.readUnsignedByte() == JPEG_EOI
            startsWithSoi && endsWithEoi
        }
    }

    sealed interface ImportOutcome {
        data class Success(val assetIds: List<TemporaryAssetId>) : ImportOutcome
        data class Partial(
            val assetIds: List<TemporaryAssetId>,
            val reason: PartialCaptureReason,
        ) : ImportOutcome
        data object Failure : ImportOutcome
    }

    private class PageImportException(
        val reason: PartialCaptureReason,
    ) : Exception()

    private companion object {
        const val DIRECTORY_NAME = "toolly-capture-spike"
        const val COPY_BUFFER_BYTES = 16 * 1024
        const val MAX_PAGE_BYTES = 25L * 1024L * 1024L
        const val MIN_JPEG_BYTES = 4L
        const val JPEG_QUALITY = 92
        const val JPEG_MARKER = 0xFF
        const val JPEG_SOI = 0xD8
        const val JPEG_EOI = 0xD9
        val ALLOWED_MIME_TYPES = setOf("image/jpeg", "image/jpg")
    }
}
