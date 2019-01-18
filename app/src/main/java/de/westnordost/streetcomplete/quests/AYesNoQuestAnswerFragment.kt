package de.westnordost.streetcomplete.quests

import android.os.Bundle
import android.view.View

import de.westnordost.streetcomplete.R
import kotlinx.android.synthetic.main.quest_buttonpanel_yes_no.*

/** Abstract base class for dialogs in which the user answers a yes/no quest  */
abstract class AYesNoQuestAnswerFragment<T> : AbstractQuestAnswerFragment<T>() {

    override val buttonsResId = R.layout.quest_buttonpanel_yes_no

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        yesButton.setOnClickListener { onClick(true) }
        noButton.setOnClickListener { onClick(false) }
    }

    protected abstract fun onClick(answer: Boolean)
}
