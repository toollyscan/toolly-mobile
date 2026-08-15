package com.toolly.shared.edit

/**
 * Rotates a [PixelBuffer] by a multiple of 90 degrees clockwise. Pure `commonMain` pixel math,
 * same shape as [PerspectiveWarp]/[ColorAdjust] -- only JPEG decode/encode is platform-specific.
 *
 * Applied *before* [PerspectiveWarp.warp] in the edit pipeline: [CropRegion] corners are always
 * relative to whatever orientation the user is currently looking at, so rotation has to land on
 * the source pixels first, not be smuggled into the crop math.
 */
object ImageRotation {
    /** [quarterTurns] is normalized modulo 4 -- negative values rotate counter-clockwise. */
    fun rotate90(source: PixelBuffer, quarterTurns: Int): PixelBuffer {
        val turns = ((quarterTurns % 4) + 4) % 4
        var result = source
        repeat(turns) { result = rotateOnce(result) }
        return result
    }

    private fun rotateOnce(source: PixelBuffer): PixelBuffer {
        val outWidth = source.height
        val outHeight = source.width
        val outArgb = IntArray(outWidth * outHeight)
        for (y in 0 until source.height) {
            for (x in 0 until source.width) {
                // (x, y) in the source lands at (height-1-y, x) in a 90-degree-clockwise rotation.
                val outX = source.height - 1 - y
                val outY = x
                outArgb[outY * outWidth + outX] = source[x, y]
            }
        }
        return PixelBuffer(outWidth, outHeight, outArgb)
    }
}
