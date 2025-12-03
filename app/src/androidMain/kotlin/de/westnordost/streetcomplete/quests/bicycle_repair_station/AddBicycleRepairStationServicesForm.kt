package de.westnordost.streetcomplete.quests.bicycle_repair_station

import android.os.Bundle
import android.view.View
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        preselectedItems =  items.filter {
            element.tags["service:bicycle:" + it.value] == "yes"
        }.toSet()
        super.onViewCreated(view, savedInstanceState)
    }
}
