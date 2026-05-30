package de.westnordost.streetcomplete.quests.building_entrance_reference

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAnswer
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddEntranceReferenceForm(
    onAnswer: (QuestAnswer<EntranceReferenceAnswer>) -> Unit
) {
    var entranceReference by rememberSerializable { mutableStateOf(lastEntranceReference?.clear()) }

    QuestForm(
        isComplete = entranceReference?.isComplete() == true,
        hasChanges = entranceReference != null,
        onClickOk = {
            lastEntranceReference = entranceReference
            onAnswer(Answer(entranceReference!!))
        },
        onAnswer = onAnswer,
        otherAnswers = listOf(
            AnswerItem(stringResource(Res.string.quest_entrance_reference_nothing_signed)) {
                onAnswer(Answer(EntranceReferenceAnswer.NotSigned))
            },
        )
    ) {
        EntranceReferenceForm(
            value = entranceReference,
            onValueChange = { entranceReference = it },
        )
    }
}

private var lastEntranceReference: EntranceReference? = null
