package de.westnordost.streetcomplete.quests.way_lit

import androidx.core.os.bundleOf

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class WayLitForm : YesNoQuestAnswerFragment() {

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_way_lit_24_7) { applyAnswer("24/7") },
        OtherAnswer(R.string.quest_way_lit_automatic) { applyAnswer("automatic") }
    )

    private fun applyAnswer(value: String) {
        applyAnswer(bundleOf(OTHER_ANSWER to value))
    }

    companion object {
        const val OTHER_ANSWER = "OTHER_ANSWER"
    }
}
