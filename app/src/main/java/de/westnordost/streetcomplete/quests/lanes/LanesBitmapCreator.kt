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
    showOnlyRightSide: Boolean,
    drawLaneMarkings: Boolean
): Bitmap? {
    if (laneCountLeft == 0 && laneCountRight == 0 || showOnlyRightSide && laneCountRight == 0) return null
    val maxLaneCount = max(laneCountLeft, laneCountRight)
    val totalLaneCount = laneCountLeft + laneCountRight
    val bothSidesDefined = laneCountLeft > 0 && laneCountRight > 0
    val shoulder = 0.125f

    val roomForLanes = when {
        bothSidesDefined -> totalLaneCount
        showOnlyRightSide -> laneCountRight
        else -> maxLaneCount * 2
    }

    val laneWidth = width / (shoulder * 2f + when {
        bothSidesDefined  -> totalLaneCount
        showOnlyRightSide -> laneCountRight
        else              -> 2 * maxLaneCount
    })

    val leftLanesOffset = shoulder

    val rightLanesOffset = shoulder + when {
        bothSidesDefined -> laneCountLeft
        showOnlyRightSide -> 0
        else -> max(laneCountLeft, laneCountRight)
    }

    val shoulderWidth = shoulder * laneWidth
    val height = (width / (roomForLanes + shoulder * 2)).toInt()
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint()

    // draw background
    paint.color = Color.parseColor("#808080")
    paint.style = Paint.Style.FILL
    val leftOffset = if (laneCountLeft > 0 || showOnlyRightSide) 0f else laneWidth * rightLanesOffset
    val rightOffset = if (laneCountRight > 0) 0f else laneWidth * rightLanesOffset
    canvas.drawRect(leftOffset, 0f, width - rightOffset, height.toFloat(), paint)

    // draw markings:
    paint.color = Color.parseColor("#ffffff")
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 0.0625f * laneWidth

    // 1. markings for the shoulders
    if (laneCountLeft > 0 || showOnlyRightSide) {
        val leftShoulderX = leftLanesOffset * laneWidth
        canvas.drawLine(leftShoulderX, 0f, leftShoulderX, height.toFloat(), paint)
    }
    if (laneCountRight > 0) {
        val rightShoulderX = shoulderWidth + roomForLanes * laneWidth
        canvas.drawLine(rightShoulderX, 0f, rightShoulderX, height.toFloat(), paint)
    }

    // 2. center line
    if (bothSidesDefined && totalLaneCount > 2 && drawLaneMarkings) {
        val offsetX = rightLanesOffset * laneWidth
        canvas.drawLine(offsetX, 0f, offsetX, height.toFloat(), paint)
    }

    // 3. lane markings
    if (drawLaneMarkings) {
        for (x in 0 until laneCountLeft) {
            canvas.drawVerticalDashedLine(shoulderWidth + x * laneWidth, paint)
        }
        for (x in 0 until laneCountRight) {
            canvas.drawVerticalDashedLine((rightLanesOffset + x) * laneWidth, paint)
        }
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
