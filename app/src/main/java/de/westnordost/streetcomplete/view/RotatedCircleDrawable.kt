package de.westnordost.streetcomplete.view

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Path
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.core.graphics.toRectF

/** Container that contains another drawable but rotates it and clips it so it is a circle */
class RotatedCircleDrawable(val drawable: Drawable) : Drawable() {

    override fun getIntrinsicWidth(): Int = drawable.intrinsicWidth
    override fun getIntrinsicHeight(): Int = drawable.intrinsicHeight

    override fun setAlpha(alpha: Int) { drawable.alpha = alpha }
    override fun getAlpha(): Int = drawable.alpha

    override fun setColorFilter(colorFilter: ColorFilter?) { drawable.colorFilter = colorFilter }
    override fun getColorFilter() = drawable.colorFilter

    override fun getOpacity(): Int = drawable.opacity

    var rotation: Float = 0f
    set(value) {
        field = value
        invalidateSelf()
    }

    override fun draw(canvas: Canvas) {
        val w = bounds.width()
        val h = bounds.height()
        val path = Path()
        path.addOval(Rect(0, 0, w,h).toRectF(), Path.Direction.CW)
        canvas.clipPath(path)
        canvas.rotate(rotation, w/2f, h/2f)
        drawable.bounds = bounds
        drawable.draw(canvas)
    }
}
