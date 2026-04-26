package de.westnordost.streetcomplete.quests.tactile_paving

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.tactile_paving.TactilePavingCrosswalkAnswer.*
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.Answers
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

class TactilePavingCrosswalkForm : AbstractOsmQuestForm<TactilePavingCrosswalkAnswer>() {

    @Composable
    override fun Content() {
        QuestForm(
            answers = Answers(
                Answer(stringResource(Res.string.quest_generic_hasFeature_no)) { applyAnswer(NO) },
                Answer(stringResource(Res.string.quest_generic_hasFeature_yes)) { applyAnswer(YES) }
            ),
            otherAnswers = listOf(
                Answer(stringResource(Res.string.quest_tactilePaving_incorrect)) { applyAnswer(INCORRECT) }
            )
        )
    }
}
