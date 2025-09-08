package de.westnordost.streetcomplete.quests.railway_crossing

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.railway_crossing.RailwayCrossingBarrier.CHICANE
import de.westnordost.streetcomplete.quests.railway_crossing.RailwayCrossingBarrier.DOUBLE_HALF
import de.westnordost.streetcomplete.quests.railway_crossing.RailwayCrossingBarrier.FULL
import de.westnordost.streetcomplete.quests.railway_crossing.RailwayCrossingBarrier.GATE
import de.westnordost.streetcomplete.quests.railway_crossing.RailwayCrossingBarrier.HALF
import de.westnordost.streetcomplete.quests.railway_crossing.RailwayCrossingBarrier.NO
import de.westnordost.streetcomplete.view.image_select.Item

fun RailwayCrossingBarrier.asItem(isLeftHandTraffic: Boolean): Item<RailwayCrossingBarrier> =
    Item(this, getIconResId(isLeftHandTraffic), titleResId)

private val RailwayCrossingBarrier.titleResId: Int? get() = when (this) {
    NO ->   R.string.quest_railway_crossing_barrier_none2
    else -> null
}

private fun RailwayCrossingBarrier.getIconResId(isLeftHandTraffic: Boolean): Int = when (this) {
    NO ->          R.drawable.ic_railway_crossing_none
    HALF ->        if (isLeftHandTraffic) R.drawable.ic_railway_crossing_half_l else R.drawable.ic_railway_crossing_half
    DOUBLE_HALF -> R.drawable.ic_railway_crossing_double_half
    FULL ->        if (isLeftHandTraffic) R.drawable.ic_railway_crossing_full_l else R.drawable.ic_railway_crossing_full
    GATE ->        R.drawable.ic_railway_crossing_gate
    CHICANE ->     R.drawable.ic_railway_crossing_chicane
}
