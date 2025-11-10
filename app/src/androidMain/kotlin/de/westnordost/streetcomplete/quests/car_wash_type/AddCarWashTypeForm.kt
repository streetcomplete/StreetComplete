package de.westnordost.streetcomplete.quests.car_wash_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AItemsSelectQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddCarWashTypeForm : AItemsSelectQuestForm<CarWashType, Set<CarWashType>>() {

    override val items = CarWashType.entries
    override val itemsPerRow = 3
    override val serializer = serializer<CarWashType>()

    @Composable override fun ItemContent(item: CarWashType) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItems: Set<CarWashType>) {
        applyAnswer(selectedItems)
    }
}
