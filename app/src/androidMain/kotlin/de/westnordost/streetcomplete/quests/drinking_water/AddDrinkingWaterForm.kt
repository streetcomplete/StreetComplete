package de.westnordost.streetcomplete.quests.drinking_water

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.ARadioGroupQuestForm
import org.jetbrains.compose.resources.stringResource

class AddDrinkingWaterForm : ARadioGroupQuestForm<DrinkingWater, DrinkingWater>() {

    override val items = DrinkingWater.entries

    @Composable override fun BoxScope.ItemContent(item: DrinkingWater) {
        Text(stringResource(item.text))
    }
}
