package de.westnordost.streetcomplete.ui.user.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import de.westnordost.streetcomplete.ui.util.pxToDp
import de.westnordost.streetcomplete.util.ktx.systemTimeNow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import java.text.DateFormatSymbols
import kotlin.math.ceil
import kotlin.math.floor

/** Draws a github-style like days-active graphic */
@Composable
fun DatesActive(
    datesActive: Set<LocalDate>,
    datesActiveRange: Int,
    padding: Dp,
    boxCornerRadius: Dp,
    boxColor: Color,
    emptyBoxColor: Color,
) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val dayOffset = 7 - systemTimeNow().toLocalDateTime(TimeZone.UTC).dayOfWeek.value

        val verticalBoxes = 7 // days in a week
        val horizontalBoxes = ceil((dayOffset + datesActiveRange).toDouble() / verticalBoxes).toInt()

        val textMeasurer = rememberTextMeasurer()

        val textStyle = MaterialTheme.typography.caption
        val symbols = DateFormatSymbols.getInstance()
        val weekdays = Array(7) { symbols.shortWeekdays[1 + (it + 1) % 7] }
        val months = symbols.shortMonths

        val weekdayColumnWidth = weekdays.maxOf { textMeasurer.measure(it, textStyle).size.width }.pxToDp()
        val monthRowHeight = textMeasurer.measure(months[0]).size.height.pxToDp()

        // stretch 100% width and determine available box size and then the height from that
        val boxSize = (maxWidth - weekdayColumnWidth - padding * 2) / horizontalBoxes - padding
        val height = monthRowHeight + padding * 2 + (boxSize + padding) * verticalBoxes

        fun getLeft(x: Int) = weekdayColumnWidth + padding * 2 + (boxSize + padding) * x
        fun getTop(y: Int) = monthRowHeight + padding * 2 + (boxSize + padding) * y

        Canvas(modifier = Modifier.size(maxWidth, height)) {
            // weekdays
            for (i in 0 until 7) {
                val top = getTop(i).toPx()
                val left = 0f
                drawText(
                    textMeasurer,
                    text = weekdays[i],
                    topLeft = Offset(left, top),
                    style = textStyle
                )
            }
            // grid + months
            for (i in 0..datesActiveRange) {
                val time = systemTimeNow().minus(i, DateTimeUnit.DAY, TimeZone.UTC)
                val date = time.toLocalDateTime(TimeZone.UTC).date

                val y = (verticalBoxes - 1) - (i + dayOffset) % verticalBoxes
                val x = (horizontalBoxes - 1) - floor(((i + dayOffset) / verticalBoxes).toDouble()).toInt()

                val left = getLeft(x).toPx()
                val top = getTop(y).toPx()

                drawRoundRect(
                    color = if (date in datesActive) boxColor else emptyBoxColor,
                    topLeft = Offset(left, top),
                    size = Size(boxSize.toPx(), boxSize.toPx()),
                    cornerRadius = CornerRadius(boxCornerRadius.toPx(), boxCornerRadius.toPx())
                )

                if (date.dayOfMonth == 1) {
                    drawText(
                        textMeasurer,
                        text = months[date.month.value - 1],
                        topLeft = Offset(left, 0f),
                        style = textStyle
                    )
                }
            }
        }
    }
}
