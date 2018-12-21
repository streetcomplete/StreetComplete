package de.westnordost.streetcomplete.quests.internet_access

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import kotlinx.android.synthetic.main.quest_internet_access.*

class AddInternetAccessForm : AbstractQuestFormAnswerFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        setContentView(R.layout.quest_internet_access)

        radioButtonGroup.setOnCheckedChangeListener { _, _ -> checkIsFormComplete() }

        return view
    }

    override fun onClickOk() {
        val internetAccessValue = when (radioButtonGroup.checkedRadioButtonId) {
            R.id.wlan -> "wlan"
            R.id.no -> "no"
            R.id.terminal -> "terminal"
            R.id.wired -> "wired"
            else -> null
        }
        val answer = Bundle()
        answer.putString(INTERNET_ACCESS, internetAccessValue)
        applyAnswer(answer)
    }

    override fun isFormComplete() = radioButtonGroup.checkedRadioButtonId != -1

    companion object {
        const val INTERNET_ACCESS = "internet_access"
    }
}
