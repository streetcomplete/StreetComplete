package de.westnordost.streetcomplete.quests.railway_crossing

import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.railway_crossing.RailwayCrossingBarrier.CHICANE
import de.westnordost.streetcomplete.quests.railway_crossing.RailwayCrossingBarrier.DOUBLE_HALF
import de.westnordost.streetcomplete.quests.railway_crossing.RailwayCrossingBarrier.FULL
import de.westnordost.streetcomplete.quests.railway_crossing.RailwayCrossingBarrier.GATE
import de.westnordost.streetcomplete.quests.railway_crossing.RailwayCrossingBarrier.HALF
import de.westnordost.streetcomplete.quests.railway_crossing.RailwayCrossingBarrier.NO
import de.westnordost.streetcomplete.view.image_select.DisplayItem

class AddRailwayCrossingBarrierForm : AImageListQuestForm<RailwayCrossingBarrier, RailwayCrossingBarrier>() {

    override val items: List<DisplayItem<RailwayCrossingBarrier>> get() {
        val isPedestrian = element.tags["railway"] == "crossing"
        val answers = if (isPedestrian) listOf(NO, CHICANE, GATE, FULL) else listOf(NO, HALF, DOUBLE_HALF, FULL)
        return answers.toItems(countryInfo.isLeftHandTraffic)
    }

    override val itemsPerRow = 4

    override fun onClickOk(selectedItems: List<RailwayCrossingBarrier>) {
        applyAnswer(selectedItems.single())
    }
}
