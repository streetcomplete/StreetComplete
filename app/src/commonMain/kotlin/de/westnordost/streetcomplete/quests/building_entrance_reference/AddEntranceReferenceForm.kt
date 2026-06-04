package de.westnordost.streetcomplete.quests.building_entrance_reference

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddEntranceReferenceForm(
    on: (QuestAction<EntranceReferenceAnswer>) -> Unit
) {
    val initialEntranceReference = remember { lastEntranceReference?.clear() }
    var entranceReference by rememberSerializable { mutableStateOf(initialEntranceReference) }

    QuestForm(
        on = on,
        isComplete = entranceReference?.isComplete() == true,
        onClickOk = {
            lastEntranceReference = entranceReference
            on(Answer(entranceReference!!))
        },
        hasChanges = entranceReference != initialEntranceReference,
        otherAnswers = { listOf(
            AnswerItem(stringResource(Res.string.quest_entrance_reference_nothing_signed)) {
                on(Answer(EntranceReferenceAnswer.NotSigned))
            },
        ) }
    ) {
        EntranceReferenceForm(
            value = entranceReference,
            onValueChange = { entranceReference = it },
        )
    }
}

private var lastEntranceReference: EntranceReference? = null
