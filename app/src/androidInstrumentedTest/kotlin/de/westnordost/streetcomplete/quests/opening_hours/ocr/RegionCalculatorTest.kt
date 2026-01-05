package de.westnordost.streetcomplete.quests.opening_hours.ocr

import android.graphics.Bitmap
import android.graphics.RectF
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Instrumented tests for RegionCalculator.
 * Tests the coordinate transformation logic for ContentScale.Fit letterboxing
 * and bitmap cropping functionality.
 */
class RegionCalculatorTest {

    // ==================== calculateBoundingBox tests ====================

    @Test
    fun calculateBoundingBox_emptyPoints_returnsNull() {
        val result = RegionCalculator.calculateBoundingBox(
            points = emptyList(),
            canvasWidth = 1080,
            canvasHeight = 1920,
            imageWidth = 4000,
            imageHeight = 3000
        )
        assertNull(result)
    }

    @Test
    fun calculateBoundingBox_invalidCanvasSize_returnsNull() {
        val points = listOf(RegionCalculator.Point(100f, 100f))
        assertNull(RegionCalculator.calculateBoundingBox(points, 0, 1920, 4000, 3000))
        assertNull(RegionCalculator.calculateBoundingBox(points, 1080, 0, 4000, 3000))
    }

    @Test
    fun calculateBoundingBox_invalidImageSize_returnsNull() {
        val points = listOf(RegionCalculator.Point(100f, 100f))
        assertNull(RegionCalculator.calculateBoundingBox(points, 1080, 1920, 0, 3000))
        assertNull(RegionCalculator.calculateBoundingBox(points, 1080, 1920, 4000, 0))
    }

    @Test
    fun calculateBoundingBox_singlePoint_returnsSinglePointRect() {
        // Canvas is 1080x1920 (phone portrait)
        // Image is 4000x3000 (landscape photo)
        // Image is wider - letterbox top/bottom
        val points = listOf(RegionCalculator.Point(540f, 960f)) // center of canvas

        val result = RegionCalculator.calculateBoundingBox(
            points = points,
            canvasWidth = 1080,
            canvasHeight = 1920,
            imageWidth = 4000,
            imageHeight = 3000
        )

        assertNotNull(result)
        // With a single point, left==right and top==bottom
        assertEquals(result.left, result.right)
        assertEquals(result.top, result.bottom)
    }

    @Test
    fun calculateBoundingBox_landscapeImagePortraitCanvas_correctTransform() {
        // Canvas: 1080x1920 (phone portrait)
        // Image: 4000x3000 (4:3 landscape photo)
        // Expected: Image fills width, letterboxed top/bottom
        // displayedWidth = 1080, displayedHeight = 1080 / (4000/3000) = 810
        // offsetY = (1920 - 810) / 2 = 555

        val canvasWidth = 1080
        val canvasHeight = 1920
        val imageWidth = 4000
        val imageHeight = 3000

        // Draw a box in the center of the displayed image area
        // Displayed image is from y=555 to y=1365 (height 810)
        val points = listOf(
            RegionCalculator.Point(100f, 600f),
            RegionCalculator.Point(200f, 700f)
        )

        val result = RegionCalculator.calculateBoundingBox(
            points = points,
            canvasWidth = canvasWidth,
            canvasHeight = canvasHeight,
            imageWidth = imageWidth,
            imageHeight = imageHeight
        )

        assertNotNull(result)

        // Verify the result is within image bounds
        assertTrue(result.left >= 0f)
        assertTrue(result.top >= 0f)
        assertTrue(result.right <= imageWidth)
        assertTrue(result.bottom <= imageHeight)

        // Scale factor: 4000 / 1080 = 3.7037...
        // Canvas x 100 -> image x = 100 * 3.7037 = 370.37
        // Canvas x 200 -> image x = 200 * 3.7037 = 740.74
        val expectedScaleX = imageWidth.toFloat() / canvasWidth.toFloat()
        val displayedHeight = canvasWidth.toFloat() / (imageWidth.toFloat() / imageHeight)
        val offsetY = (canvasHeight - displayedHeight) / 2
        val expectedScaleY = imageHeight.toFloat() / displayedHeight

        // Adjust for letterbox offset
        val adjustedMinY = 600f - offsetY
        val adjustedMaxY = 700f - offsetY

        val expectedLeft = 100f * expectedScaleX
        val expectedRight = 200f * expectedScaleX
        val expectedTop = adjustedMinY * expectedScaleY
        val expectedBottom = adjustedMaxY * expectedScaleY

        assertEquals(expectedLeft, result.left, 1f)
        assertEquals(expectedRight, result.right, 1f)
        assertEquals(expectedTop.coerceIn(0f, imageHeight.toFloat()), result.top, 1f)
        assertEquals(expectedBottom.coerceIn(0f, imageHeight.toFloat()), result.bottom, 1f)
    }

