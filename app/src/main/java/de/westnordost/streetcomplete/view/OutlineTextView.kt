package de.westnordost.streetcomplete.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import de.westnordost.streetcomplete.R


/** A text view that is able to show an outline */
class OutlineTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle
) : AppCompatTextView(context, attrs, defStyleAttr) {

    var outlineWidth: Float
        set(value) { paint.strokeWidth = value }
        get() = paint.strokeWidth

    var outlineColor: Int = 0

    private var noInvalidate: Boolean = false

    private var _textColors: ColorStateList?
    private var _shadowRadius = 0f
    private var _shadowDx = 0f
    private var _shadowDy = 0f
    private var _shadowColor = 0

    init {
        _textColors = this.textColors
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.OutlineTextView)
            outlineColor = a.getColor(R.styleable.OutlineTextView_textOutlineColor, currentTextColor)
            outlineWidth = a.getDimensionPixelSize(R.styleable.OutlineTextView_textOutlineWidth, 0).toFloat()
            a.recycle()
        }
    }

    override fun setTextColor(color: Int) {
        _textColors = ColorStateList.valueOf(color)
        super.setTextColor(color)
    }

    override fun setTextColor(colors: ColorStateList?) {
        _textColors = colors
        super.setTextColor(colors)
    }

    override fun setShadowLayer(radius: Float, dx: Float, dy: Float, color: Int) {
        _shadowColor = color
        _shadowDx = dx
        _shadowDy = dy
        _shadowRadius = radius
        super.setShadowLayer(radius, dx, dy, color)
    }

    override fun invalidate() {
        if (noInvalidate) return
        super.invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        noInvalidate = true
        setPaintToOutline()
        noInvalidate = false
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        noInvalidate = true
        setPaintToOutline()
        super.onDraw(canvas)

        setPaintToRegular()
        super.onDraw(canvas)
        noInvalidate = false
    }

    private fun setPaintToOutline() {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = outlineWidth
        super.setTextColor(outlineColor)
        super.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
    }

    private fun setPaintToRegular() {
        paint.style = Paint.Style.FILL
        paint.strokeWidth = 0f
        super.setTextColor(_textColors)
        super.setShadowLayer(_shadowRadius, _shadowDx, _shadowDy, _shadowColor)
    }
}