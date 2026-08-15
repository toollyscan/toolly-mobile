package com.toolly.spike.capture.vault.edit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.toolly.shared.capture.TemporaryAssetId
import com.toolly.shared.edit.ColorAdjust
import com.toolly.shared.edit.CropRegion
import com.toolly.shared.edit.EnhancementMode
import com.toolly.shared.edit.ImageRotation
import com.toolly.shared.edit.PageEditError
import com.toolly.shared.edit.PageEditRequest
import com.toolly.shared.edit.PageEditResult
import com.toolly.shared.edit.PageEditor
import com.toolly.shared.edit.PerspectiveWarp
import com.toolly.shared.edit.PixelBuffer
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID
import kotlin.math.sqrt

/**
 * Android [PageEditor] adapter. Decodes the source JPEG to a provider-neutral [PixelBuffer],
 * applies the shared `commonMain` crop/enhancement math (`shared-core/.../edit`), and encodes the
 * result to a new temporary JPEG asset. Only decode/encode touch a platform bitmap API -- the
 * transform itself is the exact same code that would run on iOS.
 *
 * [resolveAsset] follows the same dependency-inversion pattern as
 * [com.toolly.spike.capture.vault.EncryptedDocumentRepository]'s `resolveTemporaryAsset`: this
 * class does not know or care which store produced the source asset.
 */
internal class AndroidPageEditor(
    context: Context,
    private val resolveAsset: (TemporaryAssetId) -> File?,
) : PageEditor {

    private val appContext = context.applicationContext
    private val directory = File(appContext.cacheDir, DIRECTORY_NAME)

    init {
        directory.mkdirs()
    }

    override suspend fun apply(request: PageEditRequest): PageEditResult {
        val sourceFile = resolveAsset(request.assetId)
            ?: return PageEditResult.Failure(PageEditError.StorageFailure)
        val sourceBuffer = decodeToPixelBuffer(sourceFile)
            ?: return PageEditResult.Failure(PageEditError.ProcessingFailed)
        return applyToBuffer(
            sourceBuffer,
            request.crop,
            request.mode,
            request.intensity,
            request.rotationQuarterTurns,
        )
    }

    /**
     * Same crop/enhance/encode pipeline as [apply], entry point for callers that already have a
     * decoded [PixelBuffer] and no [TemporaryAssetId] to resolve -- used by
     * [com.toolly.spike.capture.vault.EncryptedDocumentRepository]'s re-crop-a-saved-page flow,
     * which decrypts the source page straight to memory (matching `loadAssetBitmap`'s existing
     * plaintext-stays-in-memory boundary) rather than staging it as a plaintext file first. The
     * *output* still lands in this class's own bounded cache directory, same as [apply] -- that
     * matches the accepted plaintext boundary for newly-captured pages before vault encryption
     * (`TemporaryScanStore`'s staging directory), not a new exposure.
     */
    fun applyToBuffer(
        source: PixelBuffer,
        crop: CropRegion?,
        mode: EnhancementMode,
        intensity: Float,
        rotationQuarterTurns: Int = 0,
    ): PageEditResult = try {
        val rotated = if (rotationQuarterTurns % 4 == 0) {
            source
        } else {
            ImageRotation.rotate90(source, rotationQuarterTurns)
        }
        val cropped = crop?.let { region ->
            val (outputWidth, outputHeight) = outputDimensions(rotated, region)
            PerspectiveWarp.warp(rotated, region, outputWidth, outputHeight)
        } ?: rotated
        val adjusted = ColorAdjust.apply(cropped, mode, intensity)

        val assetId = TemporaryAssetId(UUID.randomUUID().toString().replace("-", "").lowercase())
        writeJpeg(adjusted, File(directory, "${assetId.value}.jpg"))
        PageEditResult.Success(assetId)
    } catch (_: IllegalArgumentException) {
        // A degenerate CropRegion is the only expected source of this from shared PerspectiveWarp.
        PageEditResult.Failure(PageEditError.InvalidRegion)
    } catch (_: IOException) {
        PageEditResult.Failure(PageEditError.StorageFailure)
    } catch (_: OutOfMemoryError) {
        PageEditResult.Failure(PageEditError.ProcessingFailed)
    }

    /** Resolves an asset previously produced by [apply]; matches [TemporaryScanStore]'s `resolve` shape. */
    fun resolveEditedAsset(assetId: TemporaryAssetId): File? =
        File(directory, "${assetId.value}.jpg").takeIf { it.isFile && it.parentFile == directory }

    fun release(assetIds: Collection<TemporaryAssetId>) {
        for (assetId in assetIds) File(directory, "${assetId.value}.jpg").delete()
    }

    private fun decodeToPixelBuffer(file: File): PixelBuffer? {
        val bitmap = BitmapFactory.decodeFile(file.path) ?: return null
        return try {
            val pixels = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            PixelBuffer(bitmap.width, bitmap.height, pixels)
        } finally {
            bitmap.recycle()
        }
    }

    private fun writeJpeg(buffer: PixelBuffer, destination: File) {
        val bitmap = Bitmap.createBitmap(buffer.width, buffer.height, Bitmap.Config.ARGB_8888)
        val pending = File(destination.parentFile, "${destination.nameWithoutExtension}.part")
        try {
            bitmap.setPixels(buffer.argb, 0, buffer.width, 0, 0, buffer.width, buffer.height)
            FileOutputStream(pending).use { output ->
                check(bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)) {
                    "JPEG encode failed"
                }
                output.fd.sync()
            }
            check(pending.renameTo(destination)) { "Unable to finalize edited asset" }
        } catch (failure: IllegalStateException) {
            throw IOException(failure.message)
        } finally {
            bitmap.recycle()
            pending.delete()
        }
    }

    /**
     * Output size follows the longer of the crop's top/bottom and left/right pixel-space edges,
     * clamped to the source resolution so the warp never upsamples beyond the captured detail.
     */
    private fun outputDimensions(source: PixelBuffer, crop: CropRegion): Pair<Int, Int> {
        fun edgeLength(ax: Float, ay: Float, bx: Float, by: Float): Float {
            val dx = (ax - bx) * source.width
            val dy = (ay - by) * source.height
            return sqrt(dx * dx + dy * dy)
        }
        val topWidth = edgeLength(crop.topLeft.x, crop.topLeft.y, crop.topRight.x, crop.topRight.y)
        val bottomWidth = edgeLength(crop.bottomLeft.x, crop.bottomLeft.y, crop.bottomRight.x, crop.bottomRight.y)
        val leftHeight = edgeLength(crop.topLeft.x, crop.topLeft.y, crop.bottomLeft.x, crop.bottomLeft.y)
        val rightHeight = edgeLength(crop.topRight.x, crop.topRight.y, crop.bottomRight.x, crop.bottomRight.y)
        val width = maxOf(topWidth, bottomWidth).toInt().coerceIn(1, source.width)
        val height = maxOf(leftHeight, rightHeight).toInt().coerceIn(1, source.height)
        return width to height
    }

    private companion object {
        const val DIRECTORY_NAME = "toolly-page-edit"
        const val JPEG_QUALITY = 92
    }
}
