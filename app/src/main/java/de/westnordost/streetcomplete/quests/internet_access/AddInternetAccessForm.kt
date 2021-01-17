package de.westnordost.streetcomplete.quests.internet_access

import android.os.Bundle
import android.view.View

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.internet_access.InternetAccess.*
import kotlinx.android.synthetic.main.quest_internet_access.*
import kotlin.NullPointerException

class AddInternetAccessForm : AbstractQuestFormAnswerFragment<InternetAccess>() {

    override val defaultExpanded = false

    override val contentLayoutResId = R.layout.quest_internet_access

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        radioButtonGroup.setOnCheckedChangeListener { _, _ -> checkIsFormComplete() }
    }

    override fun onClickOk() {
        applyAnswer(
            when (radioButtonGroup.checkedRadioButtonId) {
                R.id.wifi ->     WIFI
                R.id.no ->       NO
                R.id.terminal -> TERMINAL
                R.id.wired ->    WIRED
                else -> throw NullPointerException()
            }
        )
    }

    override fun isFormComplete() = radioButtonGroup.checkedRadioButtonId != -1
}
