package de.westnordost.streetcomplete.quests.construction

import android.app.DatePickerDialog
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.Answers
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.util.ktx.systemTimeNow
import de.westnordost.streetcomplete.util.ktx.toInstant
import de.westnordost.streetcomplete.util.ktx.toLocalDate
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.stringResource

class MarkCompletedConstructionForm : AbstractOsmQuestForm<CompletedConstructionAnswer>() {

    @Composable
    override fun Content() {
        QuestForm(
            answers = Answers(
                Answer(stringResource(Res.string.quest_generic_hasFeature_no)) { applyAnswer(StateAnswer(false)) },
                Answer(stringResource(Res.string.quest_generic_hasFeature_yes)) { applyAnswer(StateAnswer(true)) }
            ),
            otherAnswers = listOf(
                Answer(stringResource(Res.string.quest_construction_completed_at_known_date)) { setFinishDate() }
            )
        )
    }

    private fun setFinishDate() {
        val tomorrow = systemTimeNow().toLocalDate().plus(1, DateTimeUnit.DAY)
        val dpd = DatePickerDialog(requireContext(), { _, year, month, day ->
            applyAnswer(OpeningDateAnswer(LocalDate(year, month + 1, day)))
        }, tomorrow.year, tomorrow.month.number - 1, tomorrow.day)
        dpd.setTitle(resources.getString(R.string.quest_construction_completion_date_title))
        dpd.datePicker.minDate = tomorrow.toInstant().toEpochMilliseconds()
        dpd.show()
    }
}
