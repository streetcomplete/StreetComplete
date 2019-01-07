package de.westnordost.streetcomplete.quests.localized_name

import android.os.Bundle
import android.support.v7.app.AlertDialog

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.OtherAnswer
import kotlinx.android.synthetic.main.quest_localizedname.*


class AddBusStopNameForm : AddLocalizedNameForm() {

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_name_answer_noName) { confirmNoName() },
        OtherAnswer(R.string.quest_streetName_answer_cantType) { showKeyboardInfo() }
    )

    override fun onClickOk() {
        applyNameAnswer()
    }

    private fun confirmNoName() {
        AlertDialog.Builder(activity!!)
            .setTitle(R.string.quest_name_answer_noName_confirmation_title)
            .setPositiveButton(R.string.quest_name_noName_confirmation_positive) { _, _ ->
                val data = Bundle()
                data.putBoolean(AddLocalizedNameForm.NO_NAME, true)
                applyAnswer(data)
            }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }
}
