package de.westnordost.streetcomplete.quests.playground_access

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.ui.common.quest.RadioGroupQuestForm
import org.jetbrains.compose.resources.stringResource

class AddPlaygroundAccessForm : AbstractOsmQuestForm<PlaygroundAccess>() {

    @Composable
    override fun Content() {
        RadioGroupQuestForm(
            items = PlaygroundAccess.entries,
            itemContent = { Text(stringResource(it.text)) },
            onClickOk = { applyAnswer(it) }
        )
    }
}
