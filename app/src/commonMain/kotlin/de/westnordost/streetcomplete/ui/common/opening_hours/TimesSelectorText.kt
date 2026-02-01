package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.LayoutDirection.Ltr
import androidx.compose.ui.unit.dp
import de.westnordost.osm_opening_hours.model.ClockTime
import de.westnordost.osm_opening_hours.model.ExtendedClockTime
import de.westnordost.osm_opening_hours.model.ExtendedTime
import de.westnordost.osm_opening_hours.model.StartingAtTime
import de.westnordost.osm_opening_hours.model.Time
import de.westnordost.osm_opening_hours.model.TimeIntervals
import de.westnordost.osm_opening_hours.model.TimeSpan
import de.westnordost.osm_opening_hours.model.TimesSelector
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_openingHours_from
import de.westnordost.streetcomplete.resources.quest_openingHours_until_late
import de.westnordost.streetcomplete.util.locale.DateTimeFormatStyle
import de.westnordost.streetcomplete.util.locale.LocalTimeFormatter
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource

/** A text that shows a localized time or time span and lets you select another time/time span.
 *
 *  E.g. "08:00-18:00" but also "from 12:00" or "16:00-22:00 until late" (i.e. open end), as well
 *  as "08:00". */
@Composable
fun TimesSelectorText(
    time: TimesSelector,
    onChange: (TimesSelector) -> Unit,
    modifier: Modifier = Modifier,
    locale: Locale = Locale.current,
    enabled: Boolean = true,
) {
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clickable(enabled) { showDialog = true }
            .defaultMinSize(minHeight = 48.dp)
            .padding(8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = time.toLocalizedString(LocalLayoutDirection.current, locale)
        )
    }

    if (showDialog) {
        TimesSelectorDialog(
            onDismissRequest = { showDialog = false },
            onSelect = onChange,
            initialTime = time,
            locale = locale,
        )
    }
}

@Composable
private fun TimesSelector.toLocalizedString(
    layoutDirection: LayoutDirection = Ltr,
    locale: Locale? = null,
): String {
    val timeFormatter = remember(locale) {
        LocalTimeFormatter(locale = locale, style = DateTimeFormatStyle.Short)
    }
    return when (this) {
        is StartingAtTime -> {
            val startTimeText = start.toLocalizedString(timeFormatter)
            stringResource(Res.string.quest_openingHours_from, startTimeText)
        }
        is TimeSpan -> {
            val startTimeText = start.toLocalizedString(timeFormatter)
            val endTime = end.toLocalizedString(timeFormatter)
            val endText = if (openEnd) {
                stringResource(Res.string.quest_openingHours_until_late, endTime)
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
        is Time -> {
            toLocalizedString(timeFormatter)
        }
        is TimeIntervals -> {
            throw UnsupportedOperationException()
        }
    }
}


private fun Time.toLocalizedString(timeFormatter: LocalTimeFormatter): String =
    when (this) {
        is ClockTime -> timeFormatter.format(toLocalTime())
        else -> throw UnsupportedOperationException()
    }

private fun ExtendedTime.toLocalizedString(timeFormatter: LocalTimeFormatter): String =
    when (this) {
        is ClockTime -> timeFormatter.format(toLocalTime())
        is ExtendedClockTime -> timeFormatter.format(toLocalTime())
        else -> throw UnsupportedOperationException()
    }

private fun ClockTime.toLocalTime() = LocalTime(hour, minutes)

private fun ExtendedClockTime.toLocalTime() = LocalTime(hour % 24, minutes)
