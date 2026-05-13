package de.westnordost.streetcomplete.quests.tactile_paving

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.quests.tactile_paving.TactilePavingStepsAnswer.BOTTOM
import de.westnordost.streetcomplete.quests.tactile_paving.TactilePavingStepsAnswer.NO
import de.westnordost.streetcomplete.quests.tactile_paving.TactilePavingStepsAnswer.TOP
import de.westnordost.streetcomplete.quests.tactile_paving.TactilePavingStepsAnswer.YES
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddTactilePavingStepsForm(
    onAnswer: (TactilePavingStepsAnswer) -> Unit,
) {
    QuestForm(
        answers = listOf(
            Answer(stringResource(Res.string.quest_generic_hasFeature_no)) { onAnswer(NO) },
            Answer(stringResource(Res.string.quest_generic_hasFeature_yes)) { onAnswer(YES) }
        ),
        otherAnswers = listOf(
            Answer(stringResource(Res.string.quest_tactilePaving_steps_bottom)) { onAnswer(BOTTOM) },
            Answer(stringResource(Res.string.quest_tactilePaving_steps_top)) { onAnswer(TOP) }
        )
    )
}
