package de.westnordost.streetcomplete.quests.wheelchair_access

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import kotlinx.android.synthetic.main.quest_buttonpanel_yes_limited_no.*

open class WheelchairAccessAnswerFragment : AbstractQuestAnswerFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        yesButton.setOnClickListener { onClickAnswer("yes") }
        limitedButton.setOnClickListener { onClickAnswer("limited") }
        noButton.setOnClickListener { onClickAnswer("no") }

        return view
    }

    private fun onClickAnswer(answer: String) {
        val bundle = Bundle()
        bundle.putString(ANSWER, answer)
        applyAnswer(bundle)
    }

    companion object {
        val ANSWER = "answer"
    }
}
