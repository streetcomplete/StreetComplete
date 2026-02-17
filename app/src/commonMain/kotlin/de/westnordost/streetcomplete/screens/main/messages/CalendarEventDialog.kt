package de.westnordost.streetcomplete.screens.main.messages

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.westnordost.streetcomplete.data.osmcal.CalendarEvent
import de.westnordost.streetcomplete.util.ktx.toLocalDate
import de.westnordost.streetcomplete.util.ktx.toLocalDateTime
import de.westnordost.streetcomplete.util.locale.DateTimeFormatStyle
import de.westnordost.streetcomplete.util.locale.LocalDateFormatter
import de.westnordost.streetcomplete.util.locale.LocalDateTimeFormatter

/** Dialog that shows an OSM event in the area */
@Composable
fun CalendarEventDialog(
    event: CalendarEvent,
    onDismissRequest: () -> Unit,
    onClickOpenEvent: () -> Unit,
    onToggleDontNotifyAgain: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val appear = remember { Animatable(0f) }
    val locale = Locale.current
    val dateFormatter = remember(locale) {
        LocalDateTimeFormatter(locale = locale, style = DateTimeFormatStyle.Medium)
    }

    LaunchedEffect(event) {
        appear.animateTo(1f, tween(1600, easing = LinearOutSlowInEasing))
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(null, null) { onDismissRequest() },
            contentAlignment = Alignment.Center
        ) {
            val startDate = dateFormatter.format(event.startDate.toLocalDateTime())

            if (event.endDate != null) {
                // same day: display time range
                if (event.startDate.toLocalDate() == event.endDate.toLocalDate()) {

                }
                // display date range
                else {

                }
            }

            // TODO
        }
    }
}
