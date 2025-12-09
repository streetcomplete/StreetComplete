package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import de.westnordost.osm_opening_hours.model.StartingAtTime
import de.westnordost.osm_opening_hours.model.Time
import de.westnordost.osm_opening_hours.model.TimeSpan
import de.westnordost.osm_opening_hours.model.TimeSpansSelector
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.util.locale.DateTimeFormatStyle
import de.westnordost.streetcomplete.util.locale.LocalTimeFormatter
import org.jetbrains.compose.resources.stringResource

/** A text that shows a localized time span an lets you select another time span.
 *  E.g. "08:00-18:00" but also "from 12:00" or "16:00-22:00 until late" (i.e. open end) */
@Composable
fun TimeSpansSelectorTexts(
    time: TimeSpansSelector,
    onChangeTime: (TimeSpansSelector) -> Unit,
    modifier: Modifier = Modifier,
    locale: Locale = Locale.current,
    enabled: Boolean = true,
) {
    val timeFormatter = remember(locale) {
        LocalTimeFormatter(locale = locale, style = DateTimeFormatStyle.Short)
    }
    var showStartTimeDialog by remember { mutableStateOf(false) }
    var showEndTimeDialog by remember { mutableStateOf(false) }
    val startTime = when (time) {
        is StartingAtTime -> time.start
        is TimeSpan -> time.start
    }
    val endTime = when (time) {
        is StartingAtTime -> null
        is TimeSpan -> time.end
    }
    val isOpenEnded = when (time) {
        is StartingAtTime -> true
        is TimeSpan -> time.openEnd
    }

    when (time) {
        is StartingAtTime -> {
            val startTimeText = startTime.toLocalizedString(timeFormatter)
            Text(
                text = stringResource(Res.string.opening_hours_from, startTimeText),
                modifier = modifier.clickable(enabled) { showStartTimeDialog = true }
            )
        }
        is TimeSpan -> {
            val startTimeText = startTime.toLocalizedString(timeFormatter)
            val endTimeText = endTime?.toLocalizedString(timeFormatter).orEmpty()
            val endText = if (isOpenEnded) {
                stringResource(Res.string.opening_hours_until_late, endTimeText)
            } else {
                endTimeText
            }
            FlowRow(modifier = modifier) {
                Text(
                    text = startTimeText,
                    modifier = Modifier.clickable(enabled) { showStartTimeDialog = true }
                )
                Text(rangeSeparator(locale).toString())
                Text(
                    text = endText,
                    modifier = Modifier.clickable(enabled) { showEndTimeDialog = true }
                )
            }
        }
    }

    if (showStartTimeDialog) {
        TimeSpanPointSelectDialog(
            onDismissRequest = { showStartTimeDialog = false },
            onSelect = { newStartTime: Time, openEnd: Boolean ->
                if (openEnd) {
                    onChangeTime(StartingAtTime(newStartTime))
                } else {
                    onChangeTime(TimeSpan(newStartTime, endTime ?: newStartTime))
                }
            },
            initialTime = startTime,
            initialIsOpenEnd = isOpenEnded,
            locale = locale,
            title = { Text(stringResource(Res.string.quest_openingHours_start_time)) },
        )
    }
    if (showEndTimeDialog) {
        TimeSpanPointSelectDialog(
            onDismissRequest = { showStartTimeDialog = false },
            onSelect = { newEndTime: Time, openEnd: Boolean ->
                onChangeTime(TimeSpan(startTime, newEndTime, openEnd))
            },
            initialTime = endTime ?: startTime,
            initialIsOpenEnd = isOpenEnded,
            locale = locale,
            title = { Text(stringResource(Res.string.quest_openingHours_end_time)) },
        )
    }
}
