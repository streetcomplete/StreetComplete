package de.westnordost.streetcomplete.quests.hairdresser

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.ui.common.quest.RadioGroupQuestForm
import org.jetbrains.compose.resources.stringResource

class AddHairdresserCustomersForm : AbstractOsmQuestForm<HairdresserCustomers>() {

    @Composable
    override fun Content() {
        RadioGroupQuestForm(
            items = HairdresserCustomers.entries,
            itemContent = { Text(stringResource(it.text)) },
            onClickOk = { applyAnswer(it) }
        )
    }
}
