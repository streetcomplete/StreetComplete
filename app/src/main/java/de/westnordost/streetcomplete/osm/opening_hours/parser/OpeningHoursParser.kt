package de.westnordost.streetcomplete.osm.opening_hours.parser

import de.westnordost.osm_opening_hours.model.ClockTime
import de.westnordost.osm_opening_hours.model.ExtendedClockTime
import de.westnordost.osm_opening_hours.model.ExtendedTime
import de.westnordost.osm_opening_hours.model.Holiday
import de.westnordost.osm_opening_hours.model.HolidaySelector
import de.westnordost.osm_opening_hours.model.MonthRange
import de.westnordost.osm_opening_hours.model.MonthsOrDateSelector
import de.westnordost.osm_opening_hours.model.OpeningHours
import de.westnordost.osm_opening_hours.model.Range
import de.westnordost.osm_opening_hours.model.RuleType
import de.westnordost.osm_opening_hours.model.SingleMonth
import de.westnordost.osm_opening_hours.model.StartingAtTime
import de.westnordost.osm_opening_hours.model.TimeSpan
import de.westnordost.osm_opening_hours.model.TimeSpansSelector
import de.westnordost.osm_opening_hours.model.VariableTime
import de.westnordost.osm_opening_hours.model.Weekday
import de.westnordost.osm_opening_hours.model.WeekdayRange
import de.westnordost.osm_opening_hours.model.WeekdaysSelector
import de.westnordost.streetcomplete.osm.opening_hours.model.Months
import de.westnordost.streetcomplete.osm.opening_hours.model.TimeRange
import de.westnordost.streetcomplete.osm.opening_hours.model.Weekdays
import de.westnordost.streetcomplete.osm.opening_hours.model.Weekdays.Companion.PUBLIC_HOLIDAY
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OffDaysRow
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningHoursRow
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningMonthsRow
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningWeekdaysRow
import de.westnordost.streetcomplete.quests.postbox_collection_times.CollectionTimesRow

/** returns null if the list of rules cannot be displayed by the opening hours widget */
fun OpeningHours.toOpeningHoursRows(): List<OpeningHoursRow>? {
    if (!isSupportedOpeningHours()) {
        // parsable, but not supported by StreetComplete
        return null
    }

    val result = mutableListOf<OpeningHoursRow>()

    var currentMonths: Months? = null

    for (rule in rules) {
        val range = rule.selector as Range

        val months = range.months?.toMonths()

        if (months != currentMonths) {
            result.add(OpeningMonthsRow(months ?: Months()))
        }
        currentMonths = months

        val weekdaysAndHolidays = WeekdaysAndHolidays(range.weekdays, range.holidays).toWeekdays()

        if (rule.ruleType != null && rule.ruleType in listOf(RuleType.Off, RuleType.Closed)) {
            result.add(OffDaysRow(weekdaysAndHolidays))
        } else {
            for (time in range.times!!) {
                val timeRange = (time as TimeSpansSelector).toTimeRange()
                result.add(OpeningWeekdaysRow(weekdaysAndHolidays, timeRange))
            }
        }
    }

    return result
}

fun OpeningHours.toCollectionTimesRows(): List<CollectionTimesRow>? {
    if (!isSupportedCollectionTimes()) {
        // parsable, but not supported by StreetComplete
        return null
    }
    val result = mutableListOf<CollectionTimesRow>()

    for (rule in rules) {
        val range = rule.selector as Range
        val weekdaysAndHolidays = WeekdaysAndHolidays(range.weekdays, range.holidays).toWeekdays()

        for (time in range.times!!) {
            result.add(CollectionTimesRow(weekdaysAndHolidays, (time as ClockTime).toMinutesOfDay()))
        }
    }
    return result
}

private fun Collection<MonthsOrDateSelector>.toMonths(): Months {
    val monthsData = BooleanArray(Months.MONTHS_COUNT)
    for (selector in this) {
        when (selector) {
            is SingleMonth -> {
                monthsData[selector.month.ordinal] = true
            }
            is MonthRange -> {
                val start = selector.start.ordinal
                val end = selector.end.ordinal
                if (start <= end) { // ranges like Jan-Feb
                    for (i in start..end) monthsData[i] = true
                } else { // ranges like Oct-Jan
                    for (i in start..<Months.MONTHS_COUNT) monthsData[i] = true
                    for (i in 0..end) monthsData[i] = true
                }
            }
            else -> throw IllegalArgumentException()
        }
    }
    return Months(monthsData)
}

data class WeekdaysAndHolidays(
    val weekdays: List<WeekdaysSelector>?,
    val holidays: List<HolidaySelector>?
)

private fun WeekdaysAndHolidays.toWeekdays(): Weekdays {
    val dayData = BooleanArray(Weekdays.OSM_ABBR_WEEKDAYS.size) { false }
    for (weekday in weekdays.orEmpty()) {
        when (weekday) {
            is Weekday -> {
                dayData[weekday.ordinal] = true
            }
            is WeekdayRange -> {
                val start = weekday.start.ordinal
                val end = weekday.end.ordinal
                if (start <= end) { // ranges like Tuesday-Saturday
                    for (i in start..end) dayData[i] = true
                } else { // ranges like Saturday-Tuesday
                    for (i in start..<Weekdays.WEEKDAY_COUNT) dayData[i] = true
                    for (i in 0..end) dayData[i] = true
                }
            }
            else -> throw IllegalArgumentException()
        }
    }
    for (holiday in holidays.orEmpty()) {
        when (holiday) {
            Holiday.PublicHoliday -> dayData[PUBLIC_HOLIDAY] = true
            else -> throw IllegalArgumentException()
        }
    }
    return Weekdays(dayData)
}

private fun TimeSpansSelector.toTimeRange(): TimeRange = when (this) {
    is StartingAtTime -> TimeRange(start.toMinutesOfDay(), start.toMinutesOfDay(), true)
    is TimeSpan ->       TimeRange(start.toMinutesOfDay(), end.toMinutesOfDay(), openEnd)
}

internal fun ClockTime.toMinutesOfDay(): Int = hour * 60 + minutes

internal fun ExtendedTime.toMinutesOfDay(): Int = when (this) {
    is ExtendedClockTime -> hour * 60 + minutes
    is ClockTime -> toMinutesOfDay()
    is VariableTime -> throw IllegalArgumentException()
}
