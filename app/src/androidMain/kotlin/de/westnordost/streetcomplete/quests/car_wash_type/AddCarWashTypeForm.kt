package de.westnordost.streetcomplete.quests.car_wash_type

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddCarWashTypeForm : AImageListQuestForm<CarWashType, List<CarWashType>>() {

    override val items = CarWashType.entries
    override val itemsPerRow = 3
    override val maxSelectableItems = -1

    @Composable override fun BoxScope.ItemContent(item: CarWashType) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItems: List<CarWashType>) {
        applyAnswer(selectedItems)
    }
}
