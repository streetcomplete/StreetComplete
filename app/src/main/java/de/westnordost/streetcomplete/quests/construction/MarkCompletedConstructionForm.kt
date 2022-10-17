package de.westnordost.streetcomplete.quests.construction

import android.app.DatePickerDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.util.ktx.systemTimeNow
import de.westnordost.streetcomplete.util.ktx.toInstant
import de.westnordost.streetcomplete.util.ktx.toLocalDate
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

class MarkCompletedConstructionForm : AbstractOsmQuestForm<CompletedConstructionAnswer>() {

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(StateAnswer(false)) },
        AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(StateAnswer(true)) }
    )

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_construction_completed_at_known_date) { setFinishDate() }
    )

    private fun setFinishDate() {
        val tomorrow = systemTimeNow().toLocalDate().plus(1, DateTimeUnit.DAY)
        val dpd = DatePickerDialog(requireContext(), { _, year, month, day ->
            applyAnswer(OpeningDateAnswer(LocalDate(year, month + 1, day)))
        }, tomorrow.year, tomorrow.monthNumber - 1, tomorrow.dayOfMonth)
        dpd.setTitle(resources.getString(R.string.quest_construction_completion_date_title))
        dpd.datePicker.minDate = tomorrow.toInstant().toEpochMilliseconds()
        dpd.show()
    }
}
