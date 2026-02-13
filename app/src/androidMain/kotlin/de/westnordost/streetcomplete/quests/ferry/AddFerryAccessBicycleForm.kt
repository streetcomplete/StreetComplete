package de.westnordost.streetcomplete.quests.ferry

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem

class AddFerryAccessBicycleForm : AbstractOsmQuestForm<FerryBicycleAccess>() {

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(FerryBicycleAccess.NO) },
        AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(FerryBicycleAccess.YES) }
    )

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_generic_answer_noSign) { applyAnswer(FerryBicycleAccess.NOT_SIGNED) }
    )
}
