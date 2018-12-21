package de.westnordost.streetcomplete.quests.way_lit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class WayLitForm : YesNoQuestAnswerFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        addOtherAnswer(R.string.quest_way_lit_24_7) { applyAnswer("24/7") }
        addOtherAnswer(R.string.quest_way_lit_automatic) { applyAnswer("automatic") }

        return view
    }

    private fun applyAnswer(value: String) {
        val answer = Bundle()
        answer.putString(OTHER_ANSWER, value)
        applyAnswer(answer)
    }

    companion object {
        val OTHER_ANSWER = "OTHER_ANSWER"
    }
}
