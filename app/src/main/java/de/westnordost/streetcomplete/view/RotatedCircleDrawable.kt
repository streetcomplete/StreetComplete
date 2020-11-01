package de.westnordost.streetcomplete.view

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Path
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.core.graphics.toRectF

/** Container that contains another drawable but rotates it and clips it so it is a circle */
class RotatedCircleDrawable(val drawable: Drawable) : Drawable() {

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

    override fun setAlpha(alpha: Int) {
        drawable.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        drawable.colorFilter = colorFilter
    }

    override fun getOpacity(): Int = drawable.opacity
}