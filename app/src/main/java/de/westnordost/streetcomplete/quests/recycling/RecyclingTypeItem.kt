package de.westnordost.streetcomplete.quests.recycling

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.recycling.RecyclingType.OVERGROUND_CONTAINER
import de.westnordost.streetcomplete.quests.recycling.RecyclingType.RECYCLING_CENTRE
import de.westnordost.streetcomplete.quests.recycling.RecyclingType.UNDERGROUND_CONTAINER
import de.westnordost.streetcomplete.view.image_select.Item

fun RecyclingType.asItem() = Item(this, iconResId, titleResId)

private val RecyclingType.titleResId: Int get() = when (this) {
    OVERGROUND_CONTAINER ->  R.string.overground_recycling_container
    UNDERGROUND_CONTAINER -> R.string.underground_recycling_container
    RECYCLING_CENTRE ->      R.string.recycling_centre
}

private val RecyclingType.iconResId: Int get() = when (this) {
    OVERGROUND_CONTAINER ->  R.drawable.recycling_container
    UNDERGROUND_CONTAINER -> R.drawable.recycling_container_underground
    RECYCLING_CENTRE ->      R.drawable.recycling_centre
}
