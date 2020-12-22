package de.westnordost.streetcomplete.quests.construction

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AYesNoQuestAnswerFragment
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import kotlinx.android.synthetic.main.quest_buttonpanel_yes_no.*
import java.util.*

class MarkCompletedConstructionForm : AYesNoQuestAnswerFragment<CompletedConstructionAnswer>() {
    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_construction_completes_at_known_date) { setFinishDate() }
    )

    private fun setFinishDate() {
        val tomorrow = Calendar.getInstance()
        tomorrow.add(Calendar.DAY_OF_MONTH, 1)
        val year = tomorrow.get(Calendar.YEAR)
        val month = tomorrow.get(Calendar.MONTH)
        val day = tomorrow.get(Calendar.DAY_OF_MONTH)
        val dpd = DatePickerDialog(requireContext(), { _, yearSelected, monthSelected, dayOfMonthSelected ->
            applyAnswer(OpeningDateAnswer(Date(yearSelected-1900, monthSelected, dayOfMonthSelected)))
        }, year, month, day)
        dpd.setTitle(resources.getString(R.string.quest_construction_completion_date_title))
        dpd.datePicker.minDate = tomorrow.timeInMillis
        dpd.show()
    }

    override fun onClick(answer: Boolean) {
        applyAnswer(StateAnswer(answer))
    }
}
