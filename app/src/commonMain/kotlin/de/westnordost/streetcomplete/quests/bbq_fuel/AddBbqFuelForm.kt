package de.westnordost.streetcomplete.quests.bbq_fuel

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.AreYouSureDialog
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.RadioGroupQuestForm
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddBbqFuelForm(
    on: (QuestAction<BbqFuelAnswer>) -> Unit
) {
    var confirmNotBbq by remember { mutableStateOf(false) }

    RadioGroupQuestForm(
        on = on,
        items = BbqFuel.entries,
        itemContent = { Text(stringResource(it.text)) },
        otherAnswers = { listOf(
            AnswerItem(stringResource(Res.string.quest_bbq_fuel_not_a_bbq)) { confirmNotBbq = true },
        ) }
    )

    if (confirmNotBbq) {
        AreYouSureDialog(
            onDismissRequest = { confirmNotBbq = false },
            onConfirmed = { on(Answer(BbqFuelAnswer.IsFirePit)) },
            text = { Text(stringResource(Res.string.quest_bbq_fuel_not_a_bbq_confirmation)) }
        )
    }
}
