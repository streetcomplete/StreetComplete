package de.westnordost.streetcomplete.quests.railway_crossing

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.view.Item

class AddRailwayCrossingBarrierForm : AImageListQuestAnswerFragment<String,String>() {

    override val items get() = listOf(
        Item("no", R.drawable.ic_railway_crossing_none, R.string.quest_railway_crossing_barrier_none),
        Item("half", if (countryInfo.isLeftHandTraffic) R.drawable.ic_railway_crossing_half_l else R.drawable.ic_railway_crossing_half),
        Item("double_half", R.drawable.ic_railway_crossing_double_half),
        Item("full", if (countryInfo.isLeftHandTraffic) R.drawable.ic_railway_crossing_full_l else R.drawable.ic_railway_crossing_full)
    )

    override val itemsPerRow = 4

    override fun onClickOk(selectedItems: List<String>) {
        applyAnswer(selectedItems.single())
    }
}
