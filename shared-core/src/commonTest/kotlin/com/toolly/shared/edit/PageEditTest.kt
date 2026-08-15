package com.toolly.shared.edit

import com.toolly.shared.capture.TemporaryAssetId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class PageEditTest {
    @Test
    fun pixelBufferValidatesDimensionsAndArraySize() {
        assertFailsWith<IllegalArgumentException> { PixelBuffer(0, 4, IntArray(0)) }
        assertFailsWith<IllegalArgumentException> { PixelBuffer(4, 4, IntArray(4)) }
    }

    @Test
    fun pixelBufferEqualityComparesContentNotReference() {
        val a = PixelBuffer(2, 1, intArrayOf(0xFF000000.toInt(), 0xFFFFFFFF.toInt()))
        val b = PixelBuffer(2, 1, intArrayOf(0xFF000000.toInt(), 0xFFFFFFFF.toInt()))
        val c = PixelBuffer(2, 1, intArrayOf(0xFFFFFFFF.toInt(), 0xFF000000.toInt()))

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, c)
    }

    @Test
    fun cropRegionRejectsOutOfRangeCoordinates() {
        assertFailsWith<IllegalArgumentException> { NormalizedPoint(-0.1f, 0f) }
        assertFailsWith<IllegalArgumentException> { NormalizedPoint(0f, 1.1f) }
    }

    @Test
    fun identityWarpOfASolidColorImageReproducesTheSameColor() {
        val color = 0xFF224466.toInt()
        val source = PixelBuffer.solid(width = 20, height = 30, argb = color)
        val fullFrame = CropRegion(
            topLeft = NormalizedPoint(0f, 0f),
            topRight = NormalizedPoint(1f, 0f),
            bottomRight = NormalizedPoint(1f, 1f),
            bottomLeft = NormalizedPoint(0f, 1f),
        )

        val warped = PerspectiveWarp.warp(source, fullFrame, outputWidth = 20, outputHeight = 30)

        assertEquals(source.width, warped.width)
        assertEquals(source.height, warped.height)
        assertEquals(PixelBuffer.solid(20, 30, color), warped)
    }

    @Test
    fun degenerateCropRegionIsRejected() {
        val source = PixelBuffer.solid(width = 10, height = 10, argb = 0xFFFFFFFF.toInt())
        val degenerate = CropRegion(
            topLeft = NormalizedPoint(0f, 0f),
            topRight = NormalizedPoint(0.3f, 0.5f),
            bottomRight = NormalizedPoint(0.7f, 0.5f),
            bottomLeft = NormalizedPoint(0.5f, 0.5f),
        )

        assertFailsWith<IllegalArgumentException> {
            PerspectiveWarp.warp(source, degenerate, outputWidth = 10, outputHeight = 10)
        }
    }

    @Test
    fun grayscaleAtFullIntensityMatchesLuminanceOnAllChannels() {
        val red = 0xFFFF0000.toInt()
        val source = PixelBuffer.solid(width = 2, height = 2, argb = red)

        val result = ColorAdjust.apply(source, EnhancementMode.GRAY, intensity = 1f)

        val pixel = result[0, 0]
        val r = (pixel ushr 16) and 0xFF
        val g = (pixel ushr 8) and 0xFF
        val b = pixel and 0xFF
        assertEquals(r, g)
        assertEquals(g, b)
        // Luminance of pure red (0.299 weight): round(0.299 * 255) = 76.
        assertEquals(76, r)
    }

    @Test
    fun grayscaleAtZeroIntensityLeavesColorUnchanged() {
        val color = 0xFF33CC99.toInt()
        val source = PixelBuffer.solid(width = 1, height = 1, argb = color)

        val result = ColorAdjust.apply(source, EnhancementMode.GRAY, intensity = 0f)

        assertEquals(source, result)
    }

    @Test
    fun blackAndWhiteThresholdsToPureExtremes() {
        val white = PixelBuffer.solid(1, 1, 0xFFFFFFFF.toInt())
        val black = PixelBuffer.solid(1, 1, 0xFF000000.toInt())

        val whiteResult = ColorAdjust.apply(white, EnhancementMode.BLACK_AND_WHITE, intensity = 0.5f)
        val blackResult = ColorAdjust.apply(black, EnhancementMode.BLACK_AND_WHITE, intensity = 0.5f)

        assertEquals(0xFFFFFFFF.toInt(), whiteResult[0, 0])
        assertEquals(0xFF000000.toInt(), blackResult[0, 0])
    }

    @Test
    fun colorAdjustPreservesAlpha() {
        val translucentBlue = 0x80_00_00_FF.toInt()
        val source = PixelBuffer.solid(1, 1, translucentBlue)

        val result = ColorAdjust.apply(source, EnhancementMode.CLEAN, intensity = 0.5f)

        assertEquals(0x80, (result[0, 0] ushr 24) and 0xFF)
    }

    @Test
    fun pageEditRequestRejectsOutOfRangeIntensity() {
        val assetId = TemporaryAssetId("0123456789abcdef0123456789abcdef")
        assertFailsWith<IllegalArgumentException> {
            PageEditRequest(assetId, crop = null, mode = EnhancementMode.AUTO, intensity = 1.1f)
        }
    }

    @Test
    fun pageEditRequestDefaultsToNoRotation() {
        val assetId = TemporaryAssetId("0123456789abcdef0123456789abcdef")
        val request = PageEditRequest(assetId, crop = null, mode = EnhancementMode.AUTO, intensity = 0f)
        assertEquals(0, request.rotationQuarterTurns)
    }

    @Test
    fun rotate90SwapsWidthAndHeight() {
        val source = PixelBuffer(width = 3, height = 2, argb = IntArray(6) { it })

        val rotated = ImageRotation.rotate90(source, quarterTurns = 1)

        assertEquals(2, rotated.width)
        assertEquals(3, rotated.height)
    }

    @Test
    fun rotate90MovesTopLeftPixelToTopRight() {
        // 2x2 source: 0 1
        //             2 3
        val source = PixelBuffer(width = 2, height = 2, argb = intArrayOf(0, 1, 2, 3))

        val rotated = ImageRotation.rotate90(source, quarterTurns = 1)

        // A 90-degree clockwise rotation of:
        //   0 1        2 0
        //   2 3   ->   3 1
        assertEquals(2, rotated[0, 0])
        assertEquals(0, rotated[1, 0])
        assertEquals(3, rotated[0, 1])
        assertEquals(1, rotated[1, 1])
    }

    @Test
    fun rotate90FourTimesIsIdentity() {
        val source = PixelBuffer(width = 4, height = 3, argb = IntArray(12) { it * 17 })

        val rotated = ImageRotation.rotate90(source, quarterTurns = 4)

        assertEquals(source, rotated)
    }

    @Test
    fun rotate90NegativeQuarterTurnsNormalizesModulo4() {
        val source = PixelBuffer(width = 4, height = 3, argb = IntArray(12) { it * 5 })

        val clockwiseThree = ImageRotation.rotate90(source, quarterTurns = 3)
        val counterClockwiseOne = ImageRotation.rotate90(source, quarterTurns = -1)

        assertEquals(clockwiseThree, counterClockwiseOne)
    }
}
