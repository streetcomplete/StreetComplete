package de.westnordost.streetcomplete.quests.socket

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** Draw a hexagon with a character in the center, like the EU EV charging label. Black hexagon
 *  when it [hasCable], white hexagon otherwise. */
class ChargingStationSocketEuLabelPainter(
    val label: Char,
    val hasCable: Boolean,
    val textMeasurer: TextMeasurer,
) : Painter() {
    override val intrinsicSize = Size.Unspecified

    override fun DrawScope.onDraw() {
        val stroke = 1.dp.toPx()
        // stroke must be fully within the drawing area
        inset(stroke / 2f) {
            val minDimension = min(size.width, size.height)
            val radius = minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            val hexagonPath = Path().apply {
                for (i in 0 until 6) {
                    val angleRad = 2 * PI * i / 6
                    val x = center.x + radius * cos(angleRad).toFloat()
                    val y = center.y + radius * sin(angleRad).toFloat()

                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }

            drawPath(
                path = hexagonPath,
                color = if (hasCable) Color.Black else Color.White,
                style = Fill
            )
            drawPath(
                path = hexagonPath,
                color = if (hasCable) Color.White else Color.Black,
                style = Stroke(stroke)
            )

            val targetTextPx = minDimension * 0.6f
            val textLayoutResult = textMeasurer.measure(
                text = label.toString(),
                style = TextStyle.Default.copy(fontSize = targetTextPx.toSp())
            )
            val textSize = textLayoutResult.size
            val textOffset = center - Offset(textSize.width / 2f, textSize.height / 2f)

            drawText(
                textLayoutResult = textLayoutResult,
                color = if (hasCable) Color.White else Color.Black,
                topLeft = textOffset
            )
        }
    }
}
