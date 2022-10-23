package de.westnordost.streetcomplete.view

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import androidx.core.graphics.withRotation
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ktx.flipHorizontally
import de.westnordost.streetcomplete.util.ktx.getBitmapDrawable

/*
Drawable providing decoration, suitable for a circular background
100 and more: fully grown wreath with all pretty elements
99 to 10: may be losing elements as it gets smaller
below: no decorative styling at all
 */
class LaurelWreathDrawable(val resources: Resources, private val percentageOfGrowth: Int) : Drawable() {
    private val laurelLeafOnStalk = resources.getBitmapDrawable(R.drawable.ic_laurel_leaf_rotated)
    private val horizontalEndingLeaf = resources.getBitmapDrawable(R.drawable.ic_laurel_leaf_ending)
    private val niceSubtleGreen: Paint = Paint().apply { setARGB(255, 152, 184, 126) }

    private val antiAliasPaint: Paint = Paint().apply {
        isAntiAlias = true
        isFilterBitmap = true
    }

    override fun draw(canvas: Canvas) {
        val canvasWidth: Int = bounds.width()
        val canvasHeight: Int = bounds.height()
        val circleRadius: Float = Math.min(canvasWidth, canvasHeight).toFloat() / 2f

        canvas.drawCircle((canvasWidth / 2).toFloat(), (canvasHeight / 2).toFloat(), circleRadius, niceSubtleGreen)

        if (percentageOfGrowth < 10) {
            return
        }

        val decorationSegmentImageWidth = laurelLeafOnStalk.intrinsicWidth // width is the same as intrinsicWidth

        val regularSegmentCount = 11f

        val circleCenterX = canvasWidth / 2f
        val shownSegments = ((regularSegmentCount - 1) * percentageOfGrowth / 100).toInt()
        val locationBetweenCenterAndEdge = 0.78f

        for (i in 1..shownSegments) {
            // https://developer.android.com/reference/kotlin/androidx/core/graphics/package-summary#(android.graphics.Canvas).withRotation(kotlin.Float,kotlin.Float,kotlin.Float,kotlin.Function1)
            var bitmap = laurelLeafOnStalk.bitmap
            if (i == shownSegments) {
                bitmap = horizontalEndingLeaf.bitmap
            }

            // left side
            canvas.withRotation(i * 180.0f / regularSegmentCount, canvasWidth / 2f, canvasHeight / 2f) {
                // drawBitmap takes corner of the bitmap, we care about centering segments
                canvas.drawBitmap(bitmap, circleCenterX - decorationSegmentImageWidth / 2f, canvasHeight * locationBetweenCenterAndEdge, antiAliasPaint)
            }

            // right side
            val flippedBitmap = bitmap.flipHorizontally()
            canvas.withRotation(-i * 180.0f / regularSegmentCount, canvasWidth / 2f, canvasHeight / 2f) {
                canvas.drawBitmap(flippedBitmap, circleCenterX - decorationSegmentImageWidth / 2f, canvasHeight * locationBetweenCenterAndEdge, antiAliasPaint)
            }
        }
    }

    override fun setAlpha(alpha: Int) {
        // This method is required
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        // This method is required
    }

    override fun getOpacity(): Int =
        // Must be PixelFormat.UNKNOWN, TRANSLUCENT, TRANSPARENT, or OPAQUE
        PixelFormat.OPAQUE
}
