package de.westnordost.streetcomplete.quests.fire_hydrant_ref

import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.ApplicationConstants.MAX_OSM_TAG_VALUE_LENGTH
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.AreYouSureDialog
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.theme.extraLargeInput
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddFireHydrantRefForm(
    on: (QuestAction<FireHydrantRefAnswer>) -> Unit
) {
    var ref by rememberSaveable { mutableStateOf("") }
    val isTooLong by remember { derivedStateOf { ref.length > MAX_OSM_TAG_VALUE_LENGTH } }
    var confirmNoRef by remember { mutableStateOf(false) }

    QuestForm(
        on = on,
        isComplete = ref.isNotEmpty() && !isTooLong,
        onClickOk = { on(Answer(FireHydrantRef(ref))) },
        otherAnswers = { listOf(
            AnswerItem(stringResource(Res.string.quest_ref_answer_noRef)) { confirmNoRef = true }
        ) }
    ) {
        TextField(
            value = ref,
            onValueChange = { ref = it },
            textStyle = MaterialTheme.typography.extraLargeInput,
            isError = isTooLong,
        )
    }


    if (confirmNoRef) {
        AreYouSureDialog(
            onDismissRequest = { confirmNoRef = false },
            onConfirmed = { on(Answer(FireHydrantRefAnswer.NoSign)) }
        )
    }
}
