package de.westnordost.streetcomplete.quests.lanes

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.painter.Painter
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.car1
import de.westnordost.streetcomplete.resources.car1a
import de.westnordost.streetcomplete.resources.car1b
import de.westnordost.streetcomplete.resources.car2
import de.westnordost.streetcomplete.resources.car2a
import de.westnordost.streetcomplete.resources.car2b
import de.westnordost.streetcomplete.resources.car3
import de.westnordost.streetcomplete.resources.car3a
import de.westnordost.streetcomplete.resources.car4
import de.westnordost.streetcomplete.resources.car5
import de.westnordost.streetcomplete.resources.street_side_unknown
import de.westnordost.streetcomplete.ui.common.StepperButton
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.max

/**
 *  Painter that displays the number of lanes on the left side and the number of lanes on
 *  the right side of the street, featuring
 *
 *  - many options to customize the rendering of the street (because streets have different markings
 *    in different countries)
 *  - displaying only one side (e.g. if it is a one-way street)
 *  - animated cars that drive down the street, customizable whether they drive on the left or
 *    right
 */
class LanesPainter(
    override val intrinsicSize: Size,
    private val questionMark: Painter,
    private val carsLeft: List<Car>,
    private val carsRight: List<Car>,
    private val laneCountLeft: Int,
    private val laneCountRight: Int,
    private val centerLineColor: Color = Color.White,
    private val edgeLineColor: Color = Color.White,
    private val edgeLineStyle: LineStyle = LineStyle.CONTINUOUS,
    private val isShowingLaneMarkings: Boolean = true,
    private val hasCenterLeftTurnLane: Boolean = false,
    private val isShowingBothSides: Boolean = true,
    private val isForwardTraffic: Boolean = true,
) : Painter() {
    private val isShowingOneLaneUnmarked: Boolean get() = !isShowingLaneMarkings && laneCountLeft == 0 && laneCountRight == 1

    private val isShowingOnlyRightSide: Boolean get() = !isShowingBothSides || isShowingOneLaneUnmarked

    private val noSidesAreDefined: Boolean get() = laneCountLeft == 0 && laneCountRight == 0
    private val bothSidesAreDefined: Boolean get() = laneCountLeft > 0 && laneCountRight > 0

    private val laneCountCenter: Int get() = if (hasCenterLeftTurnLane) 1 else 0

    private val leftLanesStart: Float get() = SHOULDER_WIDTH

    private val leftLanesEnd: Float get() = leftLanesStart + when {
        bothSidesAreDefined    -> laneCountLeft
        isShowingOnlyRightSide -> 0
        noSidesAreDefined      -> 1
        else                   -> max(laneCountLeft, laneCountRight)
    }

    private val lanesSpace: Int get() = laneCountCenter + when {
        bothSidesAreDefined    -> laneCountLeft + laneCountRight
        isShowingOnlyRightSide -> max(1, laneCountRight)
        noSidesAreDefined      -> 2
        else                   -> 2 * max(laneCountLeft, laneCountRight)
    }

    private val rightLanesStart: Float get() = leftLanesEnd + laneCountCenter

    private fun getLaneWidth(width: Float): Float = width / (lanesSpace + SHOULDER_WIDTH * 2)

    override fun DrawScope.onDraw() {

        drawRect(color = Color(0x33666666), size = size)

        val laneWidth = getLaneWidth(size.width)
        val lanesSpace = lanesSpace
        val leftLanesStart = leftLanesStart
        val leftLanesEnd = leftLanesEnd
        val rightLanesStart = rightLanesStart

        val zoom = if (isShowingBothSides && isShowingOneLaneUnmarked) 1.4f / laneWidth else 1f / laneWidth

        val edgeWidth = SHOULDER_WIDTH * laneWidth

        val lineWidth = LANE_MARKING_WIDTH / zoom

        val edgeLinePathEffect = edgeLineStyle.getPathEffect(lineWidth)

        val dashesPathEffect = LineStyle.DASHES.getPathEffect(lineWidth)

        // draw question marks if nothing is selected
        if (laneCountLeft <= 0 && !isShowingOnlyRightSide) {
            val qSize = leftLanesEnd * laneWidth
            drawVerticallyRepeating(
                painter = questionMark,
                left = 0f,
                painterSize = qSize,
                reverseY = isForwardTraffic
            )
        }
        if (laneCountRight <= 0) {
            val qLeft = rightLanesStart * laneWidth
            val qSize = size.width  - qLeft
            drawVerticallyRepeating(
                painter = questionMark,
                left = qLeft,
                painterSize = qSize,
                reverseY = !isForwardTraffic
            )
        }

        // draw background
        val backgroundLeft = if (laneCountLeft > 0 || isShowingOnlyRightSide) 0f else laneWidth * leftLanesEnd
        val backgroundRight = if (laneCountRight > 0) 0f else size.width - laneWidth * rightLanesStart
        drawRect(
            color = Color(0xff808080),
            topLeft = Offset(backgroundLeft, 0f),
            size = Size(size.width - backgroundRight - backgroundLeft, size.height)
        )

        // draw markings:

        // 1. markings for the shoulders
        if (laneCountLeft > 0 || isShowingOnlyRightSide) {
            drawVerticalLine(
                color = edgeLineColor,
                x = leftLanesStart * laneWidth,
                strokeWidth = lineWidth,
                pathEffect = edgeLinePathEffect,
            )
        }
        if (laneCountRight > 0) {
            drawVerticalLine(
                color = edgeLineColor,
                x = edgeWidth + lanesSpace * laneWidth,
                strokeWidth = lineWidth,
                pathEffect = edgeLinePathEffect,
            )
        }

        // 2. lane markings
        if (isShowingLaneMarkings) {
            val laneSeparatorLineColor = Color.White
            val lanesX =
                (1 until laneCountLeft).asSequence().map { edgeWidth + it * laneWidth } +
                (1 until laneCountRight).asSequence().map { (rightLanesStart + it) * laneWidth }

            for (x in lanesX) {
                drawVerticalLine(
                    color = laneSeparatorLineColor,
                    x = x,
                    strokeWidth = lineWidth,
                    pathEffect = dashesPathEffect,
                )
            }
        }

        // 3. center line
        if (bothSidesAreDefined && !hasCenterLeftTurnLane && isShowingLaneMarkings) {
            val onlyTwoLanes = laneCountLeft + laneCountRight <= 2
            drawVerticalLine(
                color = centerLineColor,
                x = leftLanesEnd * laneWidth,
                strokeWidth = lineWidth,
                pathEffect = if (onlyTwoLanes) dashesPathEffect else null
            )
        }

        // 4. center turn lane markings
        if (hasCenterLeftTurnLane) {
            drawVerticalLine(
                color = centerLineColor,
                x = leftLanesEnd * laneWidth,
                strokeWidth = lineWidth,
            )
            drawVerticalLine(
                color = centerLineColor,
                x = rightLanesStart * laneWidth,
                strokeWidth = lineWidth,
            )
            drawVerticalLine(
                color = centerLineColor,
                x = (leftLanesEnd + 0.125f) * laneWidth,
                strokeWidth = lineWidth,
                pathEffect = dashesPathEffect,
            )
            drawVerticalLine(
                color = centerLineColor,
                x = (rightLanesStart - 0.125f) * laneWidth,
                strokeWidth = lineWidth,
                pathEffect = dashesPathEffect,
            )
        }

        // 5. draw cars
        val carWidth = (1f - 2f * CAR_LANE_PADDING) / zoom
        carsRight.asReversed().forEachIndexed { index, car ->
            drawCar(
                carPainter = car.painter,
                progress = car.progress,
                isDirectionForward = car.speed > 0,
                laneIndex = rightLanesStart + index,
                laneWidth = laneWidth,
                carWidth = carWidth,
            )
        }
        carsLeft.forEachIndexed { index, car ->
            drawCar(
                carPainter = car.painter,
                progress = car.progress,
                isDirectionForward = car.speed > 0,
                laneIndex = leftLanesStart + index,
                laneWidth = laneWidth,
                carWidth = carWidth,
            )
        }
    }
}

