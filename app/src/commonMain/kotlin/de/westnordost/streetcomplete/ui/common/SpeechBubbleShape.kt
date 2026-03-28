package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.LayoutDirection.Ltr
import androidx.compose.ui.unit.LayoutDirection.Rtl
import androidx.compose.ui.unit.dp
import kotlin.math.sqrt

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

        val path = Path().apply {
            addRoundRect(RoundRect(bubble, cornerPx, cornerPx))

            when (direction) {
                SpeechBubbleArrowAbsoluteDirection.Left -> {
                    val pos = (innerBubble.height - arrowWidthPx) * absoluteBias
                    moveTo(bubble.left, innerBubble.top + pos)
                    relativeLineTo(-arrowHeightPx, arrowWidthPx / 2f)
                    relativeLineTo(arrowHeightPx, arrowWidthPx / 2f)
                }
                SpeechBubbleArrowAbsoluteDirection.Top -> {
                    val pos = (innerBubble.width - arrowWidthPx) * absoluteBias
                    moveTo(innerBubble.left + pos, bubble.top)
                    relativeLineTo(arrowWidthPx / 2f, -arrowHeightPx)
                    relativeLineTo(arrowWidthPx / 2f, arrowHeightPx)
                }
                SpeechBubbleArrowAbsoluteDirection.Right -> {
                    val pos = (innerBubble.height - arrowWidthPx) * absoluteBias
                    moveTo(bubble.right, innerBubble.top + pos)
                    relativeLineTo(arrowHeightPx, arrowWidthPx / 2f)
                    relativeLineTo(-arrowHeightPx, arrowWidthPx / 2f)
                }
                SpeechBubbleArrowAbsoluteDirection.Bottom -> {
                    val pos = (innerBubble.width - arrowWidthPx) * absoluteBias
                    moveTo(innerBubble.left + pos, bubble.bottom)
                    relativeLineTo(arrowWidthPx / 2f, arrowHeightPx)
                    relativeLineTo(arrowWidthPx / 2f, -arrowHeightPx)
                }
            }
            close()
        }
        return Outline.Generic(path)
    }
}
