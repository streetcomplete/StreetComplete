package de.westnordost.streetcomplete.quests.show_poi

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem

class ShowFixmeAnswerForm : AbstractOsmQuestForm<Boolean>() {

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_fixme_remove) { applyAnswer(false) }
    )

}
