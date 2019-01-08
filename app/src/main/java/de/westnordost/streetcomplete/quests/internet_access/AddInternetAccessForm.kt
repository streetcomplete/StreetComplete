package de.westnordost.streetcomplete.quests.internet_access

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import kotlinx.android.synthetic.main.quest_internet_access.*

class AddInternetAccessForm : AbstractQuestFormAnswerFragment() {

    override val contentLayoutResId = R.layout.quest_internet_access

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        radioButtonGroup.setOnCheckedChangeListener { _, _ -> checkIsFormComplete() }
    }

    override fun onClickOk() {
        val internetAccessValue = when (radioButtonGroup.checkedRadioButtonId) {
            R.id.wlan -> "wlan"
            R.id.no -> "no"
            R.id.terminal -> "terminal"
            R.id.wired -> "wired"
            else -> null
        }
        applyAnswer(bundleOf(INTERNET_ACCESS to internetAccessValue))
    }

    override fun isFormComplete() = radioButtonGroup.checkedRadioButtonId != -1

    companion object {
        const val INTERNET_ACCESS = "internet_access"
    }
}
