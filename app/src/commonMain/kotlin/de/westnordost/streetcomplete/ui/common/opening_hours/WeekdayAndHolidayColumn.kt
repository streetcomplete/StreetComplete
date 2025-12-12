package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.osm.opening_hours.Weekdays
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_add_24
import de.westnordost.streetcomplete.resources.quest_openingHours_add_weekdays
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun WeekdayAndHolidayColumn(
    weekdaysList: List<Weekdays>,
    onChangeWeekdaysList: (List<Weekdays>) -> Unit,
    modifier: Modifier,
    locale: Locale = Locale.current,
    userLocale: Locale = Locale.current,
    enabled: Boolean = true,
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
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
                        onChangeWeekdaysList(weekdaysList.toMutableList().also {
                            it[index] = newWeekdays
                        })
                    },
                    modifier = Modifier.weight(1f),
                    locale = locale,
                    userLocale = userLocale,
                    enabled = enabled,
                )
                TimeSpansSelectorsColumn(
                    times = weekdays.timesSelectors,
                    onChangeTimes = { newTimes ->
                        // when last time has been removed, entire column shall be removed
                        if (newTimes.isEmpty()) {
                            onChangeWeekdaysList(weekdaysList.toMutableList().also { it.removeAt(index) })
                        } else {
                            val newWeekdays = weekdaysList[index].copy(timesSelectors = newTimes)
                            onChangeWeekdaysList(weekdaysList.toMutableList().also { it[index] = newWeekdays })
                        }
                    },
                    timeTextWidth = timesWidth,
                    locale = locale,
                    enabled = enabled,
                )
            }
        }
        if (enabled) {
            OutlinedButton(
                onClick = { /** TODO add weekday... */ }
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_add_24),
                    contentDescription = stringResource(Res.string.quest_openingHours_add_weekdays)
                )
            }
        }
    }

    if (showDialog) {
        WeekdayAndHolidaySelectDialog(
            onDismissRequest = { showDialog = false },
            onSelected = { weekdaysSelectors, holidaysSelectors ->
                // TODO: actually first show weekday dialog, then times dialog...
                val newWeekdays = Weekdays(weekdaysSelectors, holidaysSelectors, timesList)
                onChangeWeekdaysList(weekdaysList.toMutableList().also { it.add(newWeekdays) })
            },
            locale = locale,
            userLocale = userLocale,
        )
    }
}
