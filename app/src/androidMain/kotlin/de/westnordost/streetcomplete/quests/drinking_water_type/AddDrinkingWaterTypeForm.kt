package de.westnordost.streetcomplete.quests.drinking_water_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddDrinkingWaterTypeForm : AItemSelectQuestForm<DrinkingWaterType, DrinkingWaterType>() {

    override val items = DrinkingWaterType.entries
    override val itemsPerRow = 3
    override val serializer = serializer<DrinkingWaterType>()

    @Composable override fun ItemContent(item: DrinkingWaterType) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItem: DrinkingWaterType) {
        applyAnswer(selectedItem)
    }
}
