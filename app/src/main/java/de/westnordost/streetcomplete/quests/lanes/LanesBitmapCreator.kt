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
    otherLaneCount: Int,
    drawLaneMarkings: Boolean
): Bitmap {

    val shoulderWidth = 0.125f * height
    val width = (height * max(laneCount, otherLaneCount) + shoulderWidth).toInt()
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint()

    // draw background
    paint.color = Color.GRAY
    paint.style = Paint.Style.FILL
    canvas.drawRect(0f, 0f, height * laneCount + shoulderWidth, height.toFloat(), paint)

    // draw markings:
    paint.color = Color.WHITE
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 0.0625f * height
    val s = paint.strokeWidth / 2

    // 1. markings for the shoulders
    if (laneCount > 1 && drawLaneMarkings) {
        canvas.drawLine(0f, 0f, 0f, height.toFloat(), paint)
    }
    val shoulderX = laneCount * height + s
    canvas.drawLine(shoulderX, 0f, shoulderX, height.toFloat(), paint)

    // 2. lane markings
    if (drawLaneMarkings) {
        for (x in 0 until laneCount) {
            val startX = x * height * 1f
            for (y in 0..2) {
                val startY = (0.125f + y * 0.5f) * height
                val endY = startY + 0.25f * height
                canvas.drawLine(startX, startY, startX, endY, paint)
            }
        }
    }

    return bitmap
}
