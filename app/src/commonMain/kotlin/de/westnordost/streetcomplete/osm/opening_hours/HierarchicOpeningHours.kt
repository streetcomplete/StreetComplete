package de.westnordost.streetcomplete.osm.opening_hours

import de.westnordost.osm_opening_hours.model.HolidaySelector
import de.westnordost.osm_opening_hours.model.MonthsOrDateSelector
import de.westnordost.osm_opening_hours.model.TimesSelector
import de.westnordost.osm_opening_hours.model.WeekdaysSelector

/**
 * Opening hours model that is hierarchical:
 * A list of months contains a list of weekdays which each contain a list of times.
 *
 * For example:
 * ```
 * January - February, November:
 *     Monday, Tuesday, Thursday:
 *         08:00 - 14:00
 *         14:30 - 18:00
 *     Saturday:
 *         off
 *     Sunday, Public Holidays:
 *         10:00 - 12:00
 * ```
 */
data class HierarchicOpeningHours(
    val monthsList: List<Months>
)

data class Months(
    val monthsSelector: List<MonthsOrDateSelector>,
    val weekdaysList: List<Weekdays>,
    val offDaysList: List<OffWeekdays>
)

data class Weekdays(
    val weekdaysSelector: List<WeekdaysSelector>,
    val holidaysSelector: List<HolidaySelector>,
    val timesList: List<TimesSelector>
)

data class OffWeekdays(
    val weekdaysSelector: List<WeekdaysSelector>,
    val holidaysSelector: List<HolidaySelector>
)
