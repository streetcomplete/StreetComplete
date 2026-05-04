package de.westnordost.streetcomplete.quests.drinking_water

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.ui.common.quest.RadioGroupQuestForm
import org.jetbrains.compose.resources.stringResource

class AddDrinkingWaterForm : AbstractOsmQuestForm<DrinkingWater>() {

    @Composable
    override fun Content() {
        RadioGroupQuestForm(
            items = DrinkingWater.entries,
            itemContent = { Text(stringResource(it.text)) },
            onClickOk = { applyAnswer(it) }
        )
    }
}
