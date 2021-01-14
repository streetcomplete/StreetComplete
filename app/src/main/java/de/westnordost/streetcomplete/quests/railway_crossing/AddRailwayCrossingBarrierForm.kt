package de.westnordost.streetcomplete.quests.railway_crossing

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.railway_crossing.RailwayCrossingBarrier.*
import de.westnordost.streetcomplete.view.image_select.Item

class AddRailwayCrossingBarrierForm : AImageListQuestAnswerFragment<RailwayCrossingBarrier, RailwayCrossingBarrier>() {

    override val items get() = listOf(
        Item(NO, R.drawable.ic_railway_crossing_none, R.string.quest_railway_crossing_barrier_none2),
        Item(HALF, if (countryInfo.isLeftHandTraffic) R.drawable.ic_railway_crossing_half_l else R.drawable.ic_railway_crossing_half),
        Item(DOUBLE_HALF, R.drawable.ic_railway_crossing_double_half),
        Item(FULL, if (countryInfo.isLeftHandTraffic) R.drawable.ic_railway_crossing_full_l else R.drawable.ic_railway_crossing_full)
    )

    override val itemsPerRow = 4

    override fun onClickOk(selectedItems: List<RailwayCrossingBarrier>) {
        applyAnswer(selectedItems.single())
    }
}
