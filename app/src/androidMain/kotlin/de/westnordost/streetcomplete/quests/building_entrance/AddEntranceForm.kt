package de.westnordost.streetcomplete.quests.building_entrance

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.ui.common.quest.RadioGroupQuestForm
import org.jetbrains.compose.resources.stringResource

class AddEntranceForm : AbstractOsmQuestForm<EntranceAnswer>() {

    @Composable
    override fun Content() {
        val items = remember { EntranceType.entries + EntranceAnswer.IsDeadEnd }
        RadioGroupQuestForm(
            items = items,
            itemContent = { Text(stringResource(it.text)) },
            onClickOk = { applyAnswer(it) }
        )
    }
}
