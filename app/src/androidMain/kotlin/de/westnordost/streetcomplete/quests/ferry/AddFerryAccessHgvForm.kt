package de.westnordost.streetcomplete.quests.ferry

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem

class AddFerryAccessHgvForm : AbstractOsmQuestForm<FerryHgvAccess>() {

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(FerryHgvAccess.NO) },
        AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(FerryHgvAccess.YES) }
    )

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_generic_answer_noSign) { applyAnswer(FerryHgvAccess.NOT_SIGNED) }
    )
}
