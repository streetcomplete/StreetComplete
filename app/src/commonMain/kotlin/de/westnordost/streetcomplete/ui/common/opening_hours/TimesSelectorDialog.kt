package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import de.westnordost.osm_opening_hours.model.ClockTime
import de.westnordost.osm_opening_hours.model.Time
import de.westnordost.osm_opening_hours.model.TimeIntervals
import de.westnordost.osm_opening_hours.model.TimeSpan
import de.westnordost.osm_opening_hours.model.TimeSpansSelector
import de.westnordost.osm_opening_hours.model.TimesSelector

@Composable
fun TimesSelectorDialog(
    onDismissRequest: () -> Unit,
    mode: TimeMode,
    onSelect: (TimesSelector) -> Unit,
    modifier: Modifier = Modifier,
    locale: Locale = Locale.current,
) {
    TimesSelectorDialog(
        onDismissRequest = onDismissRequest,
        // typical opening hours, actually, according to taginfo, the most common ones
        initialTime = when (mode) {
            TimeMode.Points -> ClockTime(9, 0)
            TimeMode.Spans -> TimeSpan(ClockTime(9,0), ClockTime(18,0))
        },
        onSelect = onSelect,
        modifier = modifier,
        locale = locale,
    )
}

@Composable
fun TimesSelectorDialog(
    onDismissRequest: () -> Unit,
    initialTime: TimesSelector,
    onSelect: (TimesSelector) -> Unit,
    modifier: Modifier = Modifier,
    locale: Locale = Locale.current,
) {
    when (initialTime) {
        is TimeSpansSelector -> {
            TimeSpansSelectorSelectDialog(
                onDismissRequest = onDismissRequest,
                onSelect = onSelect,
                initialTimeSpansSelector = initialTime,
                modifier = modifier,
                locale = locale,
            )
        }
        is Time -> {
            TimeSelectDialog(
                onDismissRequest = onDismissRequest,
                onSelect = onSelect,
                initialTime = initialTime,
                modifier = modifier,
                locale = locale,
            )
        }
        is TimeIntervals -> {
            throw UnsupportedOperationException()
        }
    }
}
