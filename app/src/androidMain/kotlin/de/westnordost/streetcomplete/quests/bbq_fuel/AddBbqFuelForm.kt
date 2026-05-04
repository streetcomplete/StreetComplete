package de.westnordost.streetcomplete.quests.bbq_fuel

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.QuestConfirmationDialog
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.RadioGroupQuestForm
import org.jetbrains.compose.resources.stringResource

class AddBbqFuelForm : AbstractOsmQuestForm<BbqFuelAnswer>() {

    @Composable
    override fun Content() {
        var confirmNotBbq by remember { mutableStateOf(false) }

        RadioGroupQuestForm(
            items = BbqFuel.entries,
            itemContent = { Text(stringResource(it.text)) },
            onClickOk = { applyAnswer(it) },
            otherAnswers = listOf(
                Answer(stringResource(Res.string.quest_bbq_fuel_not_a_bbq)) { confirmNotBbq = true },
            )
        )

        if (confirmNotBbq) {
            QuestConfirmationDialog(
                onDismissRequest = { confirmNotBbq = false },
                onConfirmed = { applyAnswer(BbqFuelAnswer.IsFirePit) },
                text = { Text(stringResource(Res.string.quest_bbq_fuel_not_a_bbq_confirmation)) }
            )
        }
    }
}
