package de.westnordost.streetcomplete.quests.show_poi

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import de.westnordost.streetcomplete.quests.AnswerItem

class ShowFixmeAnswerForm : AbstractQuestAnswerFragment<Boolean>() {

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_fixme_remove) { applyAnswer(false) }
    )

}