private fun DrawScope.drawVerticalLine(
    color: Color,
    x: Float,
    strokeWidth: Float,
    pathEffect: PathEffect? = null,
) {
    drawLine(
        color = color,
        start = Offset(x, 0f),
        end = Offset(x, size.height),
        strokeWidth = strokeWidth,
        pathEffect = pathEffect,
    )
}

private fun DrawScope.drawCar(
    carPainter: Painter,
    progress: Float,
    isDirectionForward: Boolean,
    laneIndex: Float,
    laneWidth: Float,
    carWidth: Float
) {
    val w = carPainter.intrinsicSize.width
    val h = carPainter.intrinsicSize.height

    val carHeight = carWidth * h / w

    val x = laneWidth * laneIndex + (laneWidth - carWidth) / 2f
    val y = (carHeight + size.height) * (1 - progress) - carHeight

    withTransform(
        transformBlock = {
            if (!isDirectionForward) rotate(180f, Offset(w / 2f, h / 2f))
            translate(x, y)
        }
    ) {
        with(carPainter) {
            draw(Size(carWidth, carHeight))
        }
    }
}

private fun DrawScope.drawVerticallyRepeating(
    painter: Painter,
    left: Float,
    painterSize: Float,
    reverseY: Boolean
) {
    var i = 0
    rotate(
        degrees = if (reverseY) 180f else 0f,
        pivot = Offset(left + painterSize / 2f, center.y)
    ) {
        while (painterSize * i < size.height) {
            val top = painterSize * i
            val bottom = painterSize * (i + 1)
            val right = left + painterSize
            translate(left, top) {
                with(painter) { draw(Size(right - left, bottom - top)) }
            }
            i++
        }
    }
}

