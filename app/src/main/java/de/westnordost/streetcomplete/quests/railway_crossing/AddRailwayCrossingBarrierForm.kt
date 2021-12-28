package de.westnordost.streetcomplete.quests.railway_crossing

import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.railway_crossing.RailwayCrossingBarrier.*
import de.westnordost.streetcomplete.view.image_select.DisplayItem

class AddRailwayCrossingBarrierForm : AImageListQuestAnswerFragment<RailwayCrossingBarrier, RailwayCrossingBarrier>() {

    override val items: List<DisplayItem<RailwayCrossingBarrier>> get() {
        val isPedestrian = osmElement!!.tags["railway"] == "crossing"
        val answers = if (isPedestrian) listOf(NO, CHICANE, GATE, FULL) else listOf(NO, HALF, DOUBLE_HALF, FULL)
        return answers.toItems(countryInfo.isLeftHandTraffic)
    }

    override val itemsPerRow = 4

    override fun onClickOk(selectedItems: List<RailwayCrossingBarrier>) {
        applyAnswer(selectedItems.single())
    }
}
