package de.westnordost.streetcomplete.quests.tactile_paving

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.tactile_paving.TactilePavingStepsAnswer.BOTTOM
import de.westnordost.streetcomplete.quests.tactile_paving.TactilePavingStepsAnswer.NO
import de.westnordost.streetcomplete.quests.tactile_paving.TactilePavingStepsAnswer.TOP
import de.westnordost.streetcomplete.quests.tactile_paving.TactilePavingStepsAnswer.YES

class TactilePavingStepsForm : AbstractOsmQuestForm<TactilePavingStepsAnswer>() {

    override val contentLayoutResId = R.layout.quest_tactile_paving

    override val otherAnswers get() = listOf(
        AnswerItem(R.string.quest_tactilePaving_steps_top) { applyAnswer(TOP) },
        AnswerItem(R.string.quest_tactilePaving_steps_bottom) { applyAnswer(BOTTOM) }
    )

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(YES) },
        AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(NO) }
    )
}
