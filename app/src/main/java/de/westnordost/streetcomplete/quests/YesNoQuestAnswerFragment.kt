package de.westnordost.streetcomplete.quests

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import de.westnordost.streetcomplete.R
import kotlinx.android.synthetic.main.quest_buttonpanel_yes_no.*

/** Abstract base class for dialogs in which the user answers a yes/no quest  */
open class YesNoQuestAnswerFragment : AbstractQuestAnswerFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        setButtonsView(R.layout.quest_buttonpanel_yes_no)

        yesButton.setOnClickListener { onClickYesNo(true) }
        noButton.setOnClickListener { onClickYesNo(false) }
        return view
    }

    protected open fun onClickYesNo(answer: Boolean) {
        val bundle = Bundle()
        bundle.putBoolean(ANSWER, answer)
        applyAnswer(bundle)
    }

    companion object {
        const val ANSWER = "answer"
    }
}
