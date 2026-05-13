package de.westnordost.streetcomplete.quests.bbq_fuel

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.QuestConfirmationDialog
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.RadioGroupQuestForm
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddBbqFuelForm(
    onAnswer: (BbqFuelAnswer) -> Unit
) {
    var confirmNotBbq by remember { mutableStateOf(false) }

    RadioGroupQuestForm(
        items = BbqFuel.entries,
        itemContent = { Text(stringResource(it.text)) },
        onClickOk = onAnswer,
        otherAnswers = listOf(
            Answer(stringResource(Res.string.quest_bbq_fuel_not_a_bbq)) { confirmNotBbq = true },
        )
    )

    if (confirmNotBbq) {
        QuestConfirmationDialog(
            onDismissRequest = { confirmNotBbq = false },
            onConfirmed = { onAnswer(BbqFuelAnswer.IsFirePit) },
            text = { Text(stringResource(Res.string.quest_bbq_fuel_not_a_bbq_confirmation)) }
        )
    }
}
