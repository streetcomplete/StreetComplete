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
 * January - February, November
 *     Monday, Tuesday, Thursday
 *         08:00 - 14:00
 *         14:30 - 18:00
 *     Saturday:
 *         off
 *     Sunday, Public Holidays
 *         10:00 - 12:00
 * ```
 */
data class HierarchicOpeningHours(
    val monthsList: List<Months>
) {
    constructor() : this(listOf(Months(emptyList(), emptyList())))

    fun isComplete(): Boolean =
        monthsList.isNotEmpty() && monthsList.all { it.isComplete() }
        // if any months are defined, it is required to specify months for all to
        // remove ambiguity (#6175)
        && (monthsList.all { it.selectors.isEmpty() } || monthsList.none { it.selectors.isEmpty() })
}

data class Months(
    val selectors: List<MonthsOrDateSelector>,
    val weekdaysList: List<Weekdays>
) {
    fun isComplete(): Boolean =
        weekdaysList.isNotEmpty() && weekdaysList.all { it.isComplete() }
}

data class Weekdays(
    val weekdaysSelectors: List<WeekdaysSelector>,
    val holidaysSelectors: List<HolidaySelector>,
    val times: WeekdaysContent
) {
    fun isComplete(): Boolean = times.isComplete()
}

sealed interface WeekdaysContent {
    fun isComplete(): Boolean
}
data object Off : WeekdaysContent {
    override fun isComplete(): Boolean = true
}
data class Times(val selectors: List<TimesSelector>) : WeekdaysContent {
    override fun isComplete(): Boolean = selectors.isNotEmpty()
}

