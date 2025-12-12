package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.foundation.clickable
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.LayoutDirection.Ltr
import de.westnordost.osm_opening_hours.model.StartingAtTime
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
    var showDialog by remember { mutableStateOf(false) }

    Text(
        text = time.toLocalizedString(
            layoutDirection = LocalLayoutDirection.current,
            locale = locale,
        ),
        modifier = modifier.clickable(enabled) { showDialog = true }
    )

    if (showDialog) {
        TimeSpansSelectorSelectDialog(
            onDismissRequest = { showDialog = false },
            onSelect = onChangeTime,
            initialTimeSpansSelector = time,
            locale = locale,
        )
    }
}

@Composable
private fun TimeSpansSelector.toLocalizedString(
    layoutDirection: LayoutDirection = Ltr,
    locale: Locale? = null,
): String {
    val timeFormatter = remember(locale) {
        LocalTimeFormatter(locale = locale, style = DateTimeFormatStyle.Short)
    }
    return when (this) {
        is StartingAtTime -> {
            val startTimeText = start.toLocalizedString(timeFormatter)
            stringResource(Res.string.opening_hours_from, startTimeText)
        }
        is TimeSpan -> {
            val startTimeText = start.toLocalizedString(timeFormatter)
            val endTime = end.toLocalizedString(timeFormatter)
            val endText = if (openEnd) {
                stringResource(Res.string.opening_hours_until_late, endTime)
            } else {
                endTime
            }
            localizedRange(
                start = startTimeText,
                end = endText,
                locale = locale,
                layoutDirection = layoutDirection,
            )
        }
    }
}
