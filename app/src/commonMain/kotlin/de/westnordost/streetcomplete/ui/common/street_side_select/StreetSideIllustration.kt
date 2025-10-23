package de.westnordost.streetcomplete.ui.common.street_side_select

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import de.westnordost.streetcomplete.ui.ktx.conditional
import de.westnordost.streetcomplete.ui.util.FallDownTransitionSpec
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos

/** Custom composable that conceptually shows the left and right side of a street. Both sides
 *  are clickable.
 *
 *  It is possible to set an image for the left and for the right side individually, the image set
 *  is repeated vertically (repeated along the street). Setting a (non-rotated) content for each
 *  side is also possible
 *  The whole displayed street can be rotated and it is possible to only show the right side, for
 *  example for one-way streets. */
@Composable fun StreetSideIllustration(
    leftPainter: Painter?,
    rightPainter: Painter?,
    rotation: Float,
    modifier: Modifier = Modifier,
    onClickLeft: (() -> Unit)? = null,
    onClickRight: (() -> Unit)? = null,
    itemContentLeft:  (@Composable () -> Unit)? = null,
    itemContentRight: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    isLeftSideVisible: Boolean = true,
    isRightSideVisible: Boolean = true,
) {
    val scale = 1f + abs(cos(rotation * PI / 180)).toFloat() * 0.67f
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
    ) {
        Row(Modifier
            .align(Alignment.Center)
            .requiredWidth(min(maxWidth, maxHeight))
            .requiredHeight(max(maxWidth, maxHeight))
            .rotate(rotation)
            .scale(scale)
        ) {
            if (isLeftSideVisible) {
                StreetSideIllustrationSide(
                    painter = leftPainter,
                    rotation = rotation,
                    onClick = onClickLeft,
                    enabled = enabled,
                    content = itemContentLeft,
                    isRightSide = false,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
            if (isRightSideVisible) {
                StreetSideIllustrationSide(
                    painter = rightPainter,
                    rotation = rotation,
                    onClick = onClickRight,
                    enabled = enabled,
                    content = itemContentRight,
                    isRightSide = true,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun StreetSideIllustrationSide(
    painter: Painter?,
    rotation: Float,
    onClick: (() -> Unit)?,
    enabled: Boolean,
    content: (@Composable () -> Unit)?,
    isRightSide: Boolean,
    modifier: Modifier = Modifier,
) {
    val scale = 1f + abs(cos(rotation * PI / 180)).toFloat() * 0.67f
    AnimatedContent(
        targetState = painter,
        transitionSpec = FallDownTransitionSpec,
        modifier = modifier
    ) { rightPainter ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(Color(0x33666666))
                    if (painter != null) {
                        val flip = if (isRightSide) 0f else 180f
                        rotate(flip) {
                            drawVerticallyRepeatingImage(painter)
                        }
                    }
                }
                .conditional(onClick) { clickable(onClick = it, enabled = enabled) },
            contentAlignment = Alignment.Center,
        ) {
            if (content != null) {
                Box(Modifier.rotate(-rotation).scale(1f / scale)) {
                    content()
                }
            }
        }
    }
}

private fun DrawScope.drawVerticallyRepeatingImage(painter: Painter) {
    val w = size.width
    val h = painter.intrinsicSize.height / painter.intrinsicSize.width * size.width
    val repetitions = ceil(size.height / h).toInt()
    for (i in 0 until repetitions) {
        // -1f so that they rather overlap than not on rounding imprecision
        translate(top = i * ceil(h - 1f)) {
            with(painter) { draw(Size(w, h)) }
        }
    }
}
