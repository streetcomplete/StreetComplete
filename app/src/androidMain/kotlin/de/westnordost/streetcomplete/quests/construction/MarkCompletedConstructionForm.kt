package de.westnordost.streetcomplete.quests.construction

import android.app.DatePickerDialog
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.Answers
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.common.DateSelectDialog
import de.westnordost.streetcomplete.util.ktx.systemTimeNow
import de.westnordost.streetcomplete.util.ktx.toInstant
import de.westnordost.streetcomplete.util.ktx.toLocalDate
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.stringResource

@Composable
fun MarkCompletedConstructionForm(
    onAnswer: (CompletedConstructionAnswer) -> Unit,
) {
    var showDateSelectDialog by remember { mutableStateOf(false) }

    QuestForm(
        answers = Answers(
            Answer(stringResource(Res.string.quest_generic_hasFeature_no)) { onAnswer(ConstructionState(false)) },
            Answer(stringResource(Res.string.quest_generic_hasFeature_yes)) { onAnswer(ConstructionState(true)) }
        ),
        otherAnswers = listOf(
            Answer(stringResource(Res.string.quest_construction_completed_at_known_date)) { showDateSelectDialog = true }
        )
    )

    if (showDateSelectDialog) {
        val tomorrow = remember { systemTimeNow().toLocalDate().plus(1, DateTimeUnit.DAY) }
        DateSelectDialog(
            onDismissRequest = { showDateSelectDialog = false },
            onSelect = { onAnswer(OpeningDate(it)) },
            initialDate = tomorrow,
            years = tomorrow.year..(tomorrow.year + 30),
            title = { Text(stringResource(Res.string.quest_construction_completion_date_title)) }
        )
    }
}
