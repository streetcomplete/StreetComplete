package de.westnordost.streetcomplete.quests.building_entrance_reference

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.Confirm
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.jetbrains.compose.resources.stringResource

class AddEntranceReferenceForm : AbstractOsmQuestForm<EntranceReferenceAnswer>() {

    @Composable
    override fun Content() {
        var entranceReference by rememberSerializable { mutableStateOf(lastEntranceReference?.clear()) }

        QuestForm(
            answers = Confirm(
                isComplete = entranceReference?.isComplete() == true,
                hasChanges = entranceReference != null,
            ) {
                lastEntranceReference = entranceReference
                applyAnswer(entranceReference!!)
            },
            otherAnswers = listOf(
                Answer(stringResource(Res.string.quest_entrance_reference_nothing_signed)) {
                    applyAnswer(EntranceReferenceAnswer.NotSigned)
                },
            )
        ) {
            EntranceReferenceForm(
                value = entranceReference,
                onValueChange = { entranceReference = it },
            )
        }
    }


    companion object {
        private var lastEntranceReference: EntranceReference? = null
    }
}
