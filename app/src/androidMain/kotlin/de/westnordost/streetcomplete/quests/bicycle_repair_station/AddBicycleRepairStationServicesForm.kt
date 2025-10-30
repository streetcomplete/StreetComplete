package de.westnordost.streetcomplete.quests.bicycle_repair_station

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AItemsSelectQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddBicycleRepairStationServicesForm : AItemsSelectQuestForm<BicycleRepairStationService, Set<BicycleRepairStationService>>() {

    override val items = BicycleRepairStationService.entries
    override val itemsPerRow = 3
    override val serializer = serializer<BicycleRepairStationService>()

    @Composable override fun ItemContent(item: BicycleRepairStationService) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItems: Set<BicycleRepairStationService>) {
        applyAnswer(selectedItems)
    }
}
