package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.R

class YesNoQuestForm : AbstractOsmQuestForm<Boolean>() {

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(false) },
        AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(true) }
    )
}
