package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.intl.Locale
import de.westnordost.osm_opening_hours.model.ClockTime
import de.westnordost.osm_opening_hours.model.ExtendedClockTime
import de.westnordost.osm_opening_hours.model.HolidaySelector
import de.westnordost.osm_opening_hours.model.Month
import de.westnordost.osm_opening_hours.model.MonthsOrDateSelector
import de.westnordost.osm_opening_hours.model.TimeSpan
import de.westnordost.osm_opening_hours.model.TimesSelector
import de.westnordost.osm_opening_hours.model.WeekdaysSelector
import de.westnordost.streetcomplete.osm.opening_hours.HierarchicOpeningHours
import de.westnordost.streetcomplete.osm.opening_hours.Months
import de.westnordost.streetcomplete.osm.opening_hours.Off
import de.westnordost.streetcomplete.osm.opening_hours.Times
import de.westnordost.streetcomplete.osm.opening_hours.Weekdays
import de.westnordost.streetcomplete.osm.opening_hours.getLastClockTime
import de.westnordost.streetcomplete.osm.opening_hours.getMonths
import de.westnordost.streetcomplete.osm.opening_hours.toMonthsSelectors
import de.westnordost.streetcomplete.ui.common.opening_hours.AddOpeningHoursRequest.*

/** A cascade of dialogs with which the user can add new opening hours at the end of an opening
 *  hours: Depending on the [requestedData], either times only, weekdays+times, weekdays off or
 *  months+weekdays+times are added. */
@Composable
fun AddOpeningHoursDialogCascade(
    onDismissRequest: () -> Unit,
    requestedData: AddOpeningHoursRequest,
    openingHours: HierarchicOpeningHours,
    onChange: (HierarchicOpeningHours) -> Unit,
    workweek: List<WeekdaysSelector>,
    timeMode: TimeMode,
    locale: Locale = Locale.current,
    userLocale: Locale = Locale.current,
) {
    var step by remember(requestedData) { mutableStateOf(requestedData) }
    val data = remember { AddOpeningHoursData() }

    fun selectComplete() {
        val m = data.months
        val w = data.weekdays
        val h = data.holidays
        val t = data.time

        val newMonthsList = openingHours.monthsList.toMutableList()
        // add month + weekdays + holidays + time
        if (m != null && w != null && h != null && t != null) {
            newMonthsList.add(Months(m, listOf(Weekdays(w, h, Times(listOf(t))))))
        }
        else {
            if (newMonthsList.isEmpty()) {
                newMonthsList.add(Months(emptyList(), emptyList()))
            }
            val newWeekdaysList = newMonthsList[newMonthsList.lastIndex].weekdaysList.toMutableList()
            // add weekdays + holidays + time / off
            if (w != null && h != null) {
                val content = if (t != null) Times(listOf(t)) else Off
                newWeekdaysList.add(Weekdays(w, h, content))
            } else if (t != null) {
                val newTimes = (newWeekdaysList.lastOrNull()?.times as? Times)?.selectors?.toMutableList()

                if (newTimes != null) {
                    newTimes.add(t)
                    newWeekdaysList[newWeekdaysList.lastIndex] = newWeekdaysList[newWeekdaysList.lastIndex].copy(times = Times(newTimes))
                }
                // special case: no weekdays defined -> Add times in empty weekdays
                else {
                    newWeekdaysList.add(Weekdays(emptyList(), emptyList(), Times(listOf(t))))
                }
            }
            newMonthsList[newMonthsList.lastIndex] = newMonthsList[newMonthsList.lastIndex].copy(weekdaysList = newWeekdaysList)
        }

        onChange(HierarchicOpeningHours(newMonthsList))
    }

    when (step) {
        SelectMonths -> {
            val initialMonths = remember(openingHours.monthsList) {
                val mentioned = openingHours.monthsList.flatMap { it.selectors }.getMonths()
                // initially: select nothing.
                // for any following one: pre-select months not mentioned in previous rules
                if (mentioned.isEmpty()) {
                    emptyList()
                } else {
                    val unmentioned = (Month.entries.toSet() - mentioned)
                    unmentioned.toMonthsSelectors()
                }
            }

            MonthsOrDateSelectorSelectDialog(
                onDismissRequest = { if (step == SelectMonths) onDismissRequest() },
                initialMonths = initialMonths,
                onSelected = { newMonthsSelectorList ->
                    data.months = newMonthsSelectorList
                    step = SelectWeekdays
                },

                locale = locale,
                userLocale = userLocale,
            )
        }
        SelectWeekdays,
        SelectOffWeekdays -> {
            val currentWeekdays = openingHours.monthsList.lastOrNull()?.weekdaysList ?: emptyList()
            val initialWeekdays = remember(currentWeekdays, workweek) {
                // pre-select work week only if there's no weekdays defined yet in this month
                // or if a new month is added anyway
                if (currentWeekdays.isEmpty() || requestedData == SelectMonths) {
                    workweek
                } else {
                    emptyList()
                }
            }

            WeekdayAndHolidaySelectDialog(
                onDismissRequest = {
                    if (step == SelectWeekdays || step == SelectOffWeekdays) onDismissRequest()
                },
                onSelected = { weekdaysSelectors, holidaysSelectors ->
                    data.weekdays = weekdaysSelectors
                    data.holidays = holidaysSelectors
                    if (step == SelectOffWeekdays) {
                        selectComplete()
                    } else {
                        step = SelectTimes
                    }
                },
                initialWeekdays = if (step == SelectWeekdays) initialWeekdays else emptyList(),
                locale = locale,
                userLocale = userLocale,
            )
        }
        SelectTimes -> {
            val addsTimesToExistingWeekdays = data.weekdays == null && data.holidays == null && data.months == null
            val lastClockTime = openingHours.getLastClockTime()
            val initialTime = if (addsTimesToExistingWeekdays && lastClockTime != null) {
                // when adding another time to weekdays, start the picker at 1 hour after the last
                // time (i.e. after lunch break)
                val newStartTime = lastClockTime.copy((lastClockTime.hour + 1) % 24)
                when (timeMode) {
                    TimeMode.Points -> newStartTime
                    TimeMode.Spans -> {
                        val newEndTime = ExtendedClockTime(newStartTime.hour + 4, 0)
                        TimeSpan(newStartTime, newEndTime)
                    }
                }
            } else {
                // typical opening hours, actually, according to taginfo, the most common ones
                when (timeMode) {
                    TimeMode.Points -> ClockTime(9, 0)
                    TimeMode.Spans -> TimeSpan(ClockTime(9, 0), ClockTime(18, 0))
                }
            }

            TimesSelectorDialog(
                onDismissRequest = { if (step == SelectTimes) onDismissRequest() },
                initialTime = initialTime,
                onSelect = { newTime ->
                    data.time = newTime
                    selectComplete()
                },
                locale = locale,
            )
        }
    }
}

enum class AddOpeningHoursRequest {
    SelectMonths,
    SelectWeekdays,
    SelectOffWeekdays,
    SelectTimes
}

private data class AddOpeningHoursData(
    var months: List<MonthsOrDateSelector>? = null,
    var weekdays: List<WeekdaysSelector>? = null,
    var holidays: List<HolidaySelector>? = null,
    var time: TimesSelector? = null,
)
