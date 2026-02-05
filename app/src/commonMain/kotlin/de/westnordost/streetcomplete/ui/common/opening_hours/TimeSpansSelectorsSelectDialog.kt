package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Checkbox
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import de.westnordost.osm_opening_hours.model.ClockTime
import de.westnordost.osm_opening_hours.model.ExtendedClockTime
import de.westnordost.osm_opening_hours.model.ExtendedTime
import de.westnordost.osm_opening_hours.model.StartingAtTime
import de.westnordost.osm_opening_hours.model.TimeSpan
import de.westnordost.osm_opening_hours.model.TimeSpansSelector
import de.westnordost.osm_opening_hours.model.VariableTime
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.TimePicker
import de.westnordost.streetcomplete.ui.common.dialogs.ScrollableAlertDialog
import de.westnordost.streetcomplete.ui.common.rememberTimePickerState
import de.westnordost.streetcomplete.ui.theme.largeInput
import de.westnordost.streetcomplete.util.locale.TimeFormatElements
import org.jetbrains.compose.resources.stringResource

/** Dialog in which the user can select a (clock) time span */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TimeSpansSelectorSelectDialog(
    onDismissRequest: () -> Unit,
    initialTimeSpansSelector: TimeSpansSelector?,
    onSelect: (TimeSpansSelector) -> Unit,
    modifier: Modifier = Modifier,
    locale: Locale = Locale.current,
) {
    var step by remember { mutableIntStateOf(0) }

    val timeFormatElements = remember(locale) { TimeFormatElements.of(locale) }
    val startTimePickerState = rememberTimePickerState(
        initialHour = initialTimeSpansSelector?.start?.hour ?: 0,
        initialMinutes = initialTimeSpansSelector?.start?.minutes ?: 0,
        is12Hour = timeFormatElements.clock12 != null,
        allowAfterMidnight = false,
    )
    val endTimePickerState = rememberTimePickerState(
        initialHour = initialTimeSpansSelector?.end?.hour ?: 0,
        initialMinutes = initialTimeSpansSelector?.end?.minutes ?: 0,
        is12Hour = timeFormatElements.clock12 != null,
        allowAfterMidnight = true,
    )
    var startOpenEnd by remember { mutableStateOf(initialTimeSpansSelector is StartingAtTime) }
    var endOpenEnd by remember { mutableStateOf((initialTimeSpansSelector as? TimeSpan)?.openEnd ?: false) }

    fun toggleOpenEnd(value: Boolean) {
        if (step == 0) startOpenEnd = value
        else endOpenEnd = value
    }

    ScrollableAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = {
            Text(stringResource(
                if (step == 0) Res.string.quest_openingHours_start_time
                else Res.string.quest_openingHours_end_time
            ))
        },
        content = {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
                Column(Modifier.padding(horizontal = 24.dp).fillMaxWidth()) {
                    val timePickerState = if (step == 0) startTimePickerState else endTimePickerState
                    val openEnd = if (step == 0) startOpenEnd else endOpenEnd

                    ProvideTextStyle(MaterialTheme.typography.largeInput) {
                        TimePicker(
                            state = timePickerState,
                            timeFormatElements = timeFormatElements,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            visibleAdjacentItems = 2,
                        )
                    }

                    Divider()

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .toggleable(openEnd) { toggleOpenEnd(it) }
                    ) {
                        Checkbox(
                            checked = openEnd,
                            onCheckedChange = { toggleOpenEnd(it) },
                        )
                        Text(
                            text = stringResource(Res.string.quest_openingHours_no_fixed_end),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        buttons = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.cancel))
            }
            if (step == 0) {
                if (startOpenEnd) {
                    TextButton(onClick = {
                        val time = ClockTime(startTimePickerState.hour, startTimePickerState.minute)
                        onSelect(StartingAtTime(time))
                        onDismissRequest()
                    }) {
                        Text(stringResource(Res.string.ok))
                    }
                } else {
                    TextButton(onClick = { step = 1 }) {
                        Text(stringResource(Res.string.next))
                    }
                }
            } else {
                TextButton(onClick = { step = 0 }) {
                    Text(stringResource(Res.string.action_back))
                }
                TextButton(onClick = {
                    val start = ClockTime(startTimePickerState.hour, startTimePickerState.minute)
                    val end = ExtendedClockTime(endTimePickerState.hour, endTimePickerState.minute)
                    onSelect(TimeSpan(start, end, endOpenEnd))
                    onDismissRequest()
                }) {
                    Text(stringResource(Res.string.ok))
                }
            }
        },
    )
}

private val TimeSpansSelector.start get() = when (this) {
    is StartingAtTime -> start
    is TimeSpan -> start
}

private val TimeSpansSelector.end get() = when (this) {
    is StartingAtTime -> start
    is TimeSpan -> end
}

private val ExtendedTime.hour get() = when (this) {
    is ClockTime -> hour
    is ExtendedClockTime -> hour
    is VariableTime -> throw UnsupportedOperationException()
}

private val ExtendedTime.minutes get() = when (this) {
    is ClockTime -> minutes
    is ExtendedClockTime -> minutes
    is VariableTime -> throw UnsupportedOperationException()
}
