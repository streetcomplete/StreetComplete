package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.util.locale.TimeFormatElements

@Composable
fun TimePicker(
    modifier: Modifier = Modifier,
    locale: Locale? = null,
    selectedHours: Int = 0,
    selectedMinutes: Int = 0,
) {
    val elements = remember(locale) { TimeFormatElements.of(locale) }
    val isClock12 = elements.clock12 != null

    val selectableHours = remember(isClock12) {
        if (elements.clock12 != null) (1..12).toList()
        else (0..24).toList()
    }
    val selectableMinutes = remember { (0..59).toList() }

    val selectedHoursIndex = remember(isClock12, selectedHours) {
        var displayHours = selectedHours
        if (isClock12) {
            displayHours = selectedHours % 12
            if (displayHours == 0) displayHours = 12
        }
        selectableHours.indexOf(displayHours)
    }
    val selectedAmPmIndex = remember(isClock12, selectedHours) {
        if (selectedHours <= 12) 0 else 1
    }

    val hoursState = rememberWheelPickerState(selectedHoursIndex)
    val minutesState = rememberWheelPickerState(selectedMinutes)
    val amPmState = rememberWheelPickerState(selectedAmPmIndex)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier,
    ) {
        if (elements.before.isNotEmpty()) {
            Text(elements.before)
        }
        if (elements.clock12 != null && elements.clock12.isInFront) {
            WheelPicker(
                items = listOf(elements.clock12.am, elements.clock12.pm),
                state = amPmState,
                key = { it },
                content = { Text(it) }
            )
        }
        WheelPicker(
            items = selectableHours,
            state = hoursState,
            key = { it },
            content = { Text(it.toString().padStart(2, elements.zero)) }
        )
        Text(elements.hourSeparator)
        WheelPicker(
            items = selectableMinutes,
            state = minutesState,
            key = { it },
            content = { Text(it.toString().padStart(2, elements.zero)) }
        )
        if (elements.clock12 != null && !elements.clock12.isInFront) {
            WheelPicker(
                items = listOf(elements.clock12.am, elements.clock12.pm),
                state = amPmState,
                key = { it },
                content = { Text(it) }
            )
        }
        if (elements.after.isNotEmpty()) {
            Text(elements.after)
        }
    }
}
