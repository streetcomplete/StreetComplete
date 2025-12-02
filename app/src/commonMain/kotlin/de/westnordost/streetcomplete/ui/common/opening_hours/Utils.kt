package de.westnordost.streetcomplete.ui.common.opening_hours

import de.westnordost.osm_opening_hours.model.Month
import de.westnordost.osm_opening_hours.model.MonthRange
import de.westnordost.osm_opening_hours.model.MonthsOrDateSelector
import de.westnordost.osm_opening_hours.model.SingleMonth
import de.westnordost.osm_opening_hours.model.SpecificWeekdays
import de.westnordost.osm_opening_hours.model.Weekday
import de.westnordost.osm_opening_hours.model.WeekdayRange
import de.westnordost.osm_opening_hours.model.WeekdaysSelector

/** returns the index ranges at which the items in this set are present in the given list of
 *  [entries]. The ranges may loop around the [entries]' last item.
 *  E.g.
 *
 *  `setOf(a,b,d).toOrdinalRanges(listOf(a,b,c,d,e))` returns `listOf(0..1, 3..3)`
 *
 *  `setOf(a,b,e).toOrdinalRanges(listOf(a,b,c,d,e))` returns `listOf(4..2)` (loops around) */
fun <T> Set<T>.toOrdinalRanges(entries: List<T>): List<IntRange> {
    val ranges = ArrayList<IntRange>()
    var start = -1
    var end = -1
    for ((i, entry) in entries.withIndex()) {
        if (entry in this) {
            if (start == -1) start = i
            end = i
        } else {
            if (start != -1 && end != -1) {
                // merge if looping over end of week
                val isAtEnd = end == entries.lastIndex
                val firstRange = ranges.firstOrNull()
                if (isAtEnd && firstRange != null && firstRange.start == 0) {
                    ranges[0] = start..firstRange.endInclusive
                } else {
                    ranges.add(start..end)
                }
            }
            start = -1
            end = -1
        }
    }
    return ranges
}


fun Iterable<MonthsOrDateSelector>.getSelectedMonths(): List<Month> {
    val months = ArrayList<Month>()
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
    val x =
        if (end >= start) start .. end
        else (end ..< Month.entries.size) + (0 .. start)
    return x.map { Month.entries[it] }
}


fun Iterable<WeekdaysSelector>.getSelectedWeekdays(): List<Weekday> {
    val weekdays = ArrayList<Weekday>()
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
    val x =
        if (end >= start) start .. end
        else (end ..< Weekday.entries.size) + (0 .. start)
    return x.map { Weekday.entries[it] }
}
