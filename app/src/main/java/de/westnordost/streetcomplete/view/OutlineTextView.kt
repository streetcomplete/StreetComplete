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

    private var _textColors: ColorStateList? = null

    init {

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

    override fun invalidate() {
        if (noInvalidate) return
        super.invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        paint.style = Paint.Style.STROKE
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        paint.style = Paint.Style.STROKE
        noInvalidate = true
        super.setTextColor(outlineColor)
        super.onDraw(canvas)
        paint.style = Paint.Style.FILL
        super.setTextColor(_textColors)
        noInvalidate = false
        super.onDraw(canvas)
    }
}