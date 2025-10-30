package de.westnordost.streetcomplete.ui.common.street_side_select

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
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
import de.westnordost.streetcomplete.osm.Sides
import de.westnordost.streetcomplete.osm.get
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
@Composable fun <T> StreetSideIllustration(
    value: Sides<T>,
    getIllustrationPainter: (@Composable (T?, Side) -> Painter?),
    rotation: Float,
    modifier: Modifier = Modifier,
    getFloatingPainter: @Composable (T?, Side) -> Painter? = { _, _ -> null },
    onClickSide: ((Side) -> Unit)? = null,
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
                    value = value[Side.LEFT],
                    side = Side.LEFT,
                    getIllustrationPainter = getIllustrationPainter,
                    getFloatingPainter = getFloatingPainter,
                    rotation = rotation,
                    onClickSide = onClickSide,
                    enabled = enabled,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
            if (isRightSideVisible) {
                StreetSideIllustrationSide(
                    value = value[Side.RIGHT],
                    side = Side.RIGHT,
                    getIllustrationPainter = getIllustrationPainter,
                    getFloatingPainter = getFloatingPainter,
                    rotation = rotation,
                    onClickSide = onClickSide,
                    enabled = enabled,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun <T> StreetSideIllustrationSide(
    value: T?,
    side: Side,
    getIllustrationPainter: (@Composable (T?, Side) -> Painter?),
    getFloatingPainter: @Composable (T?, Side) -> Painter?,
    rotation: Float,
    onClickSide: ((Side) -> Unit)?,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val scale = 1f + abs(cos(rotation * PI / 180)).toFloat() * 0.67f
    AnimatedContent(
        targetState = value,
        transitionSpec = FallDownTransitionSpec,
        modifier = modifier
    ) { value ->
        val painter = getIllustrationPainter(value, side)
        val floatingPainter = getFloatingPainter(value, side)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(Color(0x33666666))
                    if (painter != null) {
                        val flip = if (side == Side.RIGHT) 0f else 180f
                        rotate(flip) {
                            drawVerticallyRepeatingImage(painter)
                        }
                    }
                }
                .conditional(onClickSide) { clickable(onClick = { it(side) }, enabled = enabled) },
            contentAlignment = Alignment.Center,
        ) {
            if (floatingPainter != null) {
                Box(Modifier.rotate(-rotation).scale(1f / scale)) {
                    Image(floatingPainter, null)
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

enum class Side { LEFT, RIGHT }
