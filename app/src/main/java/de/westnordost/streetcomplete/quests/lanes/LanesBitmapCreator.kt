package de.westnordost.streetcomplete.quests.lanes

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.max

/** creates a bitmap height that shows the lanes */
fun createLanesBitmap(
    height: Int,
    laneCount: Int,
    otherLaneCount: Int?,
    drawLaneMarkings: Boolean
): Bitmap {

    val shoulderWidth = 0.125f * height
    val shouldersWidth = shoulderWidth * (if (otherLaneCount == null) 2 else 1)
    val offsetX = if (otherLaneCount == null) shoulderWidth else 0f
    val width = (height * max(laneCount, otherLaneCount ?: 0) + shouldersWidth).toInt()
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint()

    // draw background
    paint.color = Color.GRAY
    paint.style = Paint.Style.FILL
    canvas.drawRect(0f, 0f, height * laneCount + shouldersWidth, height.toFloat(), paint)

    // draw markings:
    paint.color = Color.WHITE
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 0.0625f * height

    // 1. separator to the other side
    if (otherLaneCount != null && laneCount + otherLaneCount > 2 && drawLaneMarkings) {
        canvas.drawLine(offsetX, 0f, offsetX, height.toFloat(), paint)
    }

    // 2. markings for the shoulders
    val rightShoulderX = laneCount * height + offsetX
    canvas.drawLine(rightShoulderX, 0f, rightShoulderX, height.toFloat(), paint)

    if (otherLaneCount == null) {
        canvas.drawLine(shoulderWidth, 0f, shoulderWidth, height.toFloat(), paint)
    }

    // 3. lane markings
    if (drawLaneMarkings) {
        for (x in 0 until laneCount) {
            val startX = x * height * 1f + offsetX
            for (y in 0..2) {
                val startY = (0.125f + y * 0.5f) * height
                val endY = startY + 0.25f * height
                canvas.drawLine(startX, startY, startX, endY, paint)
            }
        }
    }

    return bitmap
}
