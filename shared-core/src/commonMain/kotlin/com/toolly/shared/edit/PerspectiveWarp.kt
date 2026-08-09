package com.toolly.shared.edit

import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Dewarps the quadrilateral described by a [CropRegion] into an upright rectangular page.
 *
 * Uses the standard unit-square-to-quadrilateral projective mapping (Heckbert, "Fundamentals of
 * Texture Mapping and Image Warping", 1989 -- the same construction Android's
 * `Matrix.setPolyToPoly` performs): output pixel `(u, v)` in `[0,1] x [0,1]` maps directly to a
 * source pixel inside the quad, so no separate matrix inversion is needed.
 */
object PerspectiveWarp {
    fun warp(source: PixelBuffer, crop: CropRegion, outputWidth: Int, outputHeight: Int): PixelBuffer {
        require(outputWidth > 0 && outputHeight > 0) { "output dimensions must be positive" }
        val coefficients = quadCoefficients(source, crop)

        val outArgb = IntArray(outputWidth * outputHeight)
        for (py in 0 until outputHeight) {
            val v = (py + 0.5f) / outputHeight
            for (px in 0 until outputWidth) {
                val u = (px + 0.5f) / outputWidth
                val (sx, sy) = coefficients.mapUnitSquareToSource(u, v)
                outArgb[py * outputWidth + px] = sampleBilinear(source, sx, sy)
            }
        }
        return PixelBuffer(outputWidth, outputHeight, outArgb)
    }

    private fun quadCoefficients(source: PixelBuffer, crop: CropRegion): QuadCoefficients {
        val x0 = crop.topLeft.x * source.width
        val y0 = crop.topLeft.y * source.height
        val x1 = crop.topRight.x * source.width
        val y1 = crop.topRight.y * source.height
        val x2 = crop.bottomRight.x * source.width
        val y2 = crop.bottomRight.y * source.height
        val x3 = crop.bottomLeft.x * source.width
        val y3 = crop.bottomLeft.y * source.height

        val dx1 = x1 - x2
        val dx2 = x3 - x2
        val dx3 = x0 - x1 + x2 - x3
        val dy1 = y1 - y2
        val dy2 = y3 - y2
        val dy3 = y0 - y1 + y2 - y3

        if (dx3 == 0f && dy3 == 0f) {
            // Affine case: the quad is already a parallelogram (g = h = 0).
            return QuadCoefficients(
                a = x1 - x0, b = x2 - x1, c = x0,
                d = y1 - y0, e = y2 - y1, f = y0,
                g = 0f, h = 0f,
            )
        }

        val denominator = dx1 * dy2 - dx2 * dy1
        require(denominator != 0f) {
            "Crop region does not describe a usable quadrilateral"
        }
        val g = (dx3 * dy2 - dx2 * dy3) / denominator
        val h = (dx1 * dy3 - dx3 * dy1) / denominator
        return QuadCoefficients(
            a = x1 - x0 + g * x1, b = x3 - x0 + h * x3, c = x0,
            d = y1 - y0 + g * y1, e = y3 - y0 + h * y3, f = y0,
            g = g, h = h,
        )
    }

    private data class QuadCoefficients(
        val a: Float, val b: Float, val c: Float,
        val d: Float, val e: Float, val f: Float,
        val g: Float, val h: Float,
    ) {
        fun mapUnitSquareToSource(u: Float, v: Float): Pair<Float, Float> {
            val denominator = g * u + h * v + 1f
            val x = (a * u + b * v + c) / denominator
            val y = (d * u + e * v + f) / denominator
            return x to y
        }
    }

    private fun sampleBilinear(source: PixelBuffer, x: Float, y: Float): Int {
        val clampedX = x.coerceIn(0f, (source.width - 1).toFloat())
        val clampedY = y.coerceIn(0f, (source.height - 1).toFloat())
        val x0 = floor(clampedX).toInt()
        val y0 = floor(clampedY).toInt()
        val x1 = min(x0 + 1, source.width - 1)
        val y1 = min(y0 + 1, source.height - 1)
        val fx = clampedX - x0
        val fy = clampedY - y0

        fun channel(shift: Int): Int {
            val c00 = (source[x0, y0] ushr shift) and 0xFF
            val c10 = (source[x1, y0] ushr shift) and 0xFF
            val c01 = (source[x0, y1] ushr shift) and 0xFF
            val c11 = (source[x1, y1] ushr shift) and 0xFF
            val top = c00 + (c10 - c00) * fx
            val bottom = c01 + (c11 - c01) * fx
            return (top + (bottom - top) * fy).roundToInt().coerceIn(0, 255)
        }

        val alpha = channel(24)
        val red = channel(16)
        val green = channel(8)
        val blue = channel(0)
        return (alpha shl 24) or (red shl 16) or (green shl 8) or blue
    }
}
