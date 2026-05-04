package de.westnordost.streetcomplete.quests.accepts_cards

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.ui.common.quest.RadioGroupQuestForm
import org.jetbrains.compose.resources.stringResource

class AddAcceptsCardsForm : AbstractOsmQuestForm<CardAcceptance>() {

    @Composable
    override fun Content() {
        RadioGroupQuestForm(
            items = CardAcceptance.entries,
            itemContent = { Text(stringResource(it.text)) },
            onClickOk = { applyAnswer(it) }
        )
    }
}
