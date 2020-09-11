package de.westnordost.streetcomplete.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint.Align
import android.graphics.Rect
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ktx.toPx
import kotlin.math.min

class VerticalLabelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val textPaint = TextPaint()
    private var text: String? = null
    private val textBounds = Rect()
    private val orientationRight: Boolean

    init {
        textPaint.isAntiAlias = true
        textPaint.textAlign = Align.CENTER

        val a = context.obtainStyledAttributes(attrs, R.styleable.VerticalLabelView)

        setText(a.getString(R.styleable.VerticalLabelView_android_text))
        setTextSize(a.getDimensionPixelSize(R.styleable.VerticalLabelView_android_textSize, 16f.toPx(context).toInt()))
        setTextColor(a.getColor(R.styleable.VerticalLabelView_android_textColor, Color.BLACK))
        orientationRight = a.getBoolean(R.styleable.VerticalLabelView_orientationRight, false)
        a.recycle()
    }

    fun setText(text: String?) {
        this.text = text
        requestLayout()
        invalidate()
    }

    fun setTextSize(size: Int) {
        textPaint.textSize = size.toFloat()
        requestLayout()
        invalidate()
    }

    fun setTextColor(color: Int) {
        textPaint.color = color
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        text.let {
            textPaint.getTextBounds(it.orEmpty(), 0, text?.length ?: 0, textBounds)
        }

        val desiredWidth = textBounds.height() + paddingLeft + paddingRight
        val desiredHeight = textBounds.width() + paddingTop + paddingBottom

        val width = reconcileSize(desiredWidth, widthMeasureSpec)
        val height = reconcileSize(desiredHeight, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    private fun reconcileSize(contentSize: Int, measureSpec: Int): Int {
        val mode = MeasureSpec.getMode(measureSpec)
        val size = MeasureSpec.getSize(measureSpec)
        return when (mode) {
            MeasureSpec.EXACTLY -> size
            MeasureSpec.AT_MOST -> min(contentSize, size)
            else -> contentSize
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        text?.let {
            val originY = paddingTop + textBounds.width() / 2f
            if (orientationRight) {
                val originX = paddingLeft -  textBounds.height() - textPaint.ascent()
                canvas.translate(originX, originY)
                canvas.rotate(90f)
            } else {
                val originX = paddingLeft - textPaint.ascent()
                canvas.translate(originX, originY)
                canvas.rotate(-90f)
            }
            canvas.drawText(it, 0f, 0f, textPaint)
        }
    }
}
