package de.westnordost.streetcomplete.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.FrameLayout
import de.westnordost.streetcomplete.ktx.toPx

open class CircularMaskFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {

    override fun dispatchDraw(canvas: Canvas) {
        val path = Path()
        path.addOval(
            RectF(0f, 0f, width.toFloat(), height.toFloat()),
            Path.Direction.CW
        )
        canvas.clipPath(path)
        super.dispatchDraw(canvas)
    }
}
