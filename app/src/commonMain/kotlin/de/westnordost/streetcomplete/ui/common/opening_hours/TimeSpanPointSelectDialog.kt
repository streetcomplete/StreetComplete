package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Checkbox
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import de.westnordost.osm_opening_hours.model.ClockTime
import de.westnordost.osm_opening_hours.model.ExtendedClockTime
import de.westnordost.osm_opening_hours.model.ExtendedTime
import de.westnordost.osm_opening_hours.model.Time
import de.westnordost.osm_opening_hours.model.VariableTime
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.opening_hours_no_fixed_end
import de.westnordost.streetcomplete.ui.common.TimePicker
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmationDialog
import de.westnordost.streetcomplete.ui.common.rememberTimePickerState
import de.westnordost.streetcomplete.util.locale.TimeFormatElements
import org.jetbrains.compose.resources.stringResource

/** Dialog in which to select the start or end of a time range. The main difference to
 *  ClockTimeSelectDialog is that both the start or end of a time range can be specified to be
 *  open ended. */
@Composable
fun TimeSpanPointSelectDialog(
    onDismissRequest: () -> Unit,
    onSelect: (time: Time, openEnd: Boolean) -> Unit,
    initialTime: ExtendedTime,
    initialIsOpenEnd: Boolean,
    modifier: Modifier = Modifier,
    locale: Locale = Locale.current,
    title: (@Composable () -> Unit)? = null,
) {
    val initialHour = when (initialTime) {
        is ExtendedClockTime -> initialTime.hour
        is ClockTime -> initialTime.hour
        is VariableTime -> throw UnsupportedOperationException()
    }
    val initialMinutes = when (initialTime) {
        is ExtendedClockTime -> initialTime.minutes
        is ClockTime -> initialTime.minutes
        is VariableTime -> throw UnsupportedOperationException()
    }
    val timeFormatElements = remember(locale) { TimeFormatElements.of(locale) }
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinutes,
        is12Hour = timeFormatElements.clock12 != null
    )
    var openEnd by remember(initialIsOpenEnd) { mutableStateOf(initialIsOpenEnd) }

    ConfirmationDialog(
        onDismissRequest = onDismissRequest,
        onConfirmed = {
            onSelect(ClockTime(timePickerState.selectedHour, timePickerState.selectedMinute), openEnd)
        },
        modifier = modifier,
        title = title,
        text = {
            Column {
                TimePicker(
                    state = timePickerState,
                    timeFormatElements = timeFormatElements,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Divider()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.toggleable(openEnd) { openEnd = it }
                ) {
                    Checkbox(
                        checked = openEnd,
                        onCheckedChange = { openEnd = it },
                    )
                    Text(stringResource(Res.string.opening_hours_no_fixed_end))
                }
            }
        },
    )
}
