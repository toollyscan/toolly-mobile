package com.toolly.shared.edit

import com.toolly.shared.capture.TemporaryAssetId

/**
 * Provider-neutral port for one page crop/enhancement operation, mirroring
 * [com.toolly.shared.capture.DocumentScanner]'s shape: only opaque Toolly asset identifiers cross
 * this boundary, never platform bitmap types, file paths or URIs.
 *
 * Implementations decode the asset, apply [PageEditRequest.crop] and
 * [PageEditRequest.mode]/[PageEditRequest.intensity], and persist the result as a new temporary
 * asset. The pixel transforms themselves ([PerspectiveWarp], [ColorAdjust]) are pure `commonMain`
 * Kotlin operating on [PixelBuffer] -- only JPEG decode/encode is platform-specific.
 */
interface PageEditor {
    /** Suspends until the edit is applied, or fails. Releases the source asset only on success. */
    suspend fun apply(request: PageEditRequest): PageEditResult
}

/** A point in a page's own coordinate space, independent of source image resolution. */
data class NormalizedPoint(val x: Float, val y: Float) {
    init {
        require(x in 0f..1f) { "x must be between 0 and 1" }
        require(y in 0f..1f) { "y must be between 0 and 1" }
    }
}

/**
 * The four corners of a page as detected or manually adjusted, in clockwise order starting from
 * the top-left. Matches the draggable-corner crop overlay (wireframes `1.3`/`3.1`).
 */
data class CropRegion(
    val topLeft: NormalizedPoint,
    val topRight: NormalizedPoint,
    val bottomRight: NormalizedPoint,
    val bottomLeft: NormalizedPoint,
)

/** Finish applied to a page (wireframe `1.4 Clean and save`: Auto/Clean/Color/Gray/B&W). */
enum class EnhancementMode {
    AUTO,
    CLEAN,
    COLOR,
    GRAY,
    BLACK_AND_WHITE,
}

/**
 * One page edit request.
 *
 * [crop] is `null` when the user accepts the page as-is with only an enhancement mode applied.
 * [rotationQuarterTurns] (default 0, normalized mod 4 by [ImageRotation.rotate90]) is applied
 * before [crop] -- see that object's doc for why the ordering matters.
 */
data class PageEditRequest(
    val assetId: TemporaryAssetId,
    val crop: CropRegion?,
    val mode: EnhancementMode,
    val intensity: Float,
    val rotationQuarterTurns: Int = 0,
) {
    init {
        require(intensity in 0f..1f) { "intensity must be between 0 and 1" }
    }
}

/** Terminal outcome from [PageEditor.apply]. */
sealed class PageEditResult {
    /** The edited page was written as a new temporary asset; the caller now owns it. */
    data class Success(val assetId: TemporaryAssetId) : PageEditResult()

    data class Failure(val error: PageEditError) : PageEditResult()
}

/**
 * Allowlisted edit errors. Like [com.toolly.shared.capture.ScanError], these never contain
 * document pixels, paths, filenames or exception messages.
 */
sealed class PageEditError {
    /** [PageEditRequest.crop] does not describe a usable quadrilateral (e.g. degenerate/self-intersecting). */
    data object InvalidRegion : PageEditError()
    data object ProcessingFailed : PageEditError()
    data object StorageFailure : PageEditError()
}
