package de.westnordost.streetcomplete.quests.wheelchair_access

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.R

import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import kotlinx.android.synthetic.main.quest_buttonpanel_yes_limited_no.*

open class WheelchairAccessAnswerFragment : AbstractQuestAnswerFragment() {

    override val buttonsResId = R.layout.quest_buttonpanel_yes_limited_no

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        yesButton.setOnClickListener { onClickAnswer("yes") }
        limitedButton.setOnClickListener { onClickAnswer("limited") }
        noButton.setOnClickListener { onClickAnswer("no") }
    }

    private fun onClickAnswer(answer: String) {
        val bundle = Bundle()
        bundle.putString(ANSWER, answer)
        applyAnswer(bundle)
    }

    companion object {
        const val ANSWER = "answer"
    }
}
