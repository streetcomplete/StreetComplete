package de.westnordost.streetcomplete.quests

import android.os.Bundle
import android.view.View

import de.westnordost.streetcomplete.R
import kotlinx.android.synthetic.main.quest_buttonpanel_yes_no.*

/** Abstract base class for dialogs in which the user answers a yes/no quest  */
open class YesNoQuestAnswerFragment : AbstractQuestAnswerFragment() {

    override val buttonsResId = R.layout.quest_buttonpanel_yes_no

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        yesButton.setOnClickListener { onClickYesNo(true) }
        noButton.setOnClickListener { onClickYesNo(false) }
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
