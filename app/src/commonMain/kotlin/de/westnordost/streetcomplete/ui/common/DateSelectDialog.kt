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
import de.westnordost.streetcomplete.util.locale.DateFormatElements
import de.westnordost.streetcomplete.util.locale.systemDefaultDateFormatElements
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

/** Dialog in which to select a date */
@Composable
fun DateSelectDialog(
    onDismissRequest: () -> Unit,
    onSelect: (date: LocalDate) -> Unit,
    initialDate: LocalDate,
    modifier: Modifier = Modifier,
    title: (@Composable () -> Unit)? = null,
    years: IntRange = (initialDate.year - 10)..(initialDate.year + 10),
    locale: Locale = Locale.current,
) {
    // Prefer the platform's per-user date-format override (iOS Language & Region)
    // and fall back to deriving the order from the locale's short date pattern.
    val dateFormatElements = remember(locale) {
        systemDefaultDateFormatElements() ?: DateFormatElements.of(locale)
    }
    val datePickerState = rememberDatePickerState(
        initialDate = initialDate,
        years = years,
    )

    ScrollableAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = title,
        content = {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
                Box(Modifier.fillMaxWidth()) {
                    ProvideTextStyle(MaterialTheme.typography.largeInput) {
                        DatePicker(
                            state = datePickerState,
                            dateFormatElements = dateFormatElements,
                            modifier = Modifier.align(Alignment.Center),
                            locale = locale,
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
                    onSelect(datePickerState.date)
                    onDismissRequest()
                }
            ) {
                Text(stringResource(Res.string.ok))
            }
        }
    )
}
