package de.westnordost.streetcomplete.osm.opening_hours

import de.westnordost.osm_opening_hours.model.Weekday
import de.westnordost.osm_opening_hours.model.Weekday.*
import de.westnordost.osm_opening_hours.model.WeekdaysSelector
import de.westnordost.streetcomplete.util.StringWithCursor

/** Parse this string into a list of Weekday or WeekdaySpan, e.g. "Mo,We-Fr" */
fun String.toWeekdaysSelectors(): List<WeekdaysSelector> =
    try {
        StringWithCursor(this).parseWeekdaySelectors()
    } catch (_: Exception) {
        emptyList()
    }

private fun StringWithCursor.parseWeekdaySelectors(): List<WeekdaysSelector> {
    val result = ArrayList<WeekdaysSelector>()
    do { result.add(parseWeekdaySelector()) } while(nextIsAndAdvance(','))
    return result
}

private fun StringWithCursor.parseWeekdaySelector(): WeekdaysSelector {
    val start = parseWeekday()
    return if (nextIsAndAdvance('-')) {
        val end = parseWeekday()
        start..end
    } else {
        start
    }
}

private fun StringWithCursor.parseWeekday(): Weekday =
    weekdays.getValue(advanceBy(2))

private val weekdays by lazy { mapOf(
    "Mo" to Monday,
    "Tu" to Tuesday,
    "We" to Wednesday,
    "Th" to Thursday,
    "Fr" to Friday,
    "Sa" to Saturday,
    "Su" to Sunday
) }
