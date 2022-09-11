package de.westnordost.streetcomplete.quests.roof_shape

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.roof_shape.RoofShape.CONE
import de.westnordost.streetcomplete.quests.roof_shape.RoofShape.DOME
import de.westnordost.streetcomplete.quests.roof_shape.RoofShape.DOUBLE_SALTBOX
import de.westnordost.streetcomplete.quests.roof_shape.RoofShape.FLAT
import de.westnordost.streetcomplete.quests.roof_shape.RoofShape.GABLED
import de.westnordost.streetcomplete.quests.roof_shape.RoofShape.GAMBREL
import de.westnordost.streetcomplete.quests.roof_shape.RoofShape.HALF_HIPPED
import de.westnordost.streetcomplete.quests.roof_shape.RoofShape.HIPPED
import de.westnordost.streetcomplete.quests.roof_shape.RoofShape.MANSARD
import de.westnordost.streetcomplete.quests.roof_shape.RoofShape.MANY
import de.westnordost.streetcomplete.quests.roof_shape.RoofShape.ONION
import de.westnordost.streetcomplete.quests.roof_shape.RoofShape.PYRAMIDAL
import de.westnordost.streetcomplete.quests.roof_shape.RoofShape.QUADRUPLE_SALTBOX
import de.westnordost.streetcomplete.quests.roof_shape.RoofShape.ROUND
import de.westnordost.streetcomplete.quests.roof_shape.RoofShape.ROUND_GABLED
import de.westnordost.streetcomplete.quests.roof_shape.RoofShape.SALTBOX
import de.westnordost.streetcomplete.quests.roof_shape.RoofShape.SKILLION
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
