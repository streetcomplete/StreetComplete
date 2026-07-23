package de.westnordost.streetcomplete.quests.postbox_collection_times

import androidx.compose.animation.AnimatedContent
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.intl.Locale
import de.westnordost.osm_opening_hours.parser.toOpeningHoursOrNull
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.osm.opening_hours.HierarchicOpeningHours
import de.westnordost.streetcomplete.osm.opening_hours.toHierarchicOpeningHours
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.AreYouSureDialog
import de.westnordost.streetcomplete.ui.common.opening_hours.OpeningHoursTable
import de.westnordost.streetcomplete.ui.common.opening_hours.TimeMode
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.SlideStartHorizontally
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddPostboxCollectionTimesForm(
    on: (QuestAction<CollectionTimesAnswer>) -> Unit,
    element: Element,
    countryInfo: CountryInfo,
) {
    val oh = remember(element) { element.tags["collection_times"]?.toOpeningHoursOrNull(lenient = true) }
    val originalOpeningHours = remember(oh) { oh?.toHierarchicOpeningHours(allowTimePoints = true) }
    var openingHours by rememberSerializable(originalOpeningHours) {
        mutableStateOf(originalOpeningHours ?: HierarchicOpeningHours())
    }
    var isDisplayingPrevious by rememberSaveable(originalOpeningHours) {
        mutableStateOf(originalOpeningHours != null)
    }
    var timeMode by rememberSerializable(oh) {
        mutableStateOf(if (oh?.containsTimeSpans() == true) TimeMode.Spans else TimeMode.Points)
    }

    var confirmNoSign by remember { mutableStateOf(false) }

    val openingHoursTable: @Composable () -> Unit = {
        OpeningHoursTable(
            openingHours = openingHours,
            onChange = { openingHours = it },
            timeMode = timeMode,
            countryInfo = countryInfo,
            addButtonContent = { Text(stringResource(Res.string.quest_collectionTimes_add_times)) },
            locale = countryInfo.userPreferredLocale,
            userLocale = Locale.current,
            enabled = !isDisplayingPrevious,
        )
    }

    AnimatedContent(
        targetState = isDisplayingPrevious,
        transitionSpec = SlideStartHorizontally
    ) { isDisplayingPrevious2 ->
        if (isDisplayingPrevious2) {
            QuestForm(
                on = on,
                answers = listOf(
                    AnswerItem(stringResource(Res.string.quest_generic_hasFeature_no)) {
                        isDisplayingPrevious = false
                    },
                    AnswerItem(stringResource(Res.string.quest_generic_hasFeature_yes)) {
                        on(Answer(CollectionTimes(originalOpeningHours!!)))
                    }
                ),
                title = stringResource(Res.string.quest_postboxCollectionTimes_resurvey_title),
                otherAnswers = { listOf(
                    AnswerItem(stringResource(Res.string.quest_collectionTimes_answer_no_times_specified)) {
                        confirmNoSign = true
                    },
                ) },
                content = { openingHoursTable() }
            )
        } else {
            QuestForm(
                on = on,
                isComplete = openingHours.isComplete(),
                onClickOk = { on(Answer(CollectionTimes(openingHours))) },
                hasChanges = openingHours.monthsList.isNotEmpty(),
                otherAnswers = {
                    val switchTimeModeAnswer = when (timeMode) {
                        TimeMode.Points -> {
                            AnswerItem(stringResource(Res.string.quest_collectionTimes_answer_time_spans)) {
                                timeMode = TimeMode.Spans
                            }
                        }
                        TimeMode.Spans -> {
                            AnswerItem(stringResource(Res.string.quest_collectionTimes_answer_time_points)) {
                                timeMode = TimeMode.Points
                            }
                        }
                    }

                    listOf(
                        AnswerItem(stringResource(Res.string.quest_collectionTimes_answer_no_times_specified)) {
                            confirmNoSign = true
                        },
                        switchTimeModeAnswer
                    )
                 },
                content = { openingHoursTable() }
            )
        }
    }

    if (confirmNoSign) {
        AreYouSureDialog(
            onDismissRequest = { confirmNoSign = false },
            onConfirmed = { on(Answer(CollectionTimesAnswer.NoSign)) },
            titleText = stringResource(Res.string.quest_generic_confirmation_title)
        )
    }
}
