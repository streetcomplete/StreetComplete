package de.westnordost.streetcomplete.util.ktx

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import de.westnordost.streetcomplete.util.sdf.convertToSdf
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

fun Bitmap.flipHorizontally(): Bitmap {
    val matrix = Matrix().apply { postScale(-1f, 1f, width / 2f, width / 2f) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

fun Bitmap.toSdf(radius: Double = 8.0, cutoff: Double = 0.25): Bitmap {
    val buffer = ceil(radius * (1.0 - cutoff)).toInt()
    val w = width + 2 * buffer
    val h = height + 2 * buffer
    val pixels = IntArray(w * h)
    getPixels(pixels, w * buffer + buffer, w, 0, 0, width, height)
    convertToSdf(pixels, w, radius, cutoff)
    return Bitmap.createBitmap(pixels, w, pixels.size / w, Bitmap.Config.ARGB_8888)
}

/**
 * Enhances contrast using ColorMatrix (GPU-accelerated).
 * Good for improving text visibility before OCR.
 *
 * @param contrast The contrast multiplier (1.0 = no change, >1.0 = more contrast)
 */
fun Bitmap.enhanceContrast(contrast: Float = 1.5f): Bitmap {
    val translate = (-0.5f * contrast + 0.5f) * 255f
    val colorMatrix = ColorMatrix(
        floatArrayOf(
            contrast, 0f, 0f, 0f, translate,
            0f, contrast, 0f, 0f, translate,
            0f, 0f, contrast, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        )
    )

    val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)
    val paint = Paint().apply {
        colorFilter = ColorMatrixColorFilter(colorMatrix)
    }
    canvas.drawBitmap(this, 0f, 0f, paint)
    return result
}

/**
 * Converts bitmap to grayscale.
 */
fun Bitmap.toGrayscale(): Bitmap {
    val colorMatrix = ColorMatrix().apply { setSaturation(0f) }
    val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)
    val paint = Paint().apply {
        colorFilter = ColorMatrixColorFilter(colorMatrix)
    }
    canvas.drawBitmap(this, 0f, 0f, paint)
    return result
}

/**
 * Applies Sauvola adaptive thresholding for OCR preprocessing.
 * Sauvola is specifically designed for document/text binarization and
 * handles varying lighting conditions (shadows, reflections) well.
 *
 * Formula: threshold(x,y) = mean(x,y) * (1 + k * ((stddev(x,y) / R) - 1))
 *
 * @param windowSize The size of the local window for computing mean/stddev (odd number)
 * @param k Control factor for threshold sensitivity (0.2-0.5, higher = darker result)
 * @param r Dynamic range of standard deviation (typically 128 for 8-bit images)
 */
fun Bitmap.adaptiveThreshold(
    windowSize: Int = 31,
    k: Float = 0.3f,
    r: Float = 128f
): Bitmap {
    val w = width
    val h = height
    val pixels = IntArray(w * h)
    getPixels(pixels, 0, w, 0, 0, w, h)

    // Convert to grayscale values
    val grayPixels = IntArray(w * h)
    for (i in pixels.indices) {
        val pixel = pixels[i]
        val red = (pixel shr 16) and 0xFF
        val green = (pixel shr 8) and 0xFF
        val blue = pixel and 0xFF
        // Standard luminance formula
        grayPixels[i] = (0.299f * red + 0.587f * green + 0.114f * blue).toInt()
    }

    // Compute integral images for efficient local mean/variance calculation
    // Using Long arrays to avoid overflow
    val integral = LongArray(w * h)
    val integralSq = LongArray(w * h)
    computeIntegralImages(grayPixels, w, h, integral, integralSq)

    // Apply Sauvola threshold using integral images
    val resultPixels = IntArray(w * h)
    val half = windowSize / 2

    for (y in 0 until h) {
        for (x in 0 until w) {
            // Window bounds (clamped to image edges)
            val x1 = max(0, x - half)
            val y1 = max(0, y - half)
            val x2 = min(w - 1, x + half)
            val y2 = min(h - 1, y + half)

            val area = (x2 - x1 + 1) * (y2 - y1 + 1)

            // Get sum from integral image using inclusion-exclusion
            val sum = getIntegralSum(integral, w, x1, y1, x2, y2)
            val sumSq = getIntegralSum(integralSq, w, x1, y1, x2, y2)

            val mean = sum.toFloat() / area
            val variance = (sumSq.toFloat() / area) - (mean * mean)
            val stddev = sqrt(max(0f, variance))

            // Sauvola threshold formula
            val threshold = mean * (1f + k * ((stddev / r) - 1f))

            val gray = grayPixels[y * w + x]

            // Binary output: white (0xFFFFFFFF) or black (0xFF000000)
            resultPixels[y * w + x] = if (gray > threshold) {
                0xFFFFFFFF.toInt()  // White
            } else {
                0xFF000000.toInt()  // Black
            }
        }
    }

    return Bitmap.createBitmap(resultPixels, w, h, Bitmap.Config.ARGB_8888)
}

/**
 * Computes integral images for efficient local statistics calculation.
 * integral[i] = sum of all pixels from (0,0) to (x,y)
 * integralSq[i] = sum of squares for variance calculation
 */
private fun computeIntegralImages(
    pixels: IntArray,
    width: Int,
    height: Int,
    integral: LongArray,
    integralSq: LongArray
) {
    for (y in 0 until height) {
        for (x in 0 until width) {
            val idx = y * width + x
            val pixel = pixels[idx].toLong()
            val pixelSq = pixel * pixel

            var sum = pixel
            var sumSq = pixelSq

            if (x > 0) {
                sum += integral[y * width + x - 1]
                sumSq += integralSq[y * width + x - 1]
            }
            if (y > 0) {
                sum += integral[(y - 1) * width + x]
                sumSq += integralSq[(y - 1) * width + x]
            }
            if (x > 0 && y > 0) {
                sum -= integral[(y - 1) * width + x - 1]
                sumSq -= integralSq[(y - 1) * width + x - 1]
            }

            integral[idx] = sum
            integralSq[idx] = sumSq
        }
    }
}

/**
 * Gets the sum of a rectangular region from an integral image using inclusion-exclusion.
 */
private fun getIntegralSum(integral: LongArray, width: Int, x1: Int, y1: Int, x2: Int, y2: Int): Long {
    var sum = integral[y2 * width + x2]
    if (x1 > 0) {
        sum -= integral[y2 * width + x1 - 1]
    }
    if (y1 > 0) {
        sum -= integral[(y1 - 1) * width + x2]
    }
    if (x1 > 0 && y1 > 0) {
        sum += integral[(y1 - 1) * width + x1 - 1]
    }
    return sum
}
