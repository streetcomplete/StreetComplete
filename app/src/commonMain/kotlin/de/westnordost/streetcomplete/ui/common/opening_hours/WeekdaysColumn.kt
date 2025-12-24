package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import de.westnordost.osm_opening_hours.model.HolidaySelector
import de.westnordost.osm_opening_hours.model.WeekdaysSelector
import de.westnordost.streetcomplete.osm.opening_hours.Off
import de.westnordost.streetcomplete.osm.opening_hours.Times
import de.westnordost.streetcomplete.osm.opening_hours.Weekdays
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_add_24
import de.westnordost.streetcomplete.resources.quest_openingHours_add_off_days
import de.westnordost.streetcomplete.resources.quest_openingHours_add_weekdays
import de.westnordost.streetcomplete.ui.common.DropdownMenuItem
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** A column of weekdays for which each a column of times (or "off") exists next to it. All the
 *  values can be changed by the user.
 *
 *  E.g.
 *  ```
 *  Mon-Sat   08:00-12:00  [x]
 *            14:00-16:00  [x]
 *  Sun, PH   off          [x]
 *  [+]
 *  ```
 * */
@Composable
fun WeekdaysColumn(
    weekdaysList: List<Weekdays>,
    onChange: (List<Weekdays>) -> Unit,
    timeMode: TimeMode,
    modifier: Modifier,
    initialWeekdaysSelectors: List<WeekdaysSelector> = emptyList(),
    locale: Locale = Locale.current,
    userLocale: Locale = Locale.current,
    enabled: Boolean = true,
) {
    var addWeekdaysState by remember { mutableStateOf<AddWeekdaysState?>(null) }

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
                            timeMode = timeMode,
                            modifier = Modifier.weight(3f),
                            locale = locale,
                            enabled = enabled,
                        )
                    }
                }

            }
        }
        if (enabled) {
            OutlinedButton(
                onClick = { addWeekdaysState = AddWeekdaysState.SelectWeekdaysType }
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_add_24),
                    contentDescription = stringResource(Res.string.quest_openingHours_add_weekdays)
                )
            }
        }
    }

    WeekdaysModeDropdownMenu(
        expanded = addWeekdaysState == AddWeekdaysState.SelectWeekdaysType,
        onDismissRequest = { addWeekdaysState = null },
        onSelect = { addWeekdaysState = AddWeekdaysState.SelectWeekdays(it) }
    )
    when (val state = addWeekdaysState) {
        is AddWeekdaysState.SelectWeekdays -> {
            WeekdayAndHolidaySelectDialog(
                onDismissRequest = { addWeekdaysState = null },
                onSelected = { weekdaysSelectors, holidaysSelectors ->
                    when (state.weekaysType) {
                        WeekdaysType.Off -> {
                            val newWeekdays = Weekdays(weekdaysSelectors, holidaysSelectors, Off)
                            onChange(weekdaysList.toMutableList().also { it.add(newWeekdays) })
                        }
                        WeekdaysType.Times -> {
                            addWeekdaysState = AddWeekdaysState.SelectTime(weekdaysSelectors, holidaysSelectors)
                        }
                    }
                },
                initialWeekdays = if (weekdaysList.isEmpty()) initialWeekdaysSelectors else emptyList(),
                locale = locale,
                userLocale = userLocale,
            )
        }
        is AddWeekdaysState.SelectTime -> {
            TimesSelectorDialog(
                onDismissRequest = { addWeekdaysState = null },
                mode = timeMode,
                onSelect = { newTime ->
                    val newWeekdays = Weekdays(state.weekdays, state.holidays, Times(listOf(newTime)))
                    onChange(weekdaysList.toMutableList().also { it.add(newWeekdays) })
                },
                locale = locale,
            )
        }
        else -> {}
    }
}

private sealed interface AddWeekdaysState {
    object SelectWeekdaysType : AddWeekdaysState
    data class SelectWeekdays(val weekaysType: WeekdaysType) : AddWeekdaysState
    data class SelectTime(
        val weekdays: List<WeekdaysSelector>,
        val holidays: List<HolidaySelector>
    ) : AddWeekdaysState
}

private enum class WeekdaysType {
    /** Times follow to specify at what times of the given weekdays it is open */
    Times,
    /** Weekdays define when it is closed */
    Off,
}

@Composable
private fun WeekdaysModeDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onSelect: (WeekdaysType) -> Unit,
    modifier: Modifier = Modifier,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        for (weekdaysMode in WeekdaysType.entries) {
            DropdownMenuItem(onClick = { onSelect(weekdaysMode) }) {
                Text(stringResource(weekdaysMode.titleResource))
            }
        }
    }
}

private val WeekdaysType.titleResource: StringResource get() = when (this) {
    WeekdaysType.Times -> Res.string.quest_openingHours_add_weekdays
    WeekdaysType.Off -> Res.string.quest_openingHours_add_off_days
}
