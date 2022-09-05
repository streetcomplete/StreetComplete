package de.westnordost.streetcomplete.quests.crossing_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.crossing_type.CrossingType.MARKED
import de.westnordost.streetcomplete.quests.crossing_type.CrossingType.TRAFFIC_SIGNALS
import de.westnordost.streetcomplete.quests.crossing_type.CrossingType.UNMARKED
import de.westnordost.streetcomplete.view.image_select.Item

fun CrossingType.asItem() = Item(this, iconResId, titleResId)

private val CrossingType.titleResId: Int get() = when (this) {
    TRAFFIC_SIGNALS -> R.string.quest_crossing_type_signals_controlled
    MARKED ->          R.string.quest_crossing_type_marked
    UNMARKED ->        R.string.quest_crossing_type_unmarked
}

private val CrossingType.iconResId: Int get() = when (this) {
    TRAFFIC_SIGNALS -> R.drawable.crossing_type_signals
    MARKED ->          R.drawable.crossing_type_zebra
    UNMARKED ->        R.drawable.crossing_type_unmarked
}
