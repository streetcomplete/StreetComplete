package de.westnordost.streetcomplete.quests.railway_crossing

import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.railway_crossing.RailwayCrossingBarrier.*

class AddRailwayCrossingBarrierForm : AImageListQuestAnswerFragment<RailwayCrossingBarrier, RailwayCrossingBarrier>() {

    override val items get() =
        listOf(NO, HALF, DOUBLE_HALF, FULL).toItems(countryInfo.isLeftHandTraffic)

    override val itemsPerRow = 4

    override fun onClickOk(selectedItems: List<RailwayCrossingBarrier>) {
        applyAnswer(selectedItems.single())
    }
}
