package de.westnordost.streetcomplete.quests.moped

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.moped.ProhibitedForMopedAnswer.FORBIDDEN
import de.westnordost.streetcomplete.quests.moped.ProhibitedForMopedAnswer.YES
import de.westnordost.streetcomplete.quests.moped.ProhibitedForMopedAnswer.DESIGNATED


class AddProhibitedForMopedForm : AbstractOsmQuestForm<ProhibitedForMopedAnswer>() {

    override val contentLayoutResId = R.layout.quest_moped

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(FORBIDDEN) },
        AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(YES) },
        AnswerItem(R.string.quest_moped_prohibited_designated) { applyAnswer(DESIGNATED) }
    )
}
