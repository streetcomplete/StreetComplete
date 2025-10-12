package de.westnordost.streetcomplete.quests.lanes

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
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
import de.westnordost.streetcomplete.resources.car_nyan
import de.westnordost.streetcomplete.resources.street_side_unknown
import de.westnordost.streetcomplete.ui.util.FallDownTransitionSpec
import de.westnordost.streetcomplete.util.ktx.isApril1st
import kotlinx.coroutines.isActive
import org.jetbrains.compose.resources.painterResource
import kotlin.random.Random

/**
 *  Composable that displays the number of lanes on the left side and the number of lanes on
 *  the right side of the street, featuring
 *
 *  - many options to customize the rendering of the street (because streets have different markings
 *    in different countries)
 *  - displaying only one side (e.g. if it is a one-way street)
 *  - animated cars that drive down the street, customizable whether they drive on the left or
 *    right
 */
@Composable fun LanesSelect(
    value: Lanes,
    onClickForwardSide: () -> Unit,
    onClickBackwardSide: () -> Unit,
    modifier: Modifier = Modifier,
    centerLineColor: Color = Color.White,
    edgeLineColor: Color = Color.White,
    edgeLineStyle: LineStyle = LineStyle.CONTINUOUS,
    laneSeparatorLineColor: Color = Color.White,
    laneSeparatorLineStyle: LineStyle = LineStyle.DASHES,
    isShowingLaneMarkings: Boolean = true,
    isLeftHandTraffic: Boolean = false,
    isOneway: Boolean = false,
    isReversedOneway: Boolean = false,
) {
    AnimatedContent(
        targetState = value,
        modifier = modifier,
        transitionSpec = FallDownTransitionSpec,
        contentAlignment = Alignment.Center,
    ) {
        val questionMark = painterResource(Res.drawable.street_side_unknown)

        // don't show the other side for one-ways
        val laneCountForward = if (!isReversedOneway) value.forward else 0
        val laneCountBackward = if (!isOneway || isReversedOneway) value.backward else 0
        val hasCenterLeftTurnLane = value.centerLeftTurnLane

        val laneCountLeft = if (isLeftHandTraffic) laneCountForward else laneCountBackward
        val laneCountRight = if (isLeftHandTraffic) laneCountBackward else laneCountForward

        val laneCountCenter = if (hasCenterLeftTurnLane) 1 else 0
        // when one side is not defined, it takes the same width as the other side
        val displayLaneCountLeft = laneCountLeft
            ?: (if (laneCountRight != null && laneCountRight > 0) laneCountRight else 1)
        val displayLaneCountRight = laneCountRight
            ?: (if (laneCountLeft != null && laneCountLeft > 0) laneCountLeft else 1)

        val lanesSpace = laneCountCenter + displayLaneCountLeft + displayLaneCountRight

        val leftLanesStart = SHOULDER_WIDTH
        val leftLanesEnd = leftLanesStart + displayLaneCountLeft
        val rightLanesStart = leftLanesEnd + laneCountCenter
        val rightLanesEnd = rightLanesStart + displayLaneCountRight

        val totalLaneWidth = lanesSpace + SHOULDER_WIDTH * 2

        val carPainters =
            if (isApril1st()) listOf(painterResource(Res.drawable.car_nyan))
            else CAR_DRAWABLES.map { painterResource(it) }

        var carsLeft by remember(laneCountLeft) { mutableStateOf(
            List(laneCountLeft ?: 0) { createCar(carPainters, Random.nextFloat()) }
        ) }
        var carsRight by remember(laneCountRight) { mutableStateOf(
            List(laneCountRight ?: 0) { createCar(carPainters, Random.nextFloat()) }
        ) }

        LaunchedEffect(laneCountLeft, laneCountRight) {
            var lastFrameTime = withFrameMillis { it }
            while (isActive) {
                withFrameMillis { newFrameTime ->
                    val elapsedMillis = (newFrameTime - lastFrameTime)
                    lastFrameTime = newFrameTime
                    val delta = elapsedMillis / 7500f

                    carsLeft = carsLeft.map { car ->
                        if (car.progress <= 1) car.copy(progress = car.progress + car.speed * delta)
                        else createCar(carPainters, -CAR_SPARSITY * Random.nextFloat())
                    }
                    carsRight = carsRight.map { car ->
                        if (car.progress <= 1) car.copy(progress = car.progress + car.speed * delta)
                        else createCar(carPainters, -CAR_SPARSITY * Random.nextFloat())
                    }
                }
            }
        }

        Box {
            Canvas(Modifier.fillMaxSize()) {
                drawRect(color = Color(0x33666666), size = size)

                val laneWidth = size.width / totalLaneWidth

                val zoom = if (lanesSpace == 1) 1.4f / laneWidth else 1f / laneWidth

                val lineWidth = LANE_MARKING_WIDTH / zoom

                val edgeLinePathEffect = edgeLineStyle.getPathEffect(lineWidth)
                val laneSeparatorPathEffect = laneSeparatorLineStyle.getPathEffect(lineWidth)

                // draw question marks if nothing is selected
                if (laneCountLeft == null) {
                    drawVerticallyRepeating(
                        painter = questionMark,
                        left = 0f,
                        painterSize = leftLanesEnd * laneWidth,
                        reverseY = !isLeftHandTraffic
                    )
                }
                if (laneCountRight == null) {
                    val left = rightLanesStart * laneWidth
                    drawVerticallyRepeating(
                        painter = questionMark,
                        left = left,
                        painterSize = size.width - left,
                        reverseY = isLeftHandTraffic
                    )
                }

                // draw background
                val backgroundLeft =
                    if (laneCountLeft != null) 0f
                    else laneWidth * leftLanesEnd
                val backgroundRight =
                    if (laneCountRight != null) 0f
                    else size.width - laneWidth * rightLanesStart
                drawRect(
                    color = Color(0xff808080),
                    topLeft = Offset(backgroundLeft, 0f),
                    size = Size(size.width - backgroundRight - backgroundLeft, size.height)
                )

                // draw markings:

                // 1. markings for the shoulders
                if (laneCountLeft != null) {
                    drawVerticalLine(
                        color = edgeLineColor,
                        x = leftLanesStart * laneWidth,
                        strokeWidth = lineWidth,
                        pathEffect = edgeLinePathEffect,
                    )
                }
                if (laneCountRight != null) {
                    drawVerticalLine(
                        color = edgeLineColor,
                        x = rightLanesEnd * laneWidth,
                        strokeWidth = lineWidth,
                        pathEffect = edgeLinePathEffect,
                    )
                }

                // 2. lane markings
                if (isShowingLaneMarkings) {
                    if (laneCountLeft != null) {
                        for (x in 1..laneCountLeft) {
                            drawVerticalLine(
                                color = laneSeparatorLineColor,
                                x = (leftLanesStart + x) * laneWidth,
                                strokeWidth = lineWidth,
                                pathEffect = laneSeparatorPathEffect,
                            )
                        }
                    }
                    if (laneCountRight != null) {
                        for (x in 1..laneCountRight) {
                            drawVerticalLine(
                                color = laneSeparatorLineColor,
                                x = (rightLanesStart + x - 1) * laneWidth,
                                strokeWidth = lineWidth,
                                pathEffect = laneSeparatorPathEffect,
                            )
                        }
                    }
                }

                // 3. center line
                if (isShowingLaneMarkings && !hasCenterLeftTurnLane &&
                    laneCountLeft != null && laneCountRight != null &&
                    laneCountLeft > 0 && laneCountRight > 0
                ) {
                    val onlyTwoLanes = laneCountLeft + laneCountRight == 2
                    drawVerticalLine(
                        color = centerLineColor,
                        x = leftLanesEnd * laneWidth,
                        strokeWidth = lineWidth,
                        pathEffect = if (onlyTwoLanes) laneSeparatorPathEffect else null
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
                        x = (leftLanesEnd + LANE_MARKING_WIDTH * 2f) * laneWidth,
                        strokeWidth = lineWidth,
                        pathEffect = laneSeparatorPathEffect,
                    )
                    drawVerticalLine(
                        color = centerLineColor,
                        x = (rightLanesStart - LANE_MARKING_WIDTH * 2f) * laneWidth,
                        strokeWidth = lineWidth,
                        pathEffect = laneSeparatorPathEffect,
                    )
                }

                // 5. draw cars
                clipRect { // ensure cars are not drawn outside
                    val carWidth = (1f - 2f * CAR_LANE_PADDING) / zoom

                    carsLeft.forEachIndexed { index, car ->
                        drawCar(
                            carPainter = car.painter,
                            progress = car.progress,
                            carWidth = carWidth,
                            left = (leftLanesStart + index) * laneWidth,
                            laneWidth = laneWidth,
                            reverseY = !isLeftHandTraffic,
                        )
                    }
                    carsRight.forEachIndexed { index, car ->
                        drawCar(
                            carPainter = car.painter,
                            progress = car.progress,
                            carWidth = carWidth,
                            left = (rightLanesStart + index) * laneWidth,
                            laneWidth = laneWidth,
                            reverseY = isLeftHandTraffic,
                        )
                    }
                }
            }

            if (laneCountLeft != 0) {
                Spacer(
                    Modifier
                        .align(AbsoluteAlignment.TopLeft)
                        .fillMaxHeight()
                        .fillMaxWidth(leftLanesEnd / totalLaneWidth)
                        .clickable {
                            if (isLeftHandTraffic) onClickForwardSide()
                            else onClickBackwardSide()
                        }
                )
            }

            if (laneCountRight != 0) {
                Spacer(
                    Modifier
                        .align(AbsoluteAlignment.TopRight)
                        .fillMaxHeight()
                        .fillMaxWidth(1f - rightLanesStart / totalLaneWidth)
                        .clickable {
                            if (isLeftHandTraffic) onClickBackwardSide()
                            else onClickForwardSide()
                        }
                )
            }
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
    left: Float,
    laneWidth: Float,
    carWidth: Float,
    reverseY: Boolean
) {
    val w = carPainter.intrinsicSize.width
    val h = carPainter.intrinsicSize.height

    val carHeight = carWidth * h / w
    val carPad = (laneWidth - carWidth) / 2f

    translate(left = left) {
        rotate(
            degrees = if (reverseY) 180f else 0f,
            pivot = Offset(laneWidth / 2f, center.y)
        ) {
            translate(
                left = carPad,
                top = (carHeight + size.height) * (1 - progress) - carHeight,
            ) {
                with(carPainter) {
                    draw(Size(carWidth, carHeight))
                }
            }
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
private const val CAR_SPARSITY = 2.0f

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

private data class Car(
    val painter: Painter,
    var progress: Float,
    val speed: Float,
)

private fun createCar(
    carPainters: List<Painter>,
    progress: Float,
): Car {
    return Car(
        painter = carPainters[Random.nextInt(carPainters.size)],
        progress = progress,
        speed = CAR_SPEED * (1f + CAR_SPEED_VARIATION * Random.nextFloat())
    )
}

private fun LineStyle.getPathEffect(lineWidth: Float): PathEffect? = when (this) {
    LineStyle.CONTINUOUS -> null
    LineStyle.DASHES -> PathEffect.dashPathEffect(floatArrayOf(lineWidth * 6, lineWidth * 10))
    LineStyle.SHORT_DASHES -> PathEffect.dashPathEffect(floatArrayOf(lineWidth * 4, lineWidth * 4))
}
