package de.westnordost.streetcomplete.view

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import androidx.core.graphics.withRotation
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ktx.flipHorizontally
import de.westnordost.streetcomplete.util.ktx.getBitmapDrawable
import kotlin.math.min

/** Drawable providing decoration, suitable for a circular background
 *  depends on what is set in level of drawable that ranges from 0 to 10000
 *  for all values - colour of background circle is affected
 *
 *  there can be also laurel wreath decoration:
 *  for 10000 - fully grown wreath with all pretty elements
 *  for values between 9999 and 1000 - may be losing elements as it gets smaller,
 *  with the first loss at it goes down from 10000 to 9999
 *  below 1000: guaranteed to have no decorative leaves at all
 */
class LaurelWreathDrawable(private val resources: Resources) : Drawable() {
    private val pairOfLaurelLeafs = resources.getBitmapDrawable(R.drawable.ic_laurel_leaf_pair)
    private val horizontalEndingLeaf = resources.getBitmapDrawable(R.drawable.ic_laurel_leaf_ending)
    private val backgroundPaint = Paint()

    private val antiAliasPaint: Paint = Paint().apply {
        isAntiAlias = true
        isFilterBitmap = true
    }

    override fun onLevelChange(level: Int) = true

    override fun draw(canvas: Canvas) {
        val canvasWidth: Int = bounds.width()
        val canvasHeight: Int = bounds.height()
        val circleRadius: Float = min(canvasWidth, canvasHeight).toFloat() / 2f

        backgroundPaint.color = Color.HSVToColor(floatArrayOf(93f, level / 10000f * 0.5f, 0.72f))

        canvas.drawCircle((canvasWidth / 2).toFloat(), (canvasHeight / 2).toFloat(), circleRadius, backgroundPaint)

        if (level < 1000) return

        val decorationSegmentImageWidth = pairOfLaurelLeafs.intrinsicWidth // width is the same as intrinsicWidth

        val maximumDecorationSegmentCount = 11f

        val circleCenterX = canvasWidth / 2f
        val shownSegments = ((maximumDecorationSegmentCount - 1) * level / 10000).toInt()
        val howDistantIsDecorationFromCircleCenter = 0.78f

        for (i in 1..shownSegments) {
            var bitmap = pairOfLaurelLeafs.bitmap
            if (i == shownSegments) {
                bitmap = horizontalEndingLeaf.bitmap
            }

            // left side
            canvas.withRotation(i * 180.0f / maximumDecorationSegmentCount, canvasWidth / 2f, canvasHeight / 2f) {
                // drawBitmap takes corner of the bitmap, we care about centering segments
                canvas.drawBitmap(bitmap, circleCenterX - decorationSegmentImageWidth / 2f, canvasHeight * howDistantIsDecorationFromCircleCenter, antiAliasPaint)
            }

            // right side
            val flippedBitmap = bitmap.flipHorizontally()
            canvas.withRotation(-i * 180.0f / maximumDecorationSegmentCount, canvasWidth / 2f, canvasHeight / 2f) {
                canvas.drawBitmap(flippedBitmap, circleCenterX - decorationSegmentImageWidth / 2f, canvasHeight * howDistantIsDecorationFromCircleCenter, antiAliasPaint)
            }
        }
    }

    override fun setAlpha(alpha: Int) {
        // This method is required
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        // This method is required
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.OPAQUE
}
