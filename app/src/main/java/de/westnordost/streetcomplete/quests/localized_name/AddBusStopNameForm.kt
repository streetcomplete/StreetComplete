package de.westnordost.streetcomplete.quests.localized_name

import androidx.appcompat.app.AlertDialog

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.OtherAnswer


class AddBusStopNameForm : AAddLocalizedNameForm<BusStopNameAnswer>() {

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_name_answer_noName) { confirmNoName() },
        OtherAnswer(R.string.quest_streetName_answer_cantType) { showKeyboardInfo() }
    )

    override fun onClickOk(names: List<LocalizedName>) {
        applyAnswer(BusStopName(names))
    }

    private fun confirmNoName() {
        AlertDialog.Builder(activity!!)
            .setTitle(R.string.quest_name_answer_noName_confirmation_title)
            .setPositiveButton(R.string.quest_name_noName_confirmation_positive) { _, _ -> applyAnswer(NoBusStopName) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }
}
