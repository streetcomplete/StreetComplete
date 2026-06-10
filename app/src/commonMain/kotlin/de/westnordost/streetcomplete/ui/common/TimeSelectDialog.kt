package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.ScrollableAlertDialog
import de.westnordost.streetcomplete.ui.theme.largeInput
import de.westnordost.streetcomplete.util.locale.TimeFormatElements
import org.jetbrains.compose.resources.stringResource

/** Dialog in which to select a time */
@Composable
fun TimeSelectDialog(
    onDismissRequest: () -> Unit,
    onSelect: (hour: Int, minutes: Int) -> Unit,
    modifier: Modifier = Modifier,
    initialHour: Int = 0,
    initialMinutes: Int = 0,
    locale: Locale = Locale.current,
) {
    val timeFormatElements = remember(locale) { TimeFormatElements.of(locale) }
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinutes = initialMinutes,
        is12Hour = timeFormatElements.clock12 != null,
        allowAfterMidnight = false,
    )

    ScrollableAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = { Text(stringResource(Res.string.quest_openingHours_chooseTimeTitle)) },
        content = {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
                Box(Modifier.fillMaxWidth()) {
                    ProvideTextStyle(MaterialTheme.typography.largeInput) {
                        TimePicker(
                            state = timePickerState,
                            timeFormatElements = timeFormatElements,
                            modifier = Modifier.align(Alignment.Center),
                            visibleAdjacentItems = 2,
                        )
                    }
                }
            }
        },
        buttons = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.cancel))
            }
            TextButton(
                onClick = {
                    onSelect(timePickerState.hour, timePickerState.minute)
                    onDismissRequest()
                }
            ) {
                Text(stringResource(Res.string.ok))
            }
        }
    )
}
