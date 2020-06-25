package de.westnordost.streetcomplete.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
import de.westnordost.streetcomplete.R
import kotlin.math.sqrt

open class CircularMaskFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {

    var circularity: Float = 1f
    set(value) {
        val newVal = value.coerceIn(0f,1f)
        field = newVal
        invalidate()
    }

    init {
        context.withStyledAttributes(attrs, R.styleable.CircularMaskFrameLayout) {
            circularity = getFloat(R.styleable.CircularMaskFrameLayout_circularity, 1f)
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val diff = 2 * sqrt(w*w + h*h) / (w+h) - 0.9f
        val xoffs = diff * width * (1 - circularity)
        val yoffs = diff * height * (1 - circularity)

        val path = Path()
        path.addOval(
            RectF(0f - xoffs/2, 0f - yoffs/2, width + xoffs, height + yoffs),
            Path.Direction.CW
        )
        canvas.clipPath(path)
        super.dispatchDraw(canvas)
    }
}
