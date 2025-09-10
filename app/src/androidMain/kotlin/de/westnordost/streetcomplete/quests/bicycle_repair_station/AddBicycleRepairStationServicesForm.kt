package de.westnordost.streetcomplete.quests.bicycle_repair_station

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.ui.common.image_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddBicycleRepairStationServicesForm : AImageListQuestForm<BicycleRepairStationService, List<BicycleRepairStationService>>() {

    override val items = BicycleRepairStationService.entries
    override val maxSelectableItems = -1
    override val itemsPerRow = 3

    @Composable override fun BoxScope.ItemContent(item: BicycleRepairStationService) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItems: List<BicycleRepairStationService>) {
        applyAnswer(selectedItems)
    }
}
