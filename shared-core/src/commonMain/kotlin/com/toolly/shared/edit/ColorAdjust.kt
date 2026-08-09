package com.toolly.shared.edit

import kotlin.math.roundToInt

/**
 * Applies an [EnhancementMode] finish to a [PixelBuffer], matching wireframe `1.4 Clean and save`
 * (Auto/Clean/Color/Gray/B&W with an intensity slider). Pure per-pixel arithmetic -- no platform
 * image APIs -- so it runs identically on every KMP target.
 */
object ColorAdjust {
    fun apply(source: PixelBuffer, mode: EnhancementMode, intensity: Float): PixelBuffer {
        require(intensity in 0f..1f) { "intensity must be between 0 and 1" }
        val transform: (Int, Int, Int) -> Triple<Int, Int, Int> = when (mode) {
            EnhancementMode.COLOR -> { r, g, b -> saturate(r, g, b, intensity) }
            EnhancementMode.GRAY -> { r, g, b -> desaturate(r, g, b, intensity) }
            EnhancementMode.BLACK_AND_WHITE -> { r, g, b -> threshold(r, g, b, intensity) }
            // AUTO picks the same cleanup curve as CLEAN -- the app's own choice of "best default,"
            // not a distinct algorithm.
            EnhancementMode.CLEAN, EnhancementMode.AUTO -> { r, g, b -> cleanContrast(r, g, b, intensity) }
        }

        val out = IntArray(source.argb.size)
        for (i in source.argb.indices) {
            val pixel = source.argb[i]
            val alpha = (pixel ushr 24) and 0xFF
            val red = (pixel ushr 16) and 0xFF
            val green = (pixel ushr 8) and 0xFF
            val blue = pixel and 0xFF
            val (r, g, b) = transform(red, green, blue)
            out[i] = (alpha shl 24) or (r shl 16) or (g shl 8) or b
        }
        return PixelBuffer(source.width, source.height, out)
    }

    private fun luminance(r: Int, g: Int, b: Int): Int =
        (LUMA_R * r + LUMA_G * g + LUMA_B * b).roundToInt().coerceIn(0, 255)

    private fun saturate(r: Int, g: Int, b: Int, intensity: Float): Triple<Int, Int, Int> {
        val gray = luminance(r, g, b)
        val factor = 1f + intensity * 0.3f
        fun boost(channel: Int) = (gray + (channel - gray) * factor).roundToInt().coerceIn(0, 255)
        return Triple(boost(r), boost(g), boost(b))
    }

    private fun desaturate(r: Int, g: Int, b: Int, intensity: Float): Triple<Int, Int, Int> {
        val gray = luminance(r, g, b)
        fun blend(channel: Int) = (channel + (gray - channel) * intensity).roundToInt().coerceIn(0, 255)
        return Triple(blend(r), blend(g), blend(b))
    }

    private fun threshold(r: Int, g: Int, b: Int, intensity: Float): Triple<Int, Int, Int> {
        // intensity 0 -> lenient threshold (more pixels stay white); intensity 1 -> strict.
        val cutoff = (200 - intensity * 100).roundToInt().coerceIn(50, 220)
        val value = if (luminance(r, g, b) >= cutoff) 255 else 0
        return Triple(value, value, value)
    }

    private fun cleanContrast(r: Int, g: Int, b: Int, intensity: Float): Triple<Int, Int, Int> {
        val contrastFactor = 1f + intensity * 0.5f
        val brightnessBoost = intensity * 15f
        fun adjust(channel: Int) =
            ((channel - 128) * contrastFactor + 128 + brightnessBoost).roundToInt().coerceIn(0, 255)
        return Triple(adjust(r), adjust(g), adjust(b))
    }

    private const val LUMA_R = 0.299f
    private const val LUMA_G = 0.587f
    private const val LUMA_B = 0.114f
}
