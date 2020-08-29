package de.westnordost.streetcomplete.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Region
import android.text.BoringLayout
import android.text.Layout
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.withSave


class VerticalTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
)
    : AppCompatTextView(context, attrs, defStyleAttr) {

    private val topDown = gravity.let { g ->
        !(Gravity.isVertical(g) && g.and(Gravity.VERTICAL_GRAVITY_MASK) == Gravity.BOTTOM)
    }
    private val metrics = BoringLayout.Metrics()
    private var padLeft = 0
    private var padTop = 0

    private var layout1: Layout? = null

    override fun setText(text: CharSequence, type: BufferType) {
        super.setText(text, type)
        layout1 = null
    }

    private fun makeLayout(): Layout {
        if (layout1 == null) {
            metrics.width = height
            paint.color = currentTextColor
            paint.drawableState = drawableState
            layout1 = BoringLayout.make(text, paint, metrics.width, Layout.Alignment.ALIGN_NORMAL, 2f, 0f, metrics, false, TextUtils.TruncateAt.END, height - compoundPaddingLeft - compoundPaddingRight)
            padLeft = compoundPaddingLeft
            padTop = extendedPaddingTop
        }
        return layout1!!
    }
    override fun onDraw(c: Canvas) {
        //      c.drawColor(0xffffff80); // TEST
        if (layout == null)
            return
        c.withSave {
            if (topDown) {
                val fm = paint.fontMetrics
                translate(textSize - (fm.bottom + fm.descent), 0f)
                rotate(90f)
            } else {
                translate(textSize, height.toFloat())
                rotate(-90f)
            }
            translate(padLeft.toFloat(), padTop.toFloat())
            makeLayout().draw(this)
        }
    }
}