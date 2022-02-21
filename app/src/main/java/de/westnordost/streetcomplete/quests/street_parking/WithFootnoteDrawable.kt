package de.westnordost.streetcomplete.quests.street_parking

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.drawable.Drawable
import de.westnordost.streetcomplete.R

/** Container that contains another drawable but adds a fat "*" in the upper right corner */
class WithFootnoteDrawable(context: Context, val drawable: Drawable) : Drawable() {
    override fun getIntrinsicWidth(): Int = drawable.intrinsicWidth
    override fun getIntrinsicHeight(): Int = drawable.intrinsicHeight

    override fun setAlpha(alpha: Int) { drawable.alpha = alpha }
    override fun getAlpha(): Int = drawable.alpha

    override fun setColorFilter(colorFilter: ColorFilter?) { drawable.colorFilter = colorFilter }
    override fun getColorFilter() = drawable.colorFilter

    override fun getOpacity(): Int = drawable.opacity

    private val footnoteDrawable = context.getDrawable(R.drawable.ic_fat_footnote)!!

    override fun draw(canvas: Canvas) {
        drawable.bounds = bounds
        drawable.draw(canvas)
        val size = bounds.width() * 3 / 8
        footnoteDrawable.setBounds(bounds.width() - size, 0, bounds.width(), size)
        footnoteDrawable.draw(canvas)
    }
}
