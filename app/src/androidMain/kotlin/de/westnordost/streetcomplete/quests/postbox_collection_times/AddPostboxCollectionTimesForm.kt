package de.westnordost.streetcomplete.quests.postbox_collection_times

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.intl.Locale
import de.westnordost.osm_opening_hours.parser.toOpeningHoursOrNull
import de.westnordost.streetcomplete.osm.opening_hours.HierarchicOpeningHours
import de.westnordost.streetcomplete.osm.opening_hours.toHierarchicOpeningHours
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.QuestConfirmationDialog
import de.westnordost.streetcomplete.ui.common.opening_hours.OpeningHoursTable
import de.westnordost.streetcomplete.ui.common.opening_hours.TimeMode
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.Answers
import de.westnordost.streetcomplete.ui.common.quest.Confirm
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.jetbrains.compose.resources.stringResource

class AddPostboxCollectionTimesForm : AbstractOsmQuestForm<CollectionTimesAnswer>() {

    @Composable
    override fun Content() {
        val oh = remember { element.tags["collection_times"]?.toOpeningHoursOrNull(lenient = true) }
        val originalOpeningHours = remember { oh?.toHierarchicOpeningHours(allowTimePoints = true) }
        var openingHours by rememberSerializable {
            mutableStateOf(originalOpeningHours ?: HierarchicOpeningHours())
        }
        var isDisplayingPrevious by rememberSaveable {
            mutableStateOf(originalOpeningHours != null)
        }
        var timeMode by rememberSerializable {
            mutableStateOf(if (oh?.containsTimeSpans() == true) TimeMode.Spans else TimeMode.Points)
        }

        var confirmNoSign by remember { mutableStateOf(false) }

        QuestForm(
            answers =
                if (isDisplayingPrevious) {
                    Answers(
                        Answer(stringResource(Res.string.quest_generic_hasFeature_no)) {
                            isDisplayingPrevious = false
                        },
                        Answer(stringResource(Res.string.quest_generic_hasFeature_yes)) {
                            applyAnswer(CollectionTimes(originalOpeningHours!!))
                        }
                    )
                } else {
                    Confirm(
                        isComplete = openingHours.isComplete(),
                        hasChanges = openingHours.monthsList.isNotEmpty(),
                        onClick = { applyAnswer(CollectionTimes(openingHours)) }
                    )
                },
            otherAnswers = listOf(
                Answer(stringResource(Res.string.quest_collectionTimes_answer_no_times_specified)) {
                    confirmNoSign = true
                },
                when (timeMode) {
                    TimeMode.Points -> {
                        Answer(stringResource(Res.string.quest_collectionTimes_answer_time_spans)) {
                            timeMode = TimeMode.Spans
                        }
                    }
                    TimeMode.Spans -> {
                        Answer(stringResource(Res.string.quest_collectionTimes_answer_time_points)) {
                            timeMode = TimeMode.Points
                        }
                    }
                }
            )
        ) {
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
        if (confirmNoSign) {
            QuestConfirmationDialog(
                onDismissRequest = { confirmNoSign = false },
                onConfirmed = { applyAnswer(NoCollectionTimesSign) },
                titleText = stringResource(Res.string.quest_generic_confirmation_title)
            )
        }
    }
}
