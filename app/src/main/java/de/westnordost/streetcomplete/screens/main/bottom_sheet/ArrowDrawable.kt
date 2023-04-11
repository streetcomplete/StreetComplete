package de.westnordost.streetcomplete.screens.main.bottom_sheet

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.PointF
import android.graphics.drawable.Drawable
import androidx.core.graphics.minus
import de.westnordost.streetcomplete.util.ktx.translate
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.sqrt

/** Drawable that draws an arrow from one point to another. The tip of the arrow is exactly at the
 *  end position while the start of the arrow is centered around the start position.  */
class ArrowDrawable(private val resources: Resources) : Drawable() {

    var startPoint: PointF? = null
    set(value) {
        field = value
        invalidateSelf()
    }
    var endPoint: PointF? = null
        set(value) {
            field = value
            invalidateSelf()
        }

    private val paint = Paint().also {
        it.isAntiAlias = true
        it.strokeWidth = 6 * resources.displayMetrics.density
        it.strokeCap = Paint.Cap.ROUND
    }

    override fun setTint(tintColor: Int) {
        paint.color = tintColor
        invalidateSelf()
    }

    override fun draw(canvas: Canvas) {
        val start = startPoint ?: return
        val endPoint = endPoint ?: return

        // draw line
        val angle = atan2(endPoint.y - start.y, endPoint.x - start.x).toDouble()
        val dist = (endPoint - start).length() - paint.strokeWidth / sqrt(2f)
        val end = start.translate(dist, angle)

        canvas.drawLine(start.x, start.y, end.x, end.y, paint)
        // draw arrow
        val arrowHeadSize = 14 * resources.displayMetrics.density
        val arrowHeadLineEnd1 = end.translate(arrowHeadSize, angle + PI * 3 / 4)
        val arrowHeardLineEnd2 = end.translate(arrowHeadSize, angle - PI * 3 / 4)
        canvas.drawLine(end.x, end.y, arrowHeadLineEnd1.x, arrowHeadLineEnd1.y, paint)
        canvas.drawLine(end.x, end.y, arrowHeardLineEnd2.x, arrowHeardLineEnd2.y, paint)
    }

    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(colorFilter: ColorFilter?) {}

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
