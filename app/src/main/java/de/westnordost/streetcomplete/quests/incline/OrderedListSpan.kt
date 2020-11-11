package de.westnordost.streetcomplete.quests.incline

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.Spanned
import android.text.style.LeadingMarginSpan

/**
 * Use this span when you need to create an ordered list.
 */
class OrderedListSpan(
    private val width: Int,
    private val leadingText: String
) : LeadingMarginSpan {

    override fun drawLeadingMargin(
        canvas: Canvas,
        paint: Paint,
        x: Int,
        dir: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence,
        start: Int,
        end: Int,
        first: Boolean,
        layout: Layout
    ) {
        val spanStart = (text as Spanned).getSpanStart(this)
        val isFirstCharacter = spanStart == start
        if (isFirstCharacter) {
            canvas.drawText(leadingText, x.toFloat(), baseline.toFloat(), paint)
        }
    }

    override fun getLeadingMargin(first: Boolean): Int = width
}
