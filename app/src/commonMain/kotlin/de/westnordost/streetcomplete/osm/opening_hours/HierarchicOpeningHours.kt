package de.westnordost.streetcomplete.osm.opening_hours

import de.westnordost.osm_opening_hours.model.HolidaySelector
import de.westnordost.osm_opening_hours.model.MonthsOrDateSelector
import de.westnordost.osm_opening_hours.model.TimesSelector
import de.westnordost.osm_opening_hours.model.WeekdaysSelector
import de.westnordost.streetcomplete.ApplicationConstants.MAX_OSM_TAG_VALUE_LENGTH
import de.westnordost.streetcomplete.util.serialization.HierarchicOpeningHoursSerializer
import kotlinx.serialization.Serializable

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
@Serializable(with = HierarchicOpeningHoursSerializer::class)
data class HierarchicOpeningHours(
    val monthsList: List<Months>
) {
    constructor() : this(listOf(Months(emptyList(), emptyList())))

    fun isComplete(): Boolean =
        monthsList.isNotEmpty() && monthsList.all { it.isComplete() }
        // if any months are defined, it is required to specify months for all to
        // remove ambiguity (#6175)
        && (monthsList.all { it.selectors.isEmpty() } || monthsList.none { it.selectors.isEmpty() })

    fun isEmpty(): Boolean =
        monthsList.all { it.isEmpty() }

    fun isTooLong(): Boolean =
        toOpeningHours().toString().length > MAX_OSM_TAG_VALUE_LENGTH
}

data class Months(
    val selectors: List<MonthsOrDateSelector>,
    val weekdaysList: List<Weekdays>
) {
    fun isComplete(): Boolean =
        weekdaysList.isNotEmpty() && weekdaysList.all { it.isComplete() }

    fun isEmpty(): Boolean =
        selectors.isEmpty() && weekdaysList.all { it.isEmpty() }
}

data class Weekdays(
    val weekdaysSelectors: List<WeekdaysSelector>,
    val holidaysSelectors: List<HolidaySelector>,
    val times: WeekdaysContent
) {
    fun isComplete(): Boolean = times.isComplete()

    fun isEmpty(): Boolean =
        weekdaysSelectors.isEmpty() && holidaysSelectors.isEmpty() && times.isEmpty()
}

sealed interface WeekdaysContent {
    fun isComplete(): Boolean
    fun isEmpty(): Boolean
}
data object Off : WeekdaysContent {
    override fun isComplete(): Boolean = true
    override fun isEmpty(): Boolean = false
}
data class Times(val selectors: List<TimesSelector>) : WeekdaysContent {
    override fun isComplete(): Boolean = selectors.isNotEmpty()
    override fun isEmpty(): Boolean = selectors.isEmpty()
}
