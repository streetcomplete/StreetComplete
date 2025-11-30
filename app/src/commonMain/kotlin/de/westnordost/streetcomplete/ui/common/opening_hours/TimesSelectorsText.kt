package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.intl.Locale
import de.westnordost.osm_opening_hours.model.ClockTime
import de.westnordost.osm_opening_hours.model.EventTime
import de.westnordost.osm_opening_hours.model.ExtendedClockTime
import de.westnordost.osm_opening_hours.model.ExtendedTime
import de.westnordost.osm_opening_hours.model.StartingAtTime
import de.westnordost.osm_opening_hours.model.Time
import de.westnordost.osm_opening_hours.model.TimeIntervals
import de.westnordost.osm_opening_hours.model.TimePointsSelector
import de.westnordost.osm_opening_hours.model.TimeSpan
import de.westnordost.osm_opening_hours.model.TimeSpansSelector
import de.westnordost.osm_opening_hours.model.VariableTime
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.util.locale.DateTimeFormatStyle
import de.westnordost.streetcomplete.util.locale.LocalTimeFormatter
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** A text that shows a list of localized time points. E.g. 08:00, sunrise */
@Composable
fun TimePointsSelectorsText(
    times: List<TimePointsSelector>,
    modifier: Modifier,
) {
    val locale = Locale.current
    val layoutDirection = LocalLayoutDirection.current
    val timeFormatter = LocalTimeFormatter(
        locale = locale,
        style = DateTimeFormatStyle.Short
    )

    val timesStrings = times.map { time ->
        when (time) {
            is Time -> time.toLocalizedString(timeFormatter)
            is TimeIntervals -> throw UnsupportedOperationException()
        }
    }

    val text = timesStrings.joinToLocalizedString(locale, layoutDirection)
    Text(text, modifier)
}

/** A text that shows a list of localized time spans. E.g. 08:00-12:00, sunrise-sunset */
@Composable
fun TimeSpansSelectorsText(
    times: List<TimeSpansSelector>,
    modifier: Modifier = Modifier,
) {
    val locale = Locale.current
    val layoutDirection = LocalLayoutDirection.current
    val timeFormatter = LocalTimeFormatter(
        locale = locale,
        style = DateTimeFormatStyle.Short
    )

    val timesStrings = times.map { time ->
        when (time) {
            is StartingAtTime -> {
                val startTime = time.start.toLocalizedString(timeFormatter)
                stringResource(Res.string.opening_hours_from, startTime)
            }
            is TimeSpan -> {
                val range = localizedRange(
                    start = time.start.toLocalizedString(timeFormatter),
                    end = time.end.toLocalizedString(timeFormatter),
                    locale = locale,
                    layoutDirection = layoutDirection
                )
                stringResource(Res.string.opening_hours_until_late, range)
            }
        }
    }

    val text = timesStrings.joinToLocalizedString(locale, layoutDirection)
    Text(text, modifier)
}

@Composable
private fun Time.toLocalizedString(timeFormatter: LocalTimeFormatter): String =
    when (this) {
        is ClockTime -> timeFormatter.format(toLocalTime())
        is VariableTime -> toLocalizedString()
    }

@Composable
private fun ExtendedTime.toLocalizedString(timeFormatter: LocalTimeFormatter): String =
    when (this) {
        is ClockTime -> timeFormatter.format(toLocalTime())
        is ExtendedClockTime -> timeFormatter.format(toLocalTime())
        is VariableTime -> toLocalizedString()
    }

@Composable
private fun VariableTime.toLocalizedString(): String {
    if (timeOffset != null) throw UnsupportedOperationException()
    return stringResource(dailyEvent.getDisplayNameResource())
}

private fun EventTime.getDisplayNameResource(): StringResource = when (this) {
    EventTime.Dawn -> Res.string.opening_hours_dawn
    EventTime.Sunrise -> Res.string.opening_hours_sunrise
    EventTime.Sunset -> Res.string.opening_hours_sunset
    EventTime.Dusk -> Res.string.opening_hours_dusk
}

private fun ClockTime.toLocalTime() = LocalTime(hour, minutes)

private fun ExtendedClockTime.toLocalTime() = LocalTime(hour % 24, minutes)
