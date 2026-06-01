package de.westnordost.streetcomplete.quests.construction

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.common.DateSelectDialog
import de.westnordost.streetcomplete.ui.common.quest.LocalQuestType
import de.westnordost.streetcomplete.util.ktx.systemTimeNow
import de.westnordost.streetcomplete.util.ktx.toLocalDate
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.stringResource

@Composable
fun MarkCompletedConstructionForm(
    on: (QuestAction<CompletedConstructionAnswer>) -> Unit,
    title: String = stringResource(LocalQuestType.current!!.title),
) {
    var showDateSelectDialog by remember { mutableStateOf(false) }

    QuestForm(
        answers = listOf(
            AnswerItem(stringResource(Res.string.quest_generic_hasFeature_no)) { on(Answer(ConstructionState(false))) },
            AnswerItem(stringResource(Res.string.quest_generic_hasFeature_yes)) { on(Answer(ConstructionState(true))) }
        ),
        on = on,
        title = title,
        otherAnswers = listOf(
            AnswerItem(stringResource(Res.string.quest_construction_completed_at_known_date)) { showDateSelectDialog = true }
        )
    )

    if (showDateSelectDialog) {
        val tomorrow = remember { systemTimeNow().toLocalDate().plus(1, DateTimeUnit.DAY) }
        DateSelectDialog(
            onDismissRequest = { showDateSelectDialog = false },
            onSelect = { on(Answer(OpeningDate(it))) },
            initialDate = tomorrow,
            years = tomorrow.year..(tomorrow.year + 30),
            title = { Text(stringResource(Res.string.quest_construction_completion_date_title)) }
        )
    }
}
