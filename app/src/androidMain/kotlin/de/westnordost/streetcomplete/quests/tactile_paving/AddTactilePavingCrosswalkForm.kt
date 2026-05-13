package de.westnordost.streetcomplete.quests.tactile_paving

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.tactile_paving.TactilePavingCrosswalkAnswer.*
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddTactilePavingCrosswalkForm(
    onAnswer: (TactilePavingCrosswalkAnswer) -> Unit,
) {
    QuestForm(
        answers = listOf(
            Answer(stringResource(Res.string.quest_generic_hasFeature_no)) { onAnswer(NO) },
            Answer(stringResource(Res.string.quest_generic_hasFeature_yes)) { onAnswer(YES) }
        ),
        otherAnswers = listOf(
            Answer(stringResource(Res.string.quest_tactilePaving_incorrect)) { onAnswer(INCORRECT) }
        )
    )
}
