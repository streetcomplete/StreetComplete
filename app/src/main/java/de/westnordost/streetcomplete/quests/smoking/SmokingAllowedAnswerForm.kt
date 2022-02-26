package de.westnordost.streetcomplete.quests.smoking

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.smoking.SmokingAllowed.YES
import de.westnordost.streetcomplete.quests.smoking.SmokingAllowed.OUTSIDE
import de.westnordost.streetcomplete.quests.smoking.SmokingAllowed.NO
import de.westnordost.streetcomplete.quests.smoking.SmokingAllowed.SEPARATED

class SmokingAllowedAnswerForm : AbstractQuestAnswerFragment<SmokingAllowed>() {

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(NO) },
        AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(YES) },
        AnswerItem(R.string.quest_smoking_outside) { applyAnswer(OUTSIDE) },
    )

    override val otherAnswers get() = listOfNotNull(
        AnswerItem(R.string.quest_smoking_separated) { applyAnswer(SEPARATED) },
    )

}
