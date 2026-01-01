package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.osm.opening_hours.Off
import de.westnordost.streetcomplete.osm.opening_hours.Times
import de.westnordost.streetcomplete.osm.opening_hours.Weekdays

/** A column of weekdays for which each a column of times (or "off") exists next to it. All the
 *  values can be changed by the user.
 *
 *  E.g.
 *  ```
 *  Mon-Sat   08:00-12:00  [x]
 *            14:00-16:00  [x]
 *  Sun, PH   off          [x]
 *  ```
 * */
@Composable
fun WeekdaysColumn(
    weekdaysList: List<Weekdays>,
    onChange: (List<Weekdays>) -> Unit,
    modifier: Modifier,
    locale: Locale = Locale.current,
    userLocale: Locale = Locale.current,
    enabled: Boolean = true,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        for ((index, weekdays) in weekdaysList.withIndex()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                WeekdayAndHolidaySelectorsText(
                    weekdays = weekdays.weekdaysSelectors,
                    holidays = weekdays.holidaysSelectors,
                    onChange = { weekdaysSelectors, holidaysSelectors ->
                        val newWeekdays = weekdaysList[index].copy(
                            weekdaysSelectors = weekdaysSelectors,
                            holidaysSelectors = holidaysSelectors
                        )
                        onChange(weekdaysList.toMutableList().also {
                            it[index] = newWeekdays
                        })
                    },
                    modifier = Modifier.weight(2f),
                    locale = locale,
                    userLocale = userLocale,
                    enabled = enabled,
                )
                when (weekdays.times) {
                    Off -> {
                        OffDayRow(
                            onClickDelete = {
                                val newWeekdaysList = weekdaysList.toMutableList()
                                newWeekdaysList.removeAt(index)
                                onChange(newWeekdaysList)
                            },
                            modifier = Modifier.weight(3f),
                            enabled = enabled,
                        )
                    }
                    is Times -> {
                        TimesSelectorsColumn(
                            times = weekdays.times.selectors,
                            onChange = { newTimes ->
                                val newWeekdaysList = weekdaysList.toMutableList()
                                // when last time has been removed, entire column shall be removed
                                if (newTimes.isEmpty()) {
                                    newWeekdaysList.removeAt(index)
                                } else {
                                    newWeekdaysList[index] = weekdaysList[index].copy(times = Times(newTimes))
                                }
                                onChange(newWeekdaysList)
                            },
                            modifier = Modifier.weight(3f),
                            locale = locale,
                            enabled = enabled,
                        )
                    }
                }
            }
        }
    }
}
