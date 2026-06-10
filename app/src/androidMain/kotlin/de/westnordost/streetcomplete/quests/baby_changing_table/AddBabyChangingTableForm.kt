package de.westnordost.streetcomplete.quests.baby_changing_table

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.baby_changing_table.BabyChangingTableAnswer.*

class AddBabyChangingTableForm : AbstractOsmQuestForm<BabyChangingTableAnswer>() {

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(NO) },
        AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(YES) }
    )

    override val otherAnswers get() = listOf(
        AnswerItem(R.string.quest_wheelchairAccessPat_noToilet) { applyAnswer(NO_TOILET) }
    )
}
