package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import de.westnordost.osm_opening_hours.model.ClockTime
import de.westnordost.osm_opening_hours.model.Time
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_openingHours_chooseTimeTitle
import de.westnordost.streetcomplete.ui.common.TimePicker
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmationDialog
import de.westnordost.streetcomplete.ui.common.rememberTimePickerState
import de.westnordost.streetcomplete.util.locale.TimeFormatElements
import org.jetbrains.compose.resources.stringResource

/** Dialog in which to select a time */
@Composable
fun TimeSelectDialog(
    onDismissRequest: () -> Unit,
    onSelect: (time: Time) -> Unit,
    initialTime: Time,
    modifier: Modifier = Modifier,
    locale: Locale = Locale.current,
) {
    val initialTime = (initialTime as? ClockTime) ?: throw UnsupportedOperationException()

    val timeFormatElements = remember(locale) { TimeFormatElements.of(locale) }
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinutes = initialTime.minutes,
        is12Hour = timeFormatElements.clock12 != null
    )

    ConfirmationDialog(
        onDismissRequest = onDismissRequest,
        onConfirmed = {
            onSelect(ClockTime(timePickerState.hour, timePickerState.minute))
        },
        modifier = modifier,
        title = { Text(stringResource(Res.string.quest_openingHours_chooseTimeTitle)) },
        text = {
            TimePicker(
                state = timePickerState,
                timeFormatElements = timeFormatElements,
            )
        },
    )
}
