package de.westnordost.streetcomplete.quests.railway_crossing

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.railway_crossing.RailwayCrossingBarrier.*
import de.westnordost.streetcomplete.view.image_select.Item

fun List<RailwayCrossingBarrier>.toItems(isLeftHandTraffic: Boolean) =
    map { it.asItem(isLeftHandTraffic) }

fun RailwayCrossingBarrier.asItem(isLeftHandTraffic: Boolean): Item<RailwayCrossingBarrier> = when(this) {
    NO -> Item(this, R.drawable.ic_railway_crossing_none, R.string.quest_railway_crossing_barrier_none2)
    HALF -> Item(this, if (isLeftHandTraffic) R.drawable.ic_railway_crossing_half_l else R.drawable.ic_railway_crossing_half)
    DOUBLE_HALF -> Item(this, R.drawable.ic_railway_crossing_double_half)
    FULL -> Item(this, if (isLeftHandTraffic) R.drawable.ic_railway_crossing_full_l else R.drawable.ic_railway_crossing_full)
}
