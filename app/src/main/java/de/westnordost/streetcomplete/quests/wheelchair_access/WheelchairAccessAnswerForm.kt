package de.westnordost.streetcomplete.quests.wheelchair_access

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.R

import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import kotlinx.android.synthetic.main.quest_buttonpanel_yes_limited_no.*

open class WheelchairAccessAnswerForm : AbstractQuestAnswerFragment<String>() {

    override val buttonsResId = R.layout.quest_buttonpanel_yes_limited_no

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        yesButton.setOnClickListener { applyAnswer("yes") }
        limitedButton.setOnClickListener { applyAnswer("limited") }
        noButton.setOnClickListener { applyAnswer("no") }
    }
}
