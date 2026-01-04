package de.westnordost.streetcomplete.quests.opening_hours.ocr

import android.graphics.Bitmap
import android.graphics.RectF
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import kotlin.math.max
import kotlin.math.min

/**
 * Processes images using ML Kit OCR to extract text from highlighted regions.
 * Filters results to only return numeric characters.
 */
class OcrProcessor {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Extracts only numeric characters from the specified region of the bitmap.
     *
     * @param bitmap The full image bitmap
     * @param region The bounding box region to scan (in image coordinates)
     * @return String containing only the digits found in the region
     */
    suspend fun extractNumbersFromRegion(bitmap: Bitmap, region: RectF): String {
        // Ensure region is within bitmap bounds
        val left = max(0, region.left.toInt())
        val top = max(0, region.top.toInt())
        val right = min(bitmap.width, region.right.toInt())
        val bottom = min(bitmap.height, region.bottom.toInt())

        val width = right - left
        val height = bottom - top

        if (width <= 0 || height <= 0) {
            return ""
        }

        // Crop the bitmap to the specified region
        val croppedBitmap = Bitmap.createBitmap(bitmap, left, top, width, height)

        return try {
            val inputImage = InputImage.fromBitmap(croppedBitmap, 0)
            val result = recognizer.process(inputImage).await()

            // Extract only numeric characters from the OCR result
            result.text.filter { it.isDigit() }
        } catch (e: Exception) {
            // Return empty string on any error
            ""
        }
    }

    /**
     * Parses raw OCR numbers into hour and minute values.
     *
     * Handles various formats:
     * - "8" or "08" → 8:00
     * - "830" → 8:30
     * - "0830" or "8:30" → 8:30
     *
     * @param raw The raw numeric string from OCR
     * @param isAm Whether to interpret as AM (for 12-hour format)
     * @param is12HourMode Whether we're in 12-hour mode
     * @return Pair of (hour in 24-hour format, minute), or null if parsing fails
     */
    fun parseTimeNumbers(raw: String, isAm: Boolean, is12HourMode: Boolean): Pair<Int, Int>? {
        val digits = raw.filter { it.isDigit() }
        if (digits.isEmpty()) return null

        val (hour, minute) = when (digits.length) {
            1 -> {
                // Single digit: "8" → 8:00
                val h = digits.toIntOrNull() ?: return null
                h to 0
            }
            2 -> {
                // Two digits: could be "08" (hour) or "30" (might be minutes if context suggests)
                // Assume it's hour:00
                val h = digits.toIntOrNull() ?: return null
                h to 0
            }
            3 -> {
                // Three digits: "830" → 8:30
                val h = digits.take(1).toIntOrNull() ?: return null
                val m = digits.drop(1).toIntOrNull() ?: return null
                h to m
            }
            4 -> {
                // Four digits: "0830" → 08:30
                val h = digits.take(2).toIntOrNull() ?: return null
                val m = digits.drop(2).toIntOrNull() ?: return null
                h to m
            }
            else -> {
                // More than 4 digits: take first 4
                val truncated = digits.take(4)
                val h = truncated.take(2).toIntOrNull() ?: return null
                val m = truncated.drop(2).toIntOrNull() ?: return null
                h to m
            }
        }

        // Validate ranges
        if (minute !in 0..59) return null

        // Convert to 24-hour format if needed
        val hour24 = if (is12HourMode) {
            when {
                hour == 12 && isAm -> 0      // 12 AM = 00:00
                hour == 12 && !isAm -> 12   // 12 PM = 12:00
                !isAm -> hour + 12          // PM: add 12
                else -> hour                 // AM: keep as is
            }
        } else {
            hour
        }

        if (hour24 !in 0..23) return null

        // Round minutes to nearest 0 or 30
        val roundedMinute = if (minute < 15) 0 else if (minute < 45) 30 else 0
        val adjustedHour = if (minute >= 45) (hour24 + 1) % 24 else hour24

        return adjustedHour to roundedMinute
    }

    /**
     * Releases resources when done.
     */
    fun close() {
        recognizer.close()
    }
}
