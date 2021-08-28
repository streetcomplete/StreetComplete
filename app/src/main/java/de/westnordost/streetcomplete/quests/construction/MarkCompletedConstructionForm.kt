package de.westnordost.streetcomplete.quests.construction

import android.app.DatePickerDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ktx.toInstant
import de.westnordost.streetcomplete.quests.AYesNoQuestAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import java.time.LocalDate

class MarkCompletedConstructionForm : AYesNoQuestAnswerFragment<CompletedConstructionAnswer>() {
    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_construction_completed_at_known_date) { setFinishDate() }
    )

    private fun setFinishDate() {
        val tomorrow = LocalDate.now().plusDays(1)
        val dpd = DatePickerDialog(requireContext(), { _, year, month, day ->
            applyAnswer(OpeningDateAnswer(LocalDate.of(year, month + 1, day)))
        }, tomorrow.year, tomorrow.monthValue - 1, tomorrow.dayOfMonth)
        dpd.setTitle(resources.getString(R.string.quest_construction_completion_date_title))
        dpd.datePicker.minDate = tomorrow.toInstant().toEpochMilli()
        dpd.show()
    }

    override fun onClick(answer: Boolean) {
        applyAnswer(StateAnswer(answer))
    }
}
