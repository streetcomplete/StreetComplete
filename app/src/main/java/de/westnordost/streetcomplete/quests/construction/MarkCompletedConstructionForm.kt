package de.westnordost.streetcomplete.quests.construction

import android.app.DatePickerDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ktx.toInstant
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import de.westnordost.streetcomplete.quests.AnswerItem
import java.time.LocalDate

class MarkCompletedConstructionForm : AbstractQuestAnswerFragment<CompletedConstructionAnswer>() {

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(StateAnswer(false)) },
        AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(StateAnswer(true)) }
    )

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_construction_completed_at_known_date) { setFinishDate() }
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
}
