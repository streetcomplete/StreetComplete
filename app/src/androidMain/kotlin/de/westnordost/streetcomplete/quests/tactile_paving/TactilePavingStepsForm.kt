package de.westnordost.streetcomplete.quests.tactile_paving

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.quests.tactile_paving.TactilePavingStepsAnswer.BOTTOM
import de.westnordost.streetcomplete.quests.tactile_paving.TactilePavingStepsAnswer.NO
import de.westnordost.streetcomplete.quests.tactile_paving.TactilePavingStepsAnswer.TOP
import de.westnordost.streetcomplete.quests.tactile_paving.TactilePavingStepsAnswer.YES
import de.westnordost.streetcomplete.ui.common.quest.Answers
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

class TactilePavingStepsForm : AbstractOsmQuestForm<TactilePavingStepsAnswer>() {

    @Composable
    override fun Content() {
        QuestForm(
            answers = Answers(
                Answer(stringResource(Res.string.quest_generic_hasFeature_no)) { applyAnswer(NO) },
                Answer(stringResource(Res.string.quest_generic_hasFeature_yes)) { applyAnswer(YES) }
            ),
            otherAnswers = listOf(
                Answer(stringResource(Res.string.quest_tactilePaving_steps_bottom)) { applyAnswer(BOTTOM) },
                Answer(stringResource(Res.string.quest_tactilePaving_steps_top)) { applyAnswer(TOP) }
            )
        )
    }
}
