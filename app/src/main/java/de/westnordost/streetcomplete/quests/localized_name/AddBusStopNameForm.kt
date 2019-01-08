package de.westnordost.streetcomplete.quests.localized_name

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.OtherAnswer


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
                applyAnswer(bundleOf(NO_NAME to true))
            }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }
}
