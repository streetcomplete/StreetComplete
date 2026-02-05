package de.westnordost.streetcomplete.osm.opening_hours

import de.westnordost.osm_opening_hours.model.ClockTime
import de.westnordost.osm_opening_hours.model.ExtendedClockTime
import de.westnordost.osm_opening_hours.model.Holiday
import de.westnordost.osm_opening_hours.model.HolidaySelector
import de.westnordost.osm_opening_hours.model.HolidayWithOffset
import de.westnordost.osm_opening_hours.model.Month
import de.westnordost.osm_opening_hours.model.MonthRange
import de.westnordost.osm_opening_hours.model.MonthsOrDateSelector
import de.westnordost.osm_opening_hours.model.SingleMonth
import de.westnordost.osm_opening_hours.model.SpecificWeekdays
import de.westnordost.osm_opening_hours.model.StartingAtTime
import de.westnordost.osm_opening_hours.model.TimeIntervals
import de.westnordost.osm_opening_hours.model.TimeSpan
import de.westnordost.osm_opening_hours.model.VariableTime
import de.westnordost.osm_opening_hours.model.Weekday
import de.westnordost.osm_opening_hours.model.WeekdayRange
import de.westnordost.osm_opening_hours.model.WeekdaysSelector

fun Iterable<HolidaySelector>.getHolidays(): List<Holiday> {
    val holidays = ArrayList<Holiday>()
    for (selector in this) {
        when (selector) {
            is Holiday -> holidays.add(selector)
            is HolidayWithOffset -> throw UnsupportedOperationException()
        }
    }
    return holidays
}

fun Iterable<WeekdaysSelector>.getWeekdays(): Set<Weekday> {
    val weekdays = HashSet<Weekday>()
    for (selector in this) {
        when (selector) {
            is Weekday -> weekdays.add(selector)
            is WeekdayRange -> weekdays.addAll(selector.toWeekdays())
            is SpecificWeekdays -> throw UnsupportedOperationException()
        }
    }
    return weekdays
}

private fun WeekdayRange.toWeekdays(): List<Weekday> {
    val start = start.ordinal
    val end = end.ordinal
    val range =
        if (end >= start) start .. end
        else (start ..< Weekday.entries.size) + (0 .. end)
    return range.map { Weekday.entries[it] }
}

fun Set<Weekday>.toWeekdaysSelectors(): List<WeekdaysSelector> =
    toOrdinalRanges(Weekday.entries).flatMap {
        val start = it.start
        val end = it.endInclusive
        if (start == end) {
            listOf(Weekday.entries[start])
        } else if (start + 1 == end || start == Weekday.entries.lastIndex && end == 0) {
            listOf(Weekday.entries[start], Weekday.entries[end])
        } else {
            listOf(WeekdayRange(Weekday.entries[start], Weekday.entries[end]))
        }
    }


fun Iterable<MonthsOrDateSelector>.getMonths(): Set<Month> {
    val months = HashSet<Month>()
    for (selector in this) {
        when (selector) {
            is SingleMonth -> {
                if (selector.year != null) throw UnsupportedOperationException()
                months.add(selector.month)
            }
            is MonthRange -> {
                if (selector.year != null) throw UnsupportedOperationException()
                months.addAll(selector.toMonths())
            }
            else -> throw UnsupportedOperationException()
        }
    }
    return months
}

private fun MonthRange.toMonths(): List<Month> {
    val start = start.ordinal
    val end = end.ordinal
    val range =
        if (end >= start) start .. end
        else (start ..< Month.entries.size) + (0 .. end)
    return range.map { Month.entries[it] }
}


fun Set<Month>.toMonthsSelectors(): List<MonthsOrDateSelector> =
    toOrdinalRanges(Month.entries).flatMap {
        val start = it.start
        val end = it.endInclusive
        if (start == end) {
            listOf(SingleMonth(Month.entries[start]))
        } else if (start + 1 == end || start == Month.entries.lastIndex && end == 0) {
            listOf(SingleMonth(Month.entries[start]), SingleMonth(Month.entries[end]))
        } else {
            listOf(MonthRange(Month.entries[start], Month.entries[end]))
        }
    }


/** returns the index ranges at which the items in this set are present in the given list of
 *  [entries]. The ranges may loop around the [entries]' last item.
 *  E.g.
 *
 *  `setOf(a,b,d).toOrdinalRanges(listOf(a,b,c,d,e))` returns `listOf(0..1, 3..3)`
 *
 *  `setOf(a,b,e).toOrdinalRanges(listOf(a,b,c,d,e))` returns `listOf(4..2)` (loops around) */
internal fun <T> Set<T>.toOrdinalRanges(entries: List<T>): List<IntRange> {
    val ranges = ArrayList<IntRange>()
    var start = -1
    var end = -1
    for ((i, entry) in entries.withIndex()) {
        if (entry in this) {
            if (start == -1) start = i
            end = i
        } else {
            if (start != -1 && end != -1) {
                ranges.add(start..end)
            }
            start = -1
            end = -1
        }
    }
    if (start != -1 && end != -1) {
        ranges.add(start..end)
    }
    // merge if looping over end of week
    if (ranges.size >= 2) {
        if (ranges.first().start == 0 && ranges.last().endInclusive == entries.lastIndex) {
            val loopingRange = ranges.last().start .. ranges.first().endInclusive
            ranges.removeLast()
            ranges.removeFirst()
            ranges.add(0, loopingRange)
        }
    }
    return ranges
}

/** Return last clock time of the very last time in this opening hours, if available */
internal fun HierarchicOpeningHours.getLastClockTime(): ClockTime? {
    val lastTimeSelector = (
        monthsList.lastOrNull()
            ?.weekdaysList?.lastOrNull()
            ?.times as? Times
        )?.selectors?.lastOrNull()
        ?: return null

    val lastTime = when (lastTimeSelector) {
        is ClockTime -> lastTimeSelector
        is VariableTime -> null
        is TimeIntervals -> lastTimeSelector.end
        is StartingAtTime -> null
        is TimeSpan -> lastTimeSelector.end
    } ?: return null

    val lastClockTime = when (lastTime) {
        is ExtendedClockTime -> ClockTime(lastTime.hour % 24, lastTime.minutes)
        is ClockTime -> lastTime
        is VariableTime -> null
    }
    return lastClockTime
}
