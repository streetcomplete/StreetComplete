package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.util.locale.TimeFormatElements
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun rememberTimePickerState(
    initialHour: Int = 0,
    initialMinutes: Int = 0,
    is12Hour: Boolean = false,
    allowAfterMidnight: Boolean = false,
) = remember {
    TimePickerState(initialHour, initialMinutes, is12Hour, allowAfterMidnight)
}

class TimePickerState(
    initialHour: Int = 0,
    initialMinute: Int = 0,
    val is12Hour: Boolean = false,
    val allowAfterMidnight: Boolean = false,
) {
    internal val hoursPickerState: WheelPickerState
    internal val minutesPickerState: WheelPickerState
    internal val amPmPickerState: WheelPickerState

    val selectableHours: List<Int>
    val selectableMinutes: List<Int>

    val hour: Int by derivedStateOf {
        var selectedHours = selectableHours[hoursPickerState.selectedItemIndex]
        if (is12Hour) {
            if (selectedHours == 12) selectedHours = 0
            if (amPmPickerState.selectedItemIndex == 1) selectedHours = selectedHours + 12
        }
        selectedHours
    }

    val minute: Int by derivedStateOf {
        selectableMinutes[minutesPickerState.selectedItemIndex]
    }

    init {

        selectableHours = (
            if (is12Hour) (1..12)
            else if (allowAfterMidnight) (0..24)
            else (0..23)
        ).toList()
        selectableMinutes = ((0..45 step 15) + (0..59)).toList()

        var displayHours = initialHour
        if (is12Hour) {
            displayHours = initialHour % 12
            if (displayHours == 0) displayHours = 12
        }
        val selectedHoursIndex = selectableHours.indexOf(displayHours)
        val selectedMinutesIndex = selectableMinutes.indexOf(initialMinute)
        val selectedAmPmIndex = if (initialHour <= 12) 0 else 1

        hoursPickerState = WheelPickerState(selectedHoursIndex)
        minutesPickerState = WheelPickerState(selectedMinutesIndex)
        amPmPickerState = WheelPickerState(selectedAmPmIndex)
    }
}


@Composable
fun TimePicker(
    state: TimePickerState,
    timeFormatElements: TimeFormatElements,
    modifier: Modifier = Modifier,
    visibleAdjacentItems: Int = 1,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier,
    ) {
        if (timeFormatElements.before.isNotEmpty()) {
            Text(timeFormatElements.before)
        }
        if (timeFormatElements.clock12 != null && timeFormatElements.clock12.isInFront) {
            WheelPicker(
                items = listOf(timeFormatElements.clock12.am, timeFormatElements.clock12.pm),
                state = state.amPmPickerState,
                content = { Text(it) },
                visibleAdjacentItems = visibleAdjacentItems
            )
        }
        WheelPicker(
            items = state.selectableHours,
            state = state.hoursPickerState,
            content = { Text(it.toString().padStart(2, ' ')) },
            visibleAdjacentItems = visibleAdjacentItems
        )
        Text(timeFormatElements.hourSeparator)
        WheelPicker(
            items = state.selectableMinutes,
            state = state.minutesPickerState,
            content = { Text(it.toString().padStart(2, timeFormatElements.zero)) },
            visibleAdjacentItems = visibleAdjacentItems
        )
        if (timeFormatElements.clock12 != null && !timeFormatElements.clock12.isInFront) {
            WheelPicker(
                items = listOf(timeFormatElements.clock12.am, timeFormatElements.clock12.pm),
                state = state.amPmPickerState,
                content = { Text(it) },
                visibleAdjacentItems = visibleAdjacentItems
            )
        }
        if (timeFormatElements.after.isNotEmpty()) {
            Text(timeFormatElements.after)
        }
    }
}

@Composable @Preview
fun TimePickerPreview() {
    val elements = TimeFormatElements.of(Locale("de"))
    val state = rememberTimePickerState(12, 30, elements.clock12 != null)
    Column {
        TimePicker(state, elements)
        Text(state.hour.toString() + " " + state.minute.toString())
    }
}
