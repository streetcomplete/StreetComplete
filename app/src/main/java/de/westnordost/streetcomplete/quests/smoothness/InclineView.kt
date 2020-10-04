package de.westnordost.streetcomplete.quests.smoothness

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class InclineView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    private var degrees : Float? = null

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val paint = Paint()
        paint.color = Color.LTGRAY

        canvas?.drawRect(0F, 0F, width.toFloat(), height.toFloat(), paint)
        canvas?.save()
        if (degrees != null) {
            canvas?.rotate(-degrees!!, width.toFloat() / 2F, height.toFloat() / 2F)
        }

        paint.color = Color.BLACK
        canvas?.drawRect(-width.toFloat(), height.toFloat() / 2F, width.toFloat() * 2F, height.toFloat() * 2F, paint)
        canvas?.restore()
    }


    fun changeIncline(inclineInDegrees: Float) {
        degrees = inclineInDegrees
        invalidate()
    }

}
