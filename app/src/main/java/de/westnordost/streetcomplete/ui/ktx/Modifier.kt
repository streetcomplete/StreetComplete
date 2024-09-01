package de.westnordost.streetcomplete.ui.ktx

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp

fun Modifier.conditional(condition: Boolean, modifier: Modifier.() -> Modifier): Modifier =
    if (condition) then(modifier(Modifier)) else this

/** Like border, but stroke is inside the element */
fun Modifier.innerBorder(
    width: Dp,
    color: Color,
    shape: Shape = RectangleShape
): Modifier =
    drawWithContent {
        drawContent()
        inset((width.toPx() / 2).coerceAtLeast(1f)) {
            drawOutline(
                outline = shape.createOutline(size, layoutDirection, Density(density, fontScale)),
                color = color,
                style = Stroke(width.toPx(),)
            )
        }
    }
