package de.westnordost.streetcomplete.screens.user.profile

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import de.westnordost.streetcomplete.util.ktx.systemTimeNow
import de.westnordost.streetcomplete.util.ktx.toLocalDate
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import java.text.DateFormatSymbols
import kotlin.math.ceil
import kotlin.math.floor

/** Draws a github-style like days-active graphic */
class DatesActiveDrawable(
    private val datesActive: Set<LocalDate>,
    private val datesActiveRange: Int,
    private val boxSize: Float,
    private val padding: Float,
    private val roundRectRadius: Float,
    textColor: Int
) : Drawable() {

    private val dayOffset = 7 - systemTimeNow().toLocalDateTime(TimeZone.UTC).dayOfWeek.value
    private val weekdaysWidth: Float
    private val textHeight: Float = boxSize * 0.8f

    private val greenBoxPaint = Paint().apply { setARGB(255, 128, 177, 88) }
    private val emptyBoxPaint = Paint().apply { setARGB(20, 128, 128, 128) }
    private val textPaint = Paint().apply {
        color = textColor
        textSize = textHeight
    }

    private val weekdays: List<String>
    private val months: List<String>

    init {
        val symbols = DateFormatSymbols.getInstance()
        weekdays = Array(7) { symbols.shortWeekdays[1 + (it + 1) % 7] }.toList()
        months = symbols.shortMonths.toList()
        weekdaysWidth = weekdays.maxOf { textPaint.measureText(it) }
    }

    override fun draw(canvas: Canvas) {
        var time = systemTimeNow()

        val width = ceil((dayOffset + datesActiveRange) / 7.0).toInt()
        val height = 7

        // weekdays
        for (i in 0 until 7) {
            val top = textHeight + textHeight + i * (boxSize + padding)
            canvas.drawText(weekdays[i], 0f, top, textPaint)
        }

        // grid + months
        for (i in 0 .. datesActiveRange) {
            val date = time.toLocalDate()

            val y = (height - 1) - (i + dayOffset) % height
            val x = (width - 1) - floor(((i + dayOffset) / height).toDouble()).toInt()

            val left = getLeft(x)
            val top = getTop(y)

            canvas.drawRoundRect(
                left,
                top,
                left + boxSize,
                top + boxSize,
                roundRectRadius,
                roundRectRadius,
                if (date in datesActive) greenBoxPaint else emptyBoxPaint
            )

            if (date.dayOfMonth == 1) {
                // center text within month
                val right = getLeft(x + 4)
                val text = months[date.month.value - 1]
                val textWidth = textPaint.measureText(text)
                val start = left + (right - left - textWidth) / 2f
                canvas.drawText(months[date.month.value - 1], start, textHeight, textPaint)
            }

            time = time.minus(1, DateTimeUnit.DAY, TimeZone.UTC)
        }
    }

    private fun getLeft(x: Int): Float =
        weekdaysWidth + padding * 2 + x * (boxSize + padding)

    private fun getTop(y: Int): Float =
        textHeight + padding * 2 + y * (boxSize + padding)

    override fun getIntrinsicWidth(): Int {
        val gridWidth = ceil((dayOffset + datesActiveRange) / 7.0).toInt()
        return getLeft(gridWidth).toInt()
    }

    override fun getIntrinsicHeight(): Int {
        val gridHeight = 7
        return getTop(gridHeight).toInt()
    }

    override fun setAlpha(alpha: Int) {
        // not supported
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        // not supported
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