private const val SHOULDER_WIDTH = 0.125f // as fraction of lane width
private const val LANE_MARKING_WIDTH = 0.0625f // as fraction of lane width
private const val CAR_LANE_PADDING = 0.10f // how much space there is between car and lane markings, as fraction of lane width
private const val CAR_SPEED = 4f // in "lane graphic squares per second"
private const val CAR_SPEED_VARIATION = 0.2f // as fraction: 1 = 100% variation
private const val CAR_SPARSITY = 1f

private val CAR_DRAWABLES = listOf(
    Res.drawable.car1,
    Res.drawable.car1a,
    Res.drawable.car1b,
    Res.drawable.car2,
    Res.drawable.car2a,
    Res.drawable.car2b,
    Res.drawable.car3,
    Res.drawable.car3a,
    Res.drawable.car4,
    Res.drawable.car5,
)

enum class LineStyle { CONTINUOUS, DASHES, SHORT_DASHES }

data class Car(
    val painter: Painter,
    val progress: Float,
    val speed: Float,
)

private fun LineStyle.getPathEffect(lineWidth: Float): PathEffect? = when (this) {
    LineStyle.CONTINUOUS -> null
    LineStyle.DASHES -> PathEffect.dashPathEffect(floatArrayOf(lineWidth * 6, lineWidth * 10))
    LineStyle.SHORT_DASHES -> PathEffect.dashPathEffect(floatArrayOf(lineWidth * 4, lineWidth * 4))
}

@Preview @Composable
private fun LanesPainterPreview() {
    var lanesLeft by remember { mutableIntStateOf(1) }
    var lanesRight by remember { mutableIntStateOf(1) }
    var isForwardTraffic by remember { mutableStateOf(true) }
    var isShowingBothSides by remember { mutableStateOf(true) }
    var hasCenterLeftTurnLane by remember { mutableStateOf(false) }
    val questionMark = painterResource(Res.drawable.street_side_unknown)
    val painter = remember(lanesLeft, lanesRight, isForwardTraffic, isShowingBothSides, hasCenterLeftTurnLane) {
        LanesPainter(
            intrinsicSize = Size(500f, 500f),
            questionMark = questionMark,
            carsLeft = listOf(),
            carsRight = listOf(),
            laneCountLeft = lanesLeft,
            laneCountRight = lanesRight,
            isForwardTraffic = isForwardTraffic,
            isShowingBothSides = isShowingBothSides,
            hasCenterLeftTurnLane = hasCenterLeftTurnLane,
        )
    }
    Column {
        Image(painter, null)
        Row {
            StepperButton(onIncrease = { lanesLeft++ }, onDecrease = { lanesLeft-- })
            StepperButton(onIncrease = { lanesRight++ }, onDecrease = { lanesRight-- })
            Checkbox(isForwardTraffic, onCheckedChange = { isForwardTraffic = it })
            Checkbox(isShowingBothSides, onCheckedChange = { isShowingBothSides = it })
            Checkbox(hasCenterLeftTurnLane, onCheckedChange = { hasCenterLeftTurnLane = it })
        }
    }
}
