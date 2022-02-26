package de.westnordost.streetcomplete.quests.smoking

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.TextItem
import de.westnordost.streetcomplete.quests.smoking.SmokingAllowed.YES
import de.westnordost.streetcomplete.quests.smoking.SmokingAllowed.OUTSIDE
import de.westnordost.streetcomplete.quests.smoking.SmokingAllowed.NO
import de.westnordost.streetcomplete.quests.smoking.SmokingAllowed.SEPARATED

class SmokingAllowedAnswerForm : AListQuestAnswerFragment<SmokingAllowed>() {

    override val items = listOf(
        TextItem(NO, R.string.quest_smoking_no),
        TextItem(OUTSIDE, R.string.quest_smoking_outside),
        TextItem(SEPARATED, R.string.quest_smoking_separated),
        TextItem(YES, R.string.quest_smoking_yes),
    )
}
