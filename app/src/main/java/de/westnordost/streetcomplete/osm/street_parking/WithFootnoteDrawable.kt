package de.westnordost.streetcomplete.osm.street_parking

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.DrawableWrapper

/** Container that contains another drawable but adds a fat "*" in the upper right corner */
class WithFootnoteDrawable(context: Context, drawable: Drawable) : DrawableWrapper(drawable) {

    private val footnoteDrawable = context.getDrawable(R.drawable.ic_fat_footnote)!!

    override fun draw(canvas: Canvas) {
        drawable.bounds = bounds
        drawable.draw(canvas)
        val size = bounds.width() * 3 / 8
        footnoteDrawable.setBounds(bounds.width() - size, 0, bounds.width(), size)
        footnoteDrawable.draw(canvas)
    }
}
