package de.westnordost.streetcomplete.screens.user.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.ktx.pxToDp
import de.westnordost.streetcomplete.ui.theme.GrassGreen
import de.westnordost.streetcomplete.ui.theme.surfaceContainer
import de.westnordost.streetcomplete.util.ktx.systemTimeNow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import java.text.DateFormatSymbols
import kotlin.math.ceil

/** Draws a github-style like days-active graphic */
@Composable
fun DatesActiveTable(
    datesActive: Set<LocalDate>,
    datesActiveRange: Int,
    modifier: Modifier = Modifier,
    boxColor: Color = GrassGreen,
    emptyBoxColor: Color = MaterialTheme.colors.surfaceContainer,
    textColor: Color = contentColorFor(MaterialTheme.colors.surface),
    cellPadding: Dp = 2.dp,
    cellCornerRadius: Dp = 6.dp,
) {
    BoxWithConstraints(modifier) {
        // no data, no table
        if (datesActiveRange <= 0) return@BoxWithConstraints

        val dayOffset = 7 - systemTimeNow().toLocalDateTime(TimeZone.UTC).dayOfWeek.value

        val verticalCells = 7 // days in a week
        val horizontalCells = ceil((dayOffset + datesActiveRange).toDouble() / verticalCells).toInt()

        val textMeasurer = rememberTextMeasurer(12)

        val textStyle = MaterialTheme.typography.body2
        val symbols = DateFormatSymbols.getInstance()
        val weekdays = Array(7) { symbols.shortWeekdays[1 + (it + 1) % 7] }
        val months = symbols.shortMonths

        val weekdayColumnWidth = weekdays.maxOf { textMeasurer.measure(it, textStyle).size.width }.pxToDp()
        val textHeight = textMeasurer.measure(months[0]).size.height.pxToDp()

        // stretch 100% width and determine available box size and then the height from that
        val cellSize = (maxWidth - weekdayColumnWidth - cellPadding * 2) / horizontalCells - cellPadding
        val height = textHeight + cellPadding * 2 + (cellSize + cellPadding) * verticalCells

        val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
        val marginLeft = if (isLtr) weekdayColumnWidth else 0.dp

        fun getLeft(x: Int) = marginLeft + cellPadding * 2 + (cellSize + cellPadding) * x
        fun getTop(y: Int) = textHeight + cellPadding * 2 + (cellSize + cellPadding) * y

        Canvas(Modifier.size(maxWidth, height)) {
            // weekdays
            for (i in 0 until 7) {
                val top = getTop(i)
                val bottom = (getTop(i + 1) - cellPadding)
                val centerTop = top + (bottom - top - textHeight) / 2 // center text vertically
                val left = if (isLtr) 0.dp else getLeft(horizontalCells)

                drawText(
                    textMeasurer.measure(
                        text = weekdays[i],
                        style = textStyle,
                        constraints = Constraints.fixedWidth(weekdayColumnWidth.toPx().toInt())
                    ),
                    color = textColor,
                    topLeft = Offset(left.toPx(), centerTop.toPx())
                )
            }
            if (horizontalCells < 1) return@Canvas
            // grid + months
            for (i in 0..<datesActiveRange) {
                val time = systemTimeNow().minus(i, DateTimeUnit.DAY, TimeZone.UTC)
                val date = time.toLocalDateTime(TimeZone.UTC).date

                val y = (verticalCells - 1) - (i + dayOffset) % verticalCells
                val xLtr = (horizontalCells - 1) - (i + dayOffset) / verticalCells
                val x = if (isLtr) xLtr else (horizontalCells - 1) - xLtr

                val left = getLeft(x).toPx()
                val top = getTop(y).toPx()

                drawRoundRect(
                    color = if (date in datesActive) boxColor else emptyBoxColor,
                    topLeft = Offset(left, top),
                    size = Size(cellSize.toPx(), cellSize.toPx()),
                    cornerRadius = CornerRadius(cellCornerRadius.toPx(), cellCornerRadius.toPx())
                )

                if (date.dayOfMonth == 1) {
                    drawText(
                        textMeasurer.measure(
                            text = months[date.month.value - 1],
                            style = textStyle
                        ),
                        color = textColor,
                        topLeft = Offset(left, 0f),
                    )
                }
            }
        }
    }
}

@Preview
@Preview(locale = "ar", fontScale = 1.8f) // right-to-left and large text
@Preview(device = Devices.NEXUS_7) // large screen
@Composable
private fun DatesActivePreview() {
    DatesActiveTable(
        datesActive = IntArray(30) { (0..90).random() }.map {
            systemTimeNow().minus(it, DateTimeUnit.DAY, TimeZone.UTC).toLocalDateTime(TimeZone.UTC).date
        }.toSet(),
        datesActiveRange = 90
    )
}
