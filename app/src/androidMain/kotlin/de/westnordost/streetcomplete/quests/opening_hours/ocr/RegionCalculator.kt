package de.westnordost.streetcomplete.quests.opening_hours.ocr

import android.graphics.Bitmap
import android.graphics.RectF

/**
 * Utility object for calculating image regions from canvas coordinates,
 * accounting for ContentScale.Fit letterboxing.
 *
 * Extracted from PhotoAnnotationScreen for testability.
 */
object RegionCalculator {

    /**
     * Represents a point in canvas space.
     */
    data class Point(val x: Float, val y: Float)

    /**
     * Calculates a bounding box in image coordinates from a list of canvas coordinate points.
     * Accounts for ContentScale.Fit letterboxing.
     *
     * @param points List of points in canvas coordinates
     * @param canvasWidth Width of the canvas in pixels
     * @param canvasHeight Height of the canvas in pixels
     * @param imageWidth Width of the actual image in pixels
     * @param imageHeight Height of the actual image in pixels
     * @return RectF in image coordinates, or null if points list is empty or dimensions are invalid
     */
    fun calculateBoundingBox(
        points: List<Point>,
        canvasWidth: Int,
        canvasHeight: Int,
        imageWidth: Int,
        imageHeight: Int
    ): RectF? {
        if (points.isEmpty() || canvasWidth <= 0 || canvasHeight <= 0 ||
            imageWidth <= 0 || imageHeight <= 0) {
            return null
        }

        val minX = points.minOf { it.x }
        val maxX = points.maxOf { it.x }
        val minY = points.minOf { it.y }
        val maxY = points.maxOf { it.y }

        // Calculate how ContentScale.Fit positions the image
        val canvasAspect = canvasWidth.toFloat() / canvasHeight
        val imageAspect = imageWidth.toFloat() / imageHeight

        val displayedWidth: Float
        val displayedHeight: Float
        val offsetX: Float
        val offsetY: Float

        if (imageAspect > canvasAspect) {
            // Image is wider than canvas - letterbox top/bottom
            displayedWidth = canvasWidth.toFloat()
            displayedHeight = canvasWidth.toFloat() / imageAspect
            offsetX = 0f
            offsetY = (canvasHeight - displayedHeight) / 2
        } else {
            // Image is taller than canvas - letterbox left/right
            displayedHeight = canvasHeight.toFloat()
            displayedWidth = canvasHeight.toFloat() * imageAspect
            offsetX = (canvasWidth - displayedWidth) / 2
            offsetY = 0f
        }

        // Convert canvas coordinates to image coordinates
        // First, adjust for the offset (letterboxing)
        val adjustedMinX = minX - offsetX
        val adjustedMaxX = maxX - offsetX
        val adjustedMinY = minY - offsetY
        val adjustedMaxY = maxY - offsetY

        // Then scale from displayed size to actual image size
        val scaleX = imageWidth.toFloat() / displayedWidth
        val scaleY = imageHeight.toFloat() / displayedHeight

        return RectF(
            (adjustedMinX * scaleX).coerceIn(0f, imageWidth.toFloat()),
            (adjustedMinY * scaleY).coerceIn(0f, imageHeight.toFloat()),
            (adjustedMaxX * scaleX).coerceIn(0f, imageWidth.toFloat()),
            (adjustedMaxY * scaleY).coerceIn(0f, imageHeight.toFloat())
        )
    }

    /**
     * Crops a bitmap to the specified region.
     *
     * @param sourceBitmap The source bitmap to crop
     * @param region The region in image coordinates to crop
     * @return The cropped bitmap, or null if the region is invalid
     */
    fun cropBitmap(sourceBitmap: Bitmap, region: RectF): Bitmap? {
        val left = region.left.toInt().coerceIn(0, sourceBitmap.width - 1)
        val top = region.top.toInt().coerceIn(0, sourceBitmap.height - 1)
        val right = region.right.toInt().coerceIn(left + 1, sourceBitmap.width)
        val bottom = region.bottom.toInt().coerceIn(top + 1, sourceBitmap.height)

        val width = right - left
        val height = bottom - top

        if (width <= 0 || height <= 0) return null

        return try {
            Bitmap.createBitmap(sourceBitmap, left, top, width, height)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Calculates the display parameters for ContentScale.Fit.
     * Useful for debugging and testing.
     *
     * @return Tuple of (displayedWidth, displayedHeight, offsetX, offsetY)
     */
    fun calculateDisplayParameters(
        canvasWidth: Int,
        canvasHeight: Int,
        imageWidth: Int,
        imageHeight: Int
    ): DisplayParameters? {
        if (canvasWidth <= 0 || canvasHeight <= 0 || imageWidth <= 0 || imageHeight <= 0) {
            return null
        }

        val canvasAspect = canvasWidth.toFloat() / canvasHeight
        val imageAspect = imageWidth.toFloat() / imageHeight

        return if (imageAspect > canvasAspect) {
            // Image is wider than canvas - letterbox top/bottom
            val displayedWidth = canvasWidth.toFloat()
            val displayedHeight = canvasWidth.toFloat() / imageAspect
            val offsetX = 0f
            val offsetY = (canvasHeight - displayedHeight) / 2
            DisplayParameters(displayedWidth, displayedHeight, offsetX, offsetY)
        } else {
            // Image is taller than canvas - letterbox left/right
            val displayedHeight = canvasHeight.toFloat()
            val displayedWidth = canvasHeight.toFloat() * imageAspect
            val offsetX = (canvasWidth - displayedWidth) / 2
            val offsetY = 0f
            DisplayParameters(displayedWidth, displayedHeight, offsetX, offsetY)
        }
    }

    data class DisplayParameters(
        val displayedWidth: Float,
        val displayedHeight: Float,
        val offsetX: Float,
        val offsetY: Float
    )
}
