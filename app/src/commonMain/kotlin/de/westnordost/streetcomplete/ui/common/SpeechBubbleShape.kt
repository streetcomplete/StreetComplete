package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.LayoutDirection.Ltr
import androidx.compose.ui.unit.LayoutDirection.Rtl
import androidx.compose.ui.unit.dp

enum class SpeechBubbleArrowDirection { Start, Top, End, Bottom }

private enum class SpeechBubbleArrowAbsoluteDirection { Left, Top, Right, Bottom }

/** A shape in the shape of a speech bubble, with rounded corners of radius [cornerRadius] and an
 *  arrow of size [arrowSize] placed on the outline into the given [arrowDirection].
 *  [arrowPlacementBias] controls where on that side the arrow is placed: 0 is start/top, 1 is
 *  end/bottom. */
@Immutable
data class SpeechBubbleShape(
    private val cornerRadius: Dp = 16.dp,
    private val arrowSize: Dp = 12.dp,
    private val arrowDirection: SpeechBubbleArrowDirection = SpeechBubbleArrowDirection.Bottom,
    private val arrowPlacementBias: Float = 0f,
) : Shape {

    /** Content padding for content to appear inside the speech bubble */
    val contentPadding: PaddingValues by lazy {
        PaddingValues(
            start =  if (arrowDirection == SpeechBubbleArrowDirection.Start) arrowSize else 0.dp,
            top = if (arrowDirection == SpeechBubbleArrowDirection.Top) arrowSize else 0.dp,
            end = if (arrowDirection == SpeechBubbleArrowDirection.End) arrowSize else 0.dp,
            bottom = if (arrowDirection == SpeechBubbleArrowDirection.Bottom) arrowSize else 0.dp
        )
    }

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val cornerPx = with(density) { cornerRadius.toPx() }.coerceIn(0f, size.minDimension / 2f)
        val maxArrowWidthPx = when (arrowDirection) {
            SpeechBubbleArrowDirection.Start, SpeechBubbleArrowDirection.End -> size.height
            SpeechBubbleArrowDirection.Top, SpeechBubbleArrowDirection.Bottom -> size.width
        } - cornerPx * 2f
        val arrowHeightPx = with(density) { arrowSize.toPx() }.coerceIn(0f, maxArrowWidthPx / 2f)
        val arrowWidthPx = arrowHeightPx * 2f

        val direction = when (arrowDirection) {
            SpeechBubbleArrowDirection.Start -> when (layoutDirection) {
                Ltr -> SpeechBubbleArrowAbsoluteDirection.Left
                Rtl -> SpeechBubbleArrowAbsoluteDirection.Right
            }
            SpeechBubbleArrowDirection.End -> when (layoutDirection) {
                Ltr -> SpeechBubbleArrowAbsoluteDirection.Right
                Rtl -> SpeechBubbleArrowAbsoluteDirection.Left
            }
            SpeechBubbleArrowDirection.Top -> SpeechBubbleArrowAbsoluteDirection.Top
            SpeechBubbleArrowDirection.Bottom -> SpeechBubbleArrowAbsoluteDirection.Bottom
        }

        val bubble = when (direction) {
            SpeechBubbleArrowAbsoluteDirection.Left -> Rect(arrowHeightPx, 0f, size.width, size.height)
            SpeechBubbleArrowAbsoluteDirection.Top -> Rect(0f, arrowHeightPx, size.width, size.height)
            SpeechBubbleArrowAbsoluteDirection.Right -> Rect(0f, 0f, size.width - arrowHeightPx, size.height)
            SpeechBubbleArrowAbsoluteDirection.Bottom -> Rect(0f, 0f, size.width, size.height - arrowHeightPx)
        }

        val innerBubble = Rect(
            bubble.left + cornerPx,
            bubble.top + cornerPx,
            bubble.right - cornerPx,
            bubble.bottom - cornerPx
        )

        val absoluteBias = when (layoutDirection) {
            Ltr -> arrowPlacementBias
            Rtl -> when (arrowDirection) {
                SpeechBubbleArrowDirection.Top, SpeechBubbleArrowDirection.Bottom -> 1f - arrowPlacementBias
                SpeechBubbleArrowDirection.Start, SpeechBubbleArrowDirection.End -> arrowPlacementBias
            }
        }

        val cornerDiameter = cornerPx * 2f
        val cornerCircle = Size(cornerDiameter, cornerDiameter)

        val path = Path().apply {
            moveTo(innerBubble.left, bubble.top)
            arcTo(Rect(bubble.topLeft, cornerCircle), 270f, -90f, false)
            if (direction == SpeechBubbleArrowAbsoluteDirection.Left) {
                val pos = (innerBubble.height - arrowWidthPx) * absoluteBias
                lineTo(bubble.left, innerBubble.top + pos)
                relativeLineTo(-arrowHeightPx, arrowWidthPx / 2f)
                relativeLineTo(arrowHeightPx, arrowWidthPx / 2f)
            }
            arcTo(Rect(bubble.bottomLeft - Offset(0f, cornerDiameter), cornerCircle), 180f, -90f, false)
            if (direction == SpeechBubbleArrowAbsoluteDirection.Bottom) {
                val pos = (innerBubble.width - arrowWidthPx) * absoluteBias
                lineTo(innerBubble.left + pos, bubble.bottom)
                relativeLineTo(arrowWidthPx / 2f, arrowHeightPx)
                relativeLineTo(arrowWidthPx / 2f, -arrowHeightPx)
            }
            arcTo(Rect(bubble.bottomRight - Offset(cornerDiameter, cornerDiameter), cornerCircle), 90f, -90f, false)
            if (direction == SpeechBubbleArrowAbsoluteDirection.Right) {
                val pos = (innerBubble.height - arrowWidthPx) * absoluteBias + arrowWidthPx
                lineTo(bubble.right, innerBubble.top + pos)
                relativeLineTo(arrowHeightPx, -arrowWidthPx / 2f)
                relativeLineTo(-arrowHeightPx, -arrowWidthPx / 2f)
            }
            arcTo(Rect(bubble.topRight - Offset(cornerDiameter, 0f), cornerCircle), 0f, -90f, false)
            if (direction == SpeechBubbleArrowAbsoluteDirection.Top) {
                val pos = (innerBubble.width - arrowWidthPx) * absoluteBias + arrowWidthPx
                lineTo(innerBubble.left + pos, bubble.top)
                relativeLineTo(-arrowWidthPx / 2f, -arrowHeightPx)
                relativeLineTo(-arrowWidthPx / 2f, arrowHeightPx)
            }
            close()
        }
        return Outline.Generic(path)
    }
}

@Preview
@Composable
private fun SpeechBubbleShapePreview() {
    var cornerRadius by remember { mutableFloatStateOf(32f) }
    var arrowSize by remember { mutableFloatStateOf(16f) }
    var arrowDirection by remember { mutableStateOf(SpeechBubbleArrowDirection.Start) }
    var arrowPlacementBias by remember { mutableFloatStateOf(0.333f) }
    val shape = SpeechBubbleShape(cornerRadius.dp, arrowSize.dp, arrowDirection, arrowPlacementBias)
    Column {
        Slider(cornerRadius, onValueChange = { cornerRadius = it }, valueRange = 0f..50f)
        Slider(arrowSize, onValueChange = { arrowSize = it }, valueRange = 0f..50f)
        Slider(arrowPlacementBias, onValueChange = { arrowPlacementBias = it }, valueRange = 0f..1f)
        Row {
            SpeechBubbleArrowDirection.entries.forEach {
                Button(onClick = { arrowDirection = it }) { Text(it.name) }
            }
        }
        Box(Modifier
            .size(200.dp)
            .background(color = Color.White, shape = shape)
            .border(2.dp, color = Color.Black, shape = shape)
        )
    }
}
