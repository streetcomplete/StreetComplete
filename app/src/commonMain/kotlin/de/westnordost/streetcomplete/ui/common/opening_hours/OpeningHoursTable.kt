package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.rememberTextMeasurer
import de.westnordost.osm_opening_hours.model.TimePointsSelector
import de.westnordost.osm_opening_hours.model.TimeSpansSelector
import de.westnordost.streetcomplete.osm.opening_hours.HierarchicOpeningHours
import de.westnordost.streetcomplete.ui.ktx.pxToDp
import de.westnordost.streetcomplete.util.locale.DateTimeFormatStyle
import de.westnordost.streetcomplete.util.locale.LocalTimeFormatter
import kotlinx.datetime.LocalTime

/** Displays the given [openingHours] for editing */
@Composable
fun OpeningHoursTable(
    openingHours: HierarchicOpeningHours,
    modifier: Modifier = Modifier,
    locale: Locale = Locale.current,
    enabled: Boolean = true,
) {
    val locale = Locale.current
    val textStyle = LocalTextStyle.current
    val textMeasurer = rememberTextMeasurer()
    val timesWidthPx = remember(locale) {
        val timeFormatter = LocalTimeFormatter(locale = locale, style = DateTimeFormatStyle.Short)
        // TODO only if we display ranges...
        val rangeText = localizedRange(
             start = timeFormatter.format(LocalTime(13,59)),
             end = timeFormatter.format(LocalTime(23,59)),
             locale = locale,
        )
        textMeasurer.measure(text = rangeText, style = textStyle).size.width
    }
    val timesWidth = timesWidthPx.pxToDp()

    Column(
        modifier = modifier
    ) {
        for ((monthsIndex, months) in openingHours.monthsList.withIndex()) {
            if (months.monthsSelector.isNotEmpty()) {
                MonthsText(
                    months = months.monthsSelector,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled) {
                            // TODO
                        }
                )
            }
            if (monthsIndex > 0 || months.monthsSelector.isNotEmpty()) {
                Divider()
            }
            for ((weekdaysIndex, weekdays) in months.weekdaysList.withIndex()) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    WeekdayAndHolidaySelectorsText(
                        weekdays = weekdays.weekdaysSelector,
                        holidays = weekdays.holidaysSelector,
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled) {
                                // TODO
                            }
                    )
                    Column(
                        modifier = Modifier.width(timesWidth)
                    ) {
                        for ((timesIndex, times) in weekdays.timesList.withIndex()) {
                            when (times) {
                                is TimePointsSelector -> TODO()
                                is TimeSpansSelector -> TODO()
                            }
                        }
                    }
                }
            }
        }
    }
}

