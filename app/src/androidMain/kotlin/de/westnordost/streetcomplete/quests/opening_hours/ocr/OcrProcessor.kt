package de.westnordost.streetcomplete.quests.opening_hours.ocr

import android.graphics.Bitmap
import android.graphics.RectF
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.max
import kotlin.math.min

/**
 * Processes images using ML Kit OCR to extract time text from highlighted regions.
 * Parses various time formats commonly found on business hour signs.
 */
class OcrProcessor {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    // Regex patterns for time formats
    private val timePatterns = listOf(
        // 9:00, 09:00, 9:30
        Regex("""(\d{1,2}):(\d{2})"""),
        // 9.00, 09.00
        Regex("""(\d{1,2})\.(\d{2})"""),
        // 9AM, 9PM, 9 AM, 9 PM, 9am, 9pm
        Regex("""(\d{1,2})\s*([AaPp][Mm])"""),
        // 9:00AM, 9:00 PM
        Regex("""(\d{1,2}):(\d{2})\s*([AaPp][Mm])"""),
        // Just digits like 900, 0900, 1730
        Regex("""(\d{3,4})"""),
        // Single or double digit hour
        Regex("""(\d{1,2})""")
    )

    /**
     * Extracts time text from the specified region of the bitmap.
     *
     * @param bitmap The full image bitmap
     * @param region The bounding box region to scan (in image coordinates)
     * @return String containing the raw OCR text found in the region
     */
    suspend fun extractTextFromRegion(bitmap: Bitmap, region: RectF): String {
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
            suspendCancellableCoroutine { cont ->
                recognizer.process(inputImage)
                    .addOnSuccessListener { result ->
                        cont.resume(result.text)
                    }
                    .addOnFailureListener { e ->
                        cont.resume("") // Return empty on failure
                    }
            }
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Legacy method for backwards compatibility - extracts only digits.
     */
    suspend fun extractNumbersFromRegion(bitmap: Bitmap, region: RectF): String {
        val text = extractTextFromRegion(bitmap, region)
        return parseTimeFromText(text)
    }

    /**
     * Parses time from OCR text, handling various formats.
     * Returns a normalized string like "0900" or "1730" for easier parsing.
     */
    fun parseTimeFromText(text: String): String {
        if (text.isBlank()) return ""

        val normalizedText = text.trim().uppercase()

        // Try to match time patterns
        for (pattern in timePatterns) {
            val match = pattern.find(normalizedText)
            if (match != null) {
                return extractTimeFromMatch(match, normalizedText)
            }
        }

        // Fallback: just extract digits
        return text.filter { it.isDigit() }
    }

    private fun extractTimeFromMatch(match: MatchResult, fullText: String): String {
        val groups = match.groupValues

        return when {
            // Pattern: HH:MM AM/PM or H:MM AM/PM
            groups.size >= 4 && groups[3].isNotEmpty() -> {
                val hour = groups[1].toIntOrNull() ?: return groups[0].filter { it.isDigit() }
                val minute = groups[2].toIntOrNull() ?: 0
                val isPm = groups[3].uppercase().startsWith("P")
                val hour24 = convertTo24Hour(hour, isPm)
                String.format("%02d%02d", hour24, minute)
            }
            // Pattern: H AM/PM or HH AM/PM
            groups.size >= 3 && groups[2].matches(Regex("[AaPp][Mm]")) -> {
                val hour = groups[1].toIntOrNull() ?: return groups[0].filter { it.isDigit() }
                val isPm = groups[2].uppercase().startsWith("P")
                val hour24 = convertTo24Hour(hour, isPm)
                String.format("%02d00", hour24)
            }
            // Pattern: HH:MM or H:MM (check if AM/PM follows in text)
            groups.size >= 3 && groups[2].length == 2 -> {
                val hour = groups[1].toIntOrNull() ?: return groups[0].filter { it.isDigit() }
                val minute = groups[2].toIntOrNull() ?: 0

                // Check if AM/PM appears after the match
                val afterMatch = fullText.substring(match.range.last + 1).trim()
                val isPm = afterMatch.uppercase().startsWith("P")
                val isAm = afterMatch.uppercase().startsWith("A")

                val hour24 = when {
                    isPm -> convertTo24Hour(hour, true)
                    isAm -> convertTo24Hour(hour, false)
                    else -> hour // Assume 24-hour if no AM/PM
                }
                String.format("%02d%02d", hour24, minute)
            }
            // Pattern: 3-4 digit number (like 900, 0900, 1730)
            groups.size >= 2 && groups[1].length in 3..4 -> {
                val digits = groups[1]
                if (digits.length == 3) {
                    // 900 -> 09:00
                    "0${digits[0]}${digits.substring(1)}"
                } else {
                    digits
                }
            }
            // Pattern: 1-2 digit number (hour only)
            groups.size >= 2 && groups[1].length in 1..2 -> {
                val hour = groups[1].toIntOrNull() ?: return groups[1]

                // Check if AM/PM appears in full text
                val isPm = fullText.contains(Regex("[Pp][Mm]"))
                val isAm = fullText.contains(Regex("[Aa][Mm]"))

                val hour24 = when {
                    isPm && hour < 12 -> hour + 12
                    isAm && hour == 12 -> 0
                    else -> hour
                }
                String.format("%02d00", hour24)
            }
            else -> groups[0].filter { it.isDigit() }
        }
    }

    private fun convertTo24Hour(hour: Int, isPm: Boolean): Int {
        return when {
            hour == 12 && !isPm -> 0      // 12 AM = 00:00
            hour == 12 && isPm -> 12      // 12 PM = 12:00
            isPm -> hour + 12             // PM: add 12
            else -> hour                   // AM: keep as is
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

        return hour24 to minute
    }

    /**
     * Releases resources when done.
     */
    fun close() {
        recognizer.close()
    }
}
