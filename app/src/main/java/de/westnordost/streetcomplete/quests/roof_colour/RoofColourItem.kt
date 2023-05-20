package de.westnordost.streetcomplete.quests.roof_colour

import android.content.Context
import android.graphics.Color
import android.graphics.LightingColorFilter
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.DrawableImage
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.Item2

fun RoofColour.asItem(context: Context): DisplayItem<RoofColour> {
    val color = Color.parseColor(this.androidValue ?: this.osmValue)
    val drawable = context.getDrawable(R.drawable.ic_roof_colour)!!
    drawable.colorFilter = LightingColorFilter(color, Color.BLACK)
    val image = DrawableImage(drawable)
    return Item2(this, image)
}
