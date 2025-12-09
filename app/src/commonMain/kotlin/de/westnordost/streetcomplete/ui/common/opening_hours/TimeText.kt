package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.foundation.clickable
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import de.westnordost.osm_opening_hours.model.Time
import de.westnordost.streetcomplete.util.locale.DateTimeFormatStyle
import de.westnordost.streetcomplete.util.locale.LocalTimeFormatter

/** A text that shows a localized time and lets you select another time. E.g. 08:00 AM. */
@Composable
fun TimeText(
    time: Time,
    onChangeTime: (Time) -> Unit,
    modifier: Modifier = Modifier,
    locale: Locale = Locale.current,
    enabled: Boolean = true,
) {
    val timeFormatter = remember(locale) {
        LocalTimeFormatter(locale = locale, style = DateTimeFormatStyle.Short)
    }
    var showDialog by remember { mutableStateOf(false) }

    Text(
        text = time.toLocalizedString(timeFormatter),
        modifier = modifier.clickable(enabled) { showDialog = true }
    )

    if (showDialog) {
        TimeSelectDialog(
            onDismissRequest = { showDialog = false },
            onSelect = onChangeTime,
            initialTime = time,
            locale = locale,
        )
    }
}
