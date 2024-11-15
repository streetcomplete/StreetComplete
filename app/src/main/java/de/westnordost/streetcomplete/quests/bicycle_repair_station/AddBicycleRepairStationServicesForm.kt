package de.westnordost.streetcomplete.quests.bicycle_repair_station

import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddBicycleRepairStationServicesForm : AImageListQuestForm<BicycleRepairStationService, List<BicycleRepairStationService>>() {

    override val items get() = BicycleRepairStationService.entries.map { it.asItem() }

    override val maxSelectableItems = -1

    override fun onClickOk(selectedItems: List<BicycleRepairStationService>) {
        applyAnswer(selectedItems)
    }
}
