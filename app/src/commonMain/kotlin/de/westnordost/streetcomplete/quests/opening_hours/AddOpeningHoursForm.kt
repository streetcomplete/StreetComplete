package de.westnordost.streetcomplete.quests.opening_hours

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.intl.Locale
import de.westnordost.osm_opening_hours.model.Month
import de.westnordost.osm_opening_hours.model.MonthRange
import de.westnordost.osm_opening_hours.parser.toOpeningHoursOrNull
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAnswer
import de.westnordost.streetcomplete.osm.opening_hours.HierarchicOpeningHours
import de.westnordost.streetcomplete.osm.opening_hours.toHierarchicOpeningHours
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.QuestConfirmationDialog
import de.westnordost.streetcomplete.ui.common.opening_hours.OpeningHoursCommentDialog
import de.westnordost.streetcomplete.ui.common.opening_hours.OpeningHoursTable
import de.westnordost.streetcomplete.ui.common.opening_hours.TimeMode
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddOpeningHoursForm(
    onAnswer: (QuestAnswer<OpeningHoursAnswer>) -> Unit,
    element: Element,
    countryInfo: CountryInfo,
) {
    val originalOpeningHours = remember {
        element.tags["opening_hours"]
            ?.toOpeningHoursOrNull(lenient = true)
            ?.toHierarchicOpeningHours()
    }
    var openingHours by rememberSerializable {
        mutableStateOf(originalOpeningHours ?: HierarchicOpeningHours())
    }
    var isDisplayingPrevious by rememberSaveable {
        mutableStateOf(originalOpeningHours != null)
    }

    var showCommentDialog by remember { mutableStateOf(false) }
    var confirmNoSign by remember { mutableStateOf(false) }
    var confirm24_7 by remember { mutableStateOf(false) }

    val openingHoursTable: @Composable () -> Unit = {
        OpeningHoursTable(
            openingHours = openingHours,
            onChange = { openingHours = it },
            timeMode = TimeMode.Spans,
            countryInfo = countryInfo,
            addButtonContent = { Text(stringResource(Res.string.quest_openingHours_add_times)) },
            locale = countryInfo.userPreferredLocale,
            userLocale = Locale.current,
            enabled = !isDisplayingPrevious,
        )
    }

    if (isDisplayingPrevious) {
        QuestForm(
            title = stringResource(Res.string.quest_openingHours_resurvey_title),
            answers = listOf(
                AnswerItem(stringResource(Res.string.quest_generic_hasFeature_no)) {
                    isDisplayingPrevious = false
                },
                AnswerItem(stringResource(Res.string.quest_generic_hasFeature_yes)) {
                    onAnswer(Answer(RegularOpeningHours(originalOpeningHours!!)))
                }
            ),
            onAnswer = onAnswer,
            otherAnswers = listOf(
                AnswerItem(stringResource(Res.string.quest_openingHours_no_sign)) { confirmNoSign = true },
            ),
            content = { openingHoursTable() }
        )
    } else {
        QuestForm(
            isComplete = openingHours.isComplete(),
            hasChanges = openingHours.monthsList.isNotEmpty(),
            onClickOk = { onAnswer(Answer(RegularOpeningHours(openingHours))) },
            onAnswer = onAnswer,
            otherAnswers = listOf(
                AnswerItem(stringResource(Res.string.quest_openingHours_no_sign)) { confirmNoSign = true },
                AnswerItem(stringResource(Res.string.quest_openingHours_answer_no_regular_opening_hours)) { showCommentDialog = true },
                AnswerItem(stringResource(Res.string.quest_openingHours_answer_247)) { confirm24_7 = true },
                AnswerItem(stringResource(Res.string.quest_openingHours_answer_seasonal_opening_hours)) {
                    isDisplayingPrevious = false
                    val allMonths = listOf(MonthRange(Month.January, Month.December))
                    openingHours = HierarchicOpeningHours(
                        openingHours.monthsList.map { months ->
                            if (months.selectors.isEmpty()) {
                                months.copy(selectors = allMonths)
                            } else {
                                months
                            }
                        }
                    )
                }
            ),
            content = { openingHoursTable() }
        )
    }

    if (showCommentDialog) {
        OpeningHoursCommentDialog(
            onDismissRequest = { showCommentDialog = false },
            onConfirm = { onAnswer(Answer(DescribeOpeningHours(it))) }
        )
    }
    if (confirmNoSign) {
        QuestConfirmationDialog(
            onDismissRequest = { confirmNoSign = false },
            onConfirmed = { onAnswer(Answer(NoOpeningHoursSign)) },
            titleText = stringResource(Res.string.quest_generic_confirmation_title)
        )
    }
    if (confirm24_7) {
        QuestConfirmationDialog(
            onDismissRequest = { confirm24_7 = false },
            onConfirmed = { onAnswer(Answer(AlwaysOpen)) },
            titleText = stringResource(Res.string.quest_openingHours_24_7_confirmation)
        )
    }
}
