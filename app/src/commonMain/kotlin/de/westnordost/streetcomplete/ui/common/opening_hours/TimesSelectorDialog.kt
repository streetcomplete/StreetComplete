package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import de.westnordost.osm_opening_hours.model.Time
import de.westnordost.osm_opening_hours.model.TimeIntervals
import de.westnordost.osm_opening_hours.model.TimeSpansSelector
import de.westnordost.osm_opening_hours.model.TimesSelector

/** Dialog that lets the user select either a time point or time span, depending on the
 *  [initialTime]. */
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
