package de.westnordost.streetcomplete.quests.drinking_water_type

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddDrinkingWaterTypeForm : AImageListQuestForm<DrinkingWaterType, DrinkingWaterType>() {

    override val items = DrinkingWaterType.entries
    override val itemsPerRow = 3

    @Composable override fun BoxScope.ItemContent(item: DrinkingWaterType) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItems: List<DrinkingWaterType>) {
        applyAnswer(selectedItems.single())
    }
}
