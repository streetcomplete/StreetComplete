package de.westnordost.streetcomplete.ui.util

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.unit.Density

fun Modifier.backgroundWithPadding(
    color: Color,
    padding: PaddingValues,
    shape: Shape = RectangleShape
) = drawBehind {
    val paddingLeft = padding.calculateLeftPadding(layoutDirection).toPx()
    val paddingRight = padding.calculateRightPadding(layoutDirection).toPx()
    val paddingTop = padding.calculateTopPadding().toPx()
    val paddingBottom = padding.calculateBottomPadding().toPx()
    val outline = shape.createOutline(
        size = Size(
            size.width - paddingLeft - paddingRight,
            size.height - paddingTop - paddingBottom
        ),
        layoutDirection = layoutDirection,
        density = Density(density)
    )
    val path = Path()
    path.addOutline(outline)
    path.translate(Offset(paddingLeft, paddingTop))

    drawPath(path, color = color)
}
