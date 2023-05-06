package de.westnordost.streetcomplete.quests.railway_crossing

import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.view.image_select.DisplayItem

class AddRailwayCrossingBarrierForm : AImageListQuestForm<RailwayCrossingBarrier, RailwayCrossingBarrier>() {

    override val items: List<DisplayItem<RailwayCrossingBarrier>> get() {
        val isPedestrian = element.tags["railway"] == "crossing"
        return RailwayCrossingBarrier.getSelectableValues(isPedestrian)
            .map { it.asItem(countryInfo.isLeftHandTraffic) }
    }

    override val itemsPerRow = 4

    override fun onClickOk(selectedItems: List<RailwayCrossingBarrier>) {
        applyAnswer(selectedItems.single())
    }
}
