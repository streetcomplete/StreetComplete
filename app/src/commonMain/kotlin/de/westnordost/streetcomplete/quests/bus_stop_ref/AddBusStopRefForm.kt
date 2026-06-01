package de.westnordost.streetcomplete.quests.bus_stop_ref

import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.QuestConfirmationDialog
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.theme.extraLargeInput
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddBusStopRefForm(
    on: (QuestAction<BusStopRefAnswer>) -> Unit
) {
    var ref by rememberSaveable { mutableStateOf("") }
    var confirmNoRef by remember { mutableStateOf(false) }

    QuestForm(
        isComplete = ref.isNotEmpty(),
        onClickOk = { on(Answer(BusStopRef(ref))) },
        on = on,
        otherAnswers = listOf(
            AnswerItem(stringResource(Res.string.quest_ref_answer_noRef)) { confirmNoRef = true }
        )
    ) {
        TextField(
            value = ref,
            onValueChange = { ref = it },
            textStyle = MaterialTheme.typography.extraLargeInput,
        )
    }

    if (confirmNoRef) {
        QuestConfirmationDialog(
            onDismissRequest = { confirmNoRef = false },
            onConfirmed = { on(Answer(NoVisibleBusStopRef)) }
        )
    }
}