    @Test
    fun calculateBoundingBox_portraitImageLandscapeCanvas_correctTransform() {
        // Canvas: 1920x1080 (phone landscape)
        // Image: 3000x4000 (3:4 portrait photo)
        // Expected: Image fills height, letterboxed left/right
        // displayedHeight = 1080, displayedWidth = 1080 * (3000/4000) = 810
        // offsetX = (1920 - 810) / 2 = 555

        val canvasWidth = 1920
        val canvasHeight = 1080
        val imageWidth = 3000
        val imageHeight = 4000

        // Draw a box in the center of the displayed image area
        val points = listOf(
            RegionCalculator.Point(600f, 100f),
            RegionCalculator.Point(700f, 200f)
        )

        val result = RegionCalculator.calculateBoundingBox(
            points = points,
            canvasWidth = canvasWidth,
            canvasHeight = canvasHeight,
            imageWidth = imageWidth,
            imageHeight = imageHeight
        )

        assertNotNull(result)

        // Verify the result is within image bounds
        assertTrue(result.left >= 0f)
        assertTrue(result.top >= 0f)
        assertTrue(result.right <= imageWidth)
        assertTrue(result.bottom <= imageHeight)
    }

    @Test
    fun calculateBoundingBox_squareImageSquareCanvas_noLetterboxing() {
        // When aspect ratios match, there should be no letterboxing
        val canvasWidth = 1000
        val canvasHeight = 1000
        val imageWidth = 2000
        val imageHeight = 2000

        val points = listOf(
            RegionCalculator.Point(250f, 250f),
            RegionCalculator.Point(750f, 750f)
        )

        val result = RegionCalculator.calculateBoundingBox(
            points = points,
            canvasWidth = canvasWidth,
            canvasHeight = canvasHeight,
            imageWidth = imageWidth,
            imageHeight = imageHeight
        )

        assertNotNull(result)

        // Scale is 2x in both directions
        assertEquals(500f, result.left, 1f)
        assertEquals(500f, result.top, 1f)
        assertEquals(1500f, result.right, 1f)
        assertEquals(1500f, result.bottom, 1f)
    }

    @Test
    fun calculateBoundingBox_pointsOutsideDisplayArea_clamped() {
        // Canvas: 1080x1920
        // Image: 4000x3000 (landscape)
        // Displayed: width=1080, height=810, offsetY=555
        // Points outside the displayed image area should be clamped

        val canvasWidth = 1080
        val canvasHeight = 1920
        val imageWidth = 4000
        val imageHeight = 3000

        // Point at y=0 is above the letterboxed image (which starts at y=555)
        val points = listOf(
            RegionCalculator.Point(0f, 0f),
            RegionCalculator.Point(1080f, 1920f)
        )

        val result = RegionCalculator.calculateBoundingBox(
            points = points,
            canvasWidth = canvasWidth,
            canvasHeight = canvasHeight,
            imageWidth = imageWidth,
            imageHeight = imageHeight
        )

        assertNotNull(result)

        // Result should be clamped to image bounds
        assertTrue(result.left >= 0f)
        assertTrue(result.top >= 0f)
        assertTrue(result.right <= imageWidth)
        assertTrue(result.bottom <= imageHeight)
    }

