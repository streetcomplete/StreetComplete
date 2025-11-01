package de.westnordost.streetcomplete.quests.monument_memorial_name

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem

class IsMonumentOrMemorialForm : AbstractOsmQuestForm<Boolean>() {

    override val buttonPanelAnswers = listOf(
        // True if memorial, this will initiate a change in IsMonumentOrMemorial
        AnswerItem(R.string.quest_is_monument_or_memorial_option_memorial) { applyAnswer(true) },
        AnswerItem(R.string.quest_is_monument_or_memorial_option_monument) { applyAnswer(false) }
    )
}
