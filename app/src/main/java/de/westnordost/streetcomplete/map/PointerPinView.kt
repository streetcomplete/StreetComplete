package de.westnordost.streetcomplete.map

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.View.MeasureSpec.getMode
import android.view.View.MeasureSpec.getSize
import androidx.core.graphics.withRotation
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ktx.toPx
import kotlin.math.*


/** A view for the pointer pin that ought to be displayed at the edge of the screen. */
class PointerPinView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val pointerPin: Drawable = context.resources.getDrawable(R.drawable.quest_pin_pointer)

    private var icon: Drawable? = null

    var pinRotation: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    fun setPinIcon(drawable: Drawable?) {
        icon = drawable
        invalidate()
    }

    fun setPinIcon(resId: Int) {
        icon = context.resources.getDrawable(resId)
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredSize = DEFAULT_SIZE.toPx(context).toInt()
        val width = reconcileSize(desiredSize, widthMeasureSpec)
        val height = reconcileSize(desiredSize, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas?) {
        val c = canvas ?: return

        val size = min(width, height)
        val r = pinRotation

        pointerPin.setBounds(0,0, size, size)
        c.withRotation(r, width/2f, height/2f) {
            pointerPin.draw(c)
        }

        val icon = icon
        if (icon != null) {
            val arrowSize = min(width, height) * POINTER_SIZE_FRACTION
            val iconSize = (size * ICON_SIZE_FRACTION).toInt()
            val a = r.toDouble().normalizeAngle().toRadians()
            val x = (-sin(a) * arrowSize).toInt()
            val y = (+cos(a) * arrowSize).toInt()
            icon.setBounds(
                width/2 - iconSize/2 + x,
                height/2 - iconSize/2 + y,
                width/2 + iconSize/2 + x,
                height/2 + iconSize/2 + y)
            icon.draw(c)
        }
    }

    private fun reconcileSize(contentSize: Int, measureSpec: Int): Int {
        val mode = getMode(measureSpec)
        val size = getSize(measureSpec)
        return when (mode) {
            MeasureSpec.EXACTLY -> size
            MeasureSpec.AT_MOST -> min(contentSize, size)
            else -> contentSize
        }
    }

    companion object {
        private const val POINTER_SIZE_FRACTION = 14f / 124f
        private const val ICON_SIZE_FRACTION = 84f / 124f
        private const val DEFAULT_SIZE = 64f // in dp
    }
}

private fun Double.toRadians(): Double = this / 180.0 * PI

private fun Double.normalizeAngle(): Double {
    var r = this % 360 // r is -360..360
    r = (r + 360) % 360 // r is 0..360
    return r
}
