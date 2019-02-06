package de.westnordost.streetcomplete.quests.way_lit

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AYesNoQuestAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer

class WayLitForm : AYesNoQuestAnswerFragment<String>() {

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_way_lit_24_7) { applyAnswer("24/7") },
        OtherAnswer(R.string.quest_way_lit_automatic) { applyAnswer("automatic") }
    )

    override fun onClick(answer: Boolean) {
        applyAnswer(if(answer) "yes" else "no")
    }
}
