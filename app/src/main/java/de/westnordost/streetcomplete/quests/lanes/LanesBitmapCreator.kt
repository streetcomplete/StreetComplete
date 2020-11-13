package de.westnordost.streetcomplete.quests.lanes

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.max

/** creates a bitmap with the given [width] that shows the lanes */
fun createLanesBitmap(
    width: Int,
    laneCountLeft: Int,
    laneCountRight: Int,
    hasCenterLeftTurnLane: Boolean,
    showOnlyRightSide: Boolean,
    drawLaneMarkings: Boolean
): Bitmap? {
    require(laneCountLeft >= 0)
    require(laneCountRight >= 0)

    val bothSidesDefined = laneCountLeft > 0 && laneCountRight > 0
    val noSidesDefined = laneCountLeft == 0 && laneCountRight == 0

    val shoulder = 0.125f

    val laneCountCenter = (if (hasCenterLeftTurnLane) 1 else 0)

    val lanesSpace = laneCountCenter + when {
        bothSidesDefined ->  laneCountLeft + laneCountRight
        showOnlyRightSide -> max(1, laneCountRight)
        noSidesDefined ->    2
        else ->              2 * max(laneCountLeft, laneCountRight)
    }

    val laneWidth = width / (shoulder * 2f + lanesSpace)

    val leftLanesStart = shoulder

    val leftLanesEnd = shoulder + when {
        bothSidesDefined ->  laneCountLeft
        showOnlyRightSide -> 0
        noSidesDefined ->    1
        else ->              max(laneCountLeft, laneCountRight)
    }

    val rightLanesStart = leftLanesEnd + laneCountCenter

    val shoulderWidth = shoulder * laneWidth
    val height = (width / (lanesSpace + shoulder * 2)).toInt()
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint()

    // draw background
    paint.color = Color.parseColor("#808080")
    paint.style = Paint.Style.FILL
    val streetStartX = if (laneCountLeft > 0 || showOnlyRightSide) 0f else laneWidth * leftLanesEnd
    val streetEndX = if (laneCountRight > 0) width.toFloat() else laneWidth * rightLanesStart
    canvas.drawRect(streetStartX, 0f, streetEndX, height.toFloat(), paint)

    // draw markings:
    paint.color = Color.parseColor("#ffffff")
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 0.0625f * laneWidth

    // 1. markings for the shoulders
    if (laneCountLeft > 0 || showOnlyRightSide) {
        val leftShoulderX = leftLanesStart * laneWidth
        canvas.drawVerticalLine(leftShoulderX, paint)
    }
    if (laneCountRight > 0) {
        val rightShoulderX = shoulderWidth + lanesSpace * laneWidth
        canvas.drawVerticalLine(rightShoulderX, paint)
    }

    // 2. center line
    if (bothSidesDefined && !hasCenterLeftTurnLane && laneCountLeft + laneCountRight > 2 && drawLaneMarkings) {
        val offsetX = leftLanesEnd * laneWidth
        canvas.drawVerticalLine(offsetX, paint)
    }

    // 3. lane markings
    if (drawLaneMarkings) {
        for (x in 0 until laneCountLeft) {
            canvas.drawVerticalDashedLine(shoulderWidth + x * laneWidth, paint)
        }
        for (x in 0 until laneCountRight) {
            canvas.drawVerticalDashedLine((rightLanesStart + x) * laneWidth, paint)
        }

    }

    // 4. center turn lane markings
    paint.color = Color.parseColor("#ffff00")
    if (hasCenterLeftTurnLane) {
        canvas.drawVerticalLine(leftLanesEnd * laneWidth, paint)
        canvas.drawVerticalLine(rightLanesStart * laneWidth, paint)
        canvas.drawVerticalDashedLine((leftLanesEnd + 0.125f) * laneWidth, paint)
        canvas.drawVerticalDashedLine((rightLanesStart - 0.125f) * laneWidth, paint)
    }

    return bitmap
}

private fun Canvas.drawVerticalDashedLine(x: Float, paint: Paint) {
    for (y in 0..2) {
        val startY = (0.125f + y * 0.5f) * height
        val endY = startY + 0.25f * height
        drawLine(x, startY, x, endY, paint)
    }
}

private fun Canvas.drawVerticalLine(x: Float, paint: Paint) {
    drawLine(x, 0f, x, height.toFloat(), paint)
}
