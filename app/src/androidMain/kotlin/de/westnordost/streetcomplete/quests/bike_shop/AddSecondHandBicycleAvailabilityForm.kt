package de.westnordost.streetcomplete.quests.bike_shop

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.ui.common.quest.RadioGroupQuestForm
import org.jetbrains.compose.resources.stringResource

class AddSecondHandBicycleAvailabilityForm : AbstractOsmQuestForm<SecondHandBicycleAvailability>() {

    @Composable
    override fun Content() {
        RadioGroupQuestForm(
            items = SecondHandBicycleAvailability.entries,
            itemContent = { Text(stringResource(it.text)) },
            onClickOk = { applyAnswer(it) }
        )
    }
}
