package com.toolly.shared.edit

/**
 * A decoded image as packed ARGB_8888 pixels (0xAARRGGBB per element, row-major), independent of
 * any platform bitmap type. [PerspectiveWarp] and [ColorAdjust] operate purely on this type so the
 * same crop/enhancement math runs identically on every KMP target -- only decode-to/encode-from
 * JPEG is platform-specific.
 */
class PixelBuffer(val width: Int, val height: Int, val argb: IntArray) {
    init {
        require(width > 0 && height > 0) { "width and height must be positive" }
        require(argb.size == width * height) { "argb size must equal width * height" }
    }

    operator fun get(x: Int, y: Int): Int = argb[y * width + x]

    override fun equals(other: Any?): Boolean =
        other is PixelBuffer &&
            width == other.width &&
            height == other.height &&
            argb.contentEquals(other.argb)

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        result = 31 * result + argb.contentHashCode()
        return result
    }

    companion object {
        fun solid(width: Int, height: Int, argb: Int): PixelBuffer =
            PixelBuffer(width, height, IntArray(width * height) { argb })
    }
}
