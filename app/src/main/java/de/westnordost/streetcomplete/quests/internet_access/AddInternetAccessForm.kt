package de.westnordost.streetcomplete.quests.internet_access

import android.os.Bundle
import android.view.View

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import kotlinx.android.synthetic.main.quest_internet_access.*
import kotlin.NullPointerException

class AddInternetAccessForm : AbstractQuestFormAnswerFragment<String>() {

    override val contentLayoutResId = R.layout.quest_internet_access

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        radioButtonGroup.setOnCheckedChangeListener { _, _ -> checkIsFormComplete() }
    }

    override fun onClickOk() {
        applyAnswer(
            when (radioButtonGroup.checkedRadioButtonId) {
                R.id.wlan ->     "wlan"
                R.id.no ->       "no"
                R.id.terminal -> "terminal"
                R.id.wired ->    "wired"
                else -> throw NullPointerException()
            }
        )
    }

    override fun isFormComplete() = radioButtonGroup.checkedRadioButtonId != -1
}
