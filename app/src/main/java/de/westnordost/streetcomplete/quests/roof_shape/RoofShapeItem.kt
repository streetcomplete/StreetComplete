package de.westnordost.streetcomplete.quests.roof_shape

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.roof_shape.RoofShape.*
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.Item

fun RoofShape.asItem(): DisplayItem<RoofShape>? {
    val iconResId = iconResId ?: return null
    return Item(this, iconResId)
}

private val RoofShape.iconResId: Int? get() = when (this) {
    GABLED ->            R.drawable.ic_roof_gabled
    HIPPED ->            R.drawable.ic_roof_hipped
    FLAT ->              R.drawable.ic_roof_flat
    PYRAMIDAL ->         R.drawable.ic_roof_pyramidal
    HALF_HIPPED ->       R.drawable.ic_roof_half_hipped
    SKILLION ->          R.drawable.ic_roof_skillion
    GAMBREL ->           R.drawable.ic_roof_gambrel
    ROUND ->             R.drawable.ic_roof_round
    DOUBLE_SALTBOX ->    R.drawable.ic_roof_double_saltbox
    SALTBOX ->           R.drawable.ic_roof_saltbox
    MANSARD ->           R.drawable.ic_roof_mansard
    DOME ->              R.drawable.ic_roof_dome
    QUADRUPLE_SALTBOX -> R.drawable.ic_roof_quadruple_saltbox
    ROUND_GABLED ->      R.drawable.ic_roof_round_gabled
    ONION ->             R.drawable.ic_roof_onion
    CONE ->              R.drawable.ic_roof_cone
    MANY ->              null
}
