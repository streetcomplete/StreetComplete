package de.westnordost.streetcomplete.ui.ktx

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

fun Modifier.conditional(
    condition: Boolean,
    modifier: Modifier.() -> Modifier
): Modifier =
    if (condition) then(modifier(Modifier)) else this

/** set padding proportional to the composable's size */
fun Modifier.proportionalPadding(
    start: Float = 0f,
    top: Float = 0f,
    end: Float = 0f,
    bottom: Float = 0f
) = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val width = placeable.width
    val height = placeable.height
    val startPad = (start * width).toInt()
    val endPad = (end * width).toInt()
    val topPad = (top * height).toInt()
    val bottomPad = (bottom * height).toInt()
    layout(
        width = width + startPad + endPad,
        height = height + topPad + bottomPad,
    ) {
        placeable.placeRelative(x = startPad, y = topPad)
    }
}

/** set padding proportional to the composable's size */
fun Modifier.proportionalPadding(
    horizontal: Float = 0f,
    vertical: Float = 0f
) = proportionalPadding(
    start = horizontal,
    end = horizontal,
    top = vertical,
    bottom = vertical
)

/** set padding proportional to the composable's size */
fun Modifier.proportionalPadding(all: Float = 0f) =
    proportionalPadding(all, all, all, all)

/** set absolute offset proportional to the composable's size */
fun Modifier.proportionalAbsoluteOffset(
    x: Float = 0f,
    y: Float = 0f
) = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val width = placeable.width
    val height = placeable.height
    layout(width, height) {
        placeable.place(
            x = (x * width).toInt(),
            y = (y * height).toInt()
        )
    }
}

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

/** Draw a background that is inset by the given padding values */
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

/** Styles the element as selected */
fun Modifier.selectionFrame(
    isSelected: Boolean,
    color: Color = Color.Unspecified,
    shape: Shape? = null,
): Modifier = this.composed {
    val color = if (color == Color.Unspecified) MaterialTheme.colors.secondary else color
    val shape = shape ?: MaterialTheme.shapes.medium
    val selected by animateFloatAsState(if (isSelected) 1f else 0f)
    this
        .clip(shape)
        .background(color.copy(alpha = selected * 0.4f), shape)
        .border(2.dp, color.copy(alpha = selected * 0.8f), shape)
        .graphicsLayer(
            scaleX = 1f - selected * 0.075f,
            scaleY = 1f - selected * 0.075f,
            alpha = 1f - selected * 0.2f)
}

/** Adds fading edges to the element */
fun Modifier.fadingEdges(
    start: Dp = 0.dp,
    top: Dp = 0.dp,
    end: Dp = 0.dp,
    bottom: Dp = 0.dp
): Modifier = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        val left = if (layoutDirection == LayoutDirection.Ltr) start else end
        val right = if (layoutDirection == LayoutDirection.Ltr) end else start

        if (top != 0.dp) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black),
                    startY = 0f,
                    endY = top.toPx()
                ),
                topLeft = Offset.Zero,
                size = size.copy(height = top.toPx()),
                blendMode = BlendMode.DstIn
            )
        }

        if (bottom != 0.dp) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Black, Color.Transparent),
                    startY = size.height - bottom.toPx(),
                    endY = size.height
                ),
                topLeft = Offset(0f, size.height - bottom.toPx()),
                size = size.copy(height = bottom.toPx()),
                blendMode = BlendMode.DstIn
            )
        }

        if (left != 0.dp) {
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, Color.Black),
                    startX = 0f,
                    endX = left.toPx()
                ),
                topLeft = Offset.Zero,
                size = size.copy(width = left.toPx()),
                blendMode = BlendMode.DstIn
            )
        }

        if (right != 0.dp) {
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Black, Color.Transparent),
                    startX = size.width - right.toPx(),
                    endX = size.width
                ),
                topLeft = Offset(size.width - right.toPx(), 0f),
                size = size.copy(width = right.toPx()),
                blendMode = BlendMode.DstIn
            )
        }
    }