    // ==================== calculateDisplayParameters tests ====================

    @Test
    fun calculateDisplayParameters_landscapeInPortrait_letterboxTopBottom() {
        val params = RegionCalculator.calculateDisplayParameters(
            canvasWidth = 1080,
            canvasHeight = 1920,
            imageWidth = 4000,
            imageHeight = 3000
        )

        assertNotNull(params)
        assertEquals(1080f, params.displayedWidth, 0.1f)
        assertEquals(810f, params.displayedHeight, 0.1f)
        assertEquals(0f, params.offsetX, 0.1f)
        assertEquals(555f, params.offsetY, 0.1f)
    }

    @Test
    fun calculateDisplayParameters_portraitInLandscape_letterboxLeftRight() {
        val params = RegionCalculator.calculateDisplayParameters(
            canvasWidth = 1920,
            canvasHeight = 1080,
            imageWidth = 3000,
            imageHeight = 4000
        )

        assertNotNull(params)
        assertEquals(810f, params.displayedWidth, 0.1f)
        assertEquals(1080f, params.displayedHeight, 0.1f)
        assertEquals(555f, params.offsetX, 0.1f)
        assertEquals(0f, params.offsetY, 0.1f)
    }

    @Test
    fun calculateDisplayParameters_sameAspectRatio_noLetterboxing() {
        val params = RegionCalculator.calculateDisplayParameters(
            canvasWidth = 1000,
            canvasHeight = 1000,
            imageWidth = 2000,
            imageHeight = 2000
        )

        assertNotNull(params)
        assertEquals(1000f, params.displayedWidth, 0.1f)
        assertEquals(1000f, params.displayedHeight, 0.1f)
        assertEquals(0f, params.offsetX, 0.1f)
        assertEquals(0f, params.offsetY, 0.1f)
    }

    @Test
    fun calculateDisplayParameters_invalidDimensions_returnsNull() {
        assertNull(RegionCalculator.calculateDisplayParameters(0, 1000, 2000, 2000))
        assertNull(RegionCalculator.calculateDisplayParameters(1000, 0, 2000, 2000))
        assertNull(RegionCalculator.calculateDisplayParameters(1000, 1000, 0, 2000))
        assertNull(RegionCalculator.calculateDisplayParameters(1000, 1000, 2000, 0))
    }

    // ==================== cropBitmap tests ====================

    @Test
    fun cropBitmap_validRegion_returnsCroppedBitmap() {
        // Create a test bitmap
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val region = RectF(25f, 25f, 75f, 75f)

        val result = RegionCalculator.cropBitmap(bitmap, region)

        assertNotNull(result)
        assertEquals(50, result.width)
        assertEquals(50, result.height)

        bitmap.recycle()
        result.recycle()
    }

    @Test
    fun cropBitmap_regionAtEdge_returnsCroppedBitmap() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val region = RectF(0f, 0f, 50f, 50f)

        val result = RegionCalculator.cropBitmap(bitmap, region)

        assertNotNull(result)
        assertEquals(50, result.width)
        assertEquals(50, result.height)

        bitmap.recycle()
        result.recycle()
    }

    @Test
    fun cropBitmap_regionExceedsBounds_clamped() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        // Region extends beyond bitmap bounds
        val region = RectF(-10f, -10f, 110f, 110f)

        val result = RegionCalculator.cropBitmap(bitmap, region)

        assertNotNull(result)
        // Should be clamped to the bitmap size
        assertEquals(100, result.width)
        assertEquals(100, result.height)

        bitmap.recycle()
        result.recycle()
    }

    @Test
    fun cropBitmap_zeroWidthRegion_returnsMinimumWidth() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        // Zero width region - should be coerced to at least 1 pixel
        val region = RectF(50f, 25f, 50f, 75f)

        val result = RegionCalculator.cropBitmap(bitmap, region)

        // The implementation coerces right to be at least left+1
        assertNotNull(result)
        assertEquals(1, result.width)
        assertEquals(50, result.height)

        bitmap.recycle()
        result.recycle()
    }
}
