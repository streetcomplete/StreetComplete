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
import de.westnordost.streetcomplete.osm.opening_hours.HierarchicOpeningHours
import de.westnordost.streetcomplete.osm.opening_hours.toHierarchicOpeningHours
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.QuestConfirmationDialog
import de.westnordost.streetcomplete.ui.common.opening_hours.OpeningHoursCommentDialog
import de.westnordost.streetcomplete.ui.common.opening_hours.OpeningHoursTable
import de.westnordost.streetcomplete.ui.common.opening_hours.TimeMode
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.Answers
import de.westnordost.streetcomplete.ui.common.quest.Confirm
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.jetbrains.compose.resources.stringResource

class AddOpeningHoursForm : AbstractOsmQuestForm<OpeningHoursAnswer>() {

    @Composable
    override fun Content() {
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

        QuestForm(
            answers =
                if (isDisplayingPrevious) {
                    Answers(
                        Answer(stringResource(Res.string.quest_generic_hasFeature_no)) {
                            isDisplayingPrevious = false
                        },
                        Answer(stringResource(Res.string.quest_generic_hasFeature_yes)) {
                            applyAnswer(RegularOpeningHours(originalOpeningHours!!))
                        }
                    )
                } else {
                    Confirm(
                        isComplete = openingHours.isComplete(),
                        hasChanges = openingHours.monthsList.isNotEmpty(),
                        onClick = { applyAnswer(RegularOpeningHours(openingHours)) }
                    )
                },
            otherAnswers = listOf(
                Answer(stringResource(Res.string.quest_openingHours_no_sign)) { confirmNoSign = true },
                Answer(stringResource(Res.string.quest_openingHours_answer_no_regular_opening_hours)) { showCommentDialog = true },
                Answer(stringResource(Res.string.quest_openingHours_answer_247)) { confirm24_7 = true },
                Answer(stringResource(Res.string.quest_openingHours_answer_seasonal_opening_hours)) {
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
            )
        ) {
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

        if (showCommentDialog) {
            OpeningHoursCommentDialog(
                onDismissRequest = { showCommentDialog = false },
                onConfirm = { applyAnswer(DescribeOpeningHours(it)) }
            )
        }
        if (confirmNoSign) {
            QuestConfirmationDialog(
                onDismissRequest = { confirmNoSign = false },
                onConfirmed = { applyAnswer(NoOpeningHoursSign) },
                titleText = stringResource(Res.string.quest_generic_confirmation_title)
            )
        }
        if (confirm24_7) {
            QuestConfirmationDialog(
                onDismissRequest = { confirm24_7 = false },
                onConfirmed = { applyAnswer(AlwaysOpen) },
                titleText = stringResource(Res.string.quest_openingHours_24_7_confirmation)
            )
        }
    }
}
