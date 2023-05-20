package de.westnordost.streetcomplete.quests.roof_colour

import android.content.Context
import android.graphics.Color
import android.graphics.LightingColorFilter
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.roof_shape.RoofShape
import de.westnordost.streetcomplete.view.DrawableImage
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.Item2

fun RoofColour.asItem(context: Context, roofShape: RoofShape?): DisplayItem<RoofColour> {
    val color = Color.parseColor(this.androidValue ?: this.osmValue)
    val iconResId = roofShape?.colorIconResId ?: R.drawable.ic_roof_colour_gabled
    val drawable = context.getDrawable(iconResId)!!
    drawable.colorFilter = LightingColorFilter(color, Color.BLACK)
    val image = DrawableImage(drawable)
    return Item2(this, image)
}

private val RoofShape.colorIconResId: Int?
    get() = when (this) {
    RoofShape.GABLED ->            R.drawable.ic_roof_colour_gabled
    RoofShape.HIPPED ->            R.drawable.ic_roof_colour_hipped
    RoofShape.FLAT ->              R.drawable.ic_roof_colour_flat
    RoofShape.PYRAMIDAL ->         R.drawable.ic_roof_colour_pyramidal
    RoofShape.HALF_HIPPED ->       R.drawable.ic_roof_colour_half_hipped
    RoofShape.SKILLION ->          R.drawable.ic_roof_colour_skillion
    RoofShape.GAMBREL ->           R.drawable.ic_roof_colour_gambrel
    RoofShape.ROUND ->             R.drawable.ic_roof_colour_round
    RoofShape.DOUBLE_SALTBOX ->    R.drawable.ic_roof_colour_double_saltbox
    RoofShape.SALTBOX ->           R.drawable.ic_roof_colour_saltbox
    RoofShape.MANSARD ->           R.drawable.ic_roof_colour_mansard
    RoofShape.DOME ->              R.drawable.ic_roof_colour_dome
    RoofShape.QUADRUPLE_SALTBOX -> R.drawable.ic_roof_colour_quadruple_saltbox
    RoofShape.ROUND_GABLED ->      R.drawable.ic_roof_colour_round_gabled
    RoofShape.ONION ->             R.drawable.ic_roof_colour_onion
    RoofShape.CONE ->              R.drawable.ic_roof_colour_cone
    else ->                        null
}
