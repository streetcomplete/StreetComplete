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
import de.westnordost.streetcomplete.util.ktx.dpToPx
import kotlin.math.min

/** A very basic text view whose text is displayed vertically. No line break is possible */
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

        text = a.getString(R.styleable.VerticalLabelView_android_text)
        textPaint.textSize = a.getDimensionPixelSize(R.styleable.VerticalLabelView_android_textSize, context.dpToPx(16).toInt()).toFloat()
        textPaint.color = a.getColor(R.styleable.VerticalLabelView_android_textColor, Color.BLACK)
        val shadowColor = a.getColor(R.styleable.VerticalLabelView_android_shadowColor, Color.BLACK)
        val shadowDx = a.getFloat(R.styleable.VerticalLabelView_android_shadowDx, 0f)
        val shadowDy = a.getFloat(R.styleable.VerticalLabelView_android_shadowDy, 0f)
        val shadowRadius = a.getFloat(R.styleable.VerticalLabelView_android_shadowRadius, -1f)
        if (shadowRadius > 0f || shadowDx > 0f || shadowDy > 0f) {
            textPaint.setShadowLayer(shadowRadius, shadowDx, shadowDy, shadowColor)
        }
        textPaint.isFakeBoldText = true
        orientationRight = a.getBoolean(R.styleable.VerticalLabelView_orientationRight, false)
        a.recycle()
        requestLayout()
    }

    fun setText(text: String?) {
        if (this.text == text) return
        this.text = text
        requestLayout()
        invalidate()
    }

    fun setTextSize(size: Int) {
        if (textPaint.textSize == size.toFloat()) return
        textPaint.textSize = size.toFloat()
        requestLayout()
        invalidate()
    }

    fun setTextColor(color: Int) {
        if (textPaint.color == color) return
        textPaint.color = color
        invalidate()
    }

    fun setShadowLayer(radius: Float, dx: Float, dy: Float, shadowColor: Int) {
        textPaint.setShadowLayer(radius, dx, dy, shadowColor)
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
                val originX = paddingLeft - textBounds.height() - textPaint.ascent()
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
