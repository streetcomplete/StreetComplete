package de.westnordost.streetcomplete.osm.opening_hours

import de.westnordost.osm_opening_hours.model.OpeningHours
import de.westnordost.osm_opening_hours.model.Range
import de.westnordost.osm_opening_hours.model.Rule
import de.westnordost.osm_opening_hours.model.RuleType
import de.westnordost.osm_opening_hours.model.TimesSelector

/** returns null if the list of rules cannot be displayed by the opening hours widget */
fun OpeningHours.toHierarchicOpeningHours(
    allowTimePoints: Boolean = false
): HierarchicOpeningHours? {
    if (!isSupportedOpeningHours(allowTimePoints)) {
        // parsable, but not supported by StreetComplete
        return null
    }
    val result = mutableListOf<Months>()
    var currentWeekdaysList = mutableListOf<Weekdays>()
    var currentTimeSelectors = mutableListOf<TimesSelector>()

    // we add an empty rule at the end because the loop below only fills the result list with each
    // the previous rule in relation to the current rule
    var previousRule: Rule? = null
    for (rule in rules + Rule(Range())) {
        val range = rule.selector as Range

        val weekdays = range.weekdays.orEmpty()
        val holidays = range.holidays.orEmpty()
        val months = range.months.orEmpty()

        if (previousRule != null) {
            val previousRange = previousRule.selector as Range
            val previousWeekdays = previousRange.weekdays.orEmpty()
            val previousHolidays = previousRange.holidays.orEmpty()
            val previousMonths = previousRange.months.orEmpty()
            val previousIsClosed = previousRule.ruleType?.isClosed == true

            if (
                weekdays != previousWeekdays ||
                holidays != previousHolidays ||
                previousIsClosed
            ) {
                currentWeekdaysList.add(Weekdays(
                    weekdaysSelectors = previousWeekdays,
                    holidaysSelectors = previousHolidays,
                    times = if (previousIsClosed) Off else Times(currentTimeSelectors)
                ))
                currentTimeSelectors = mutableListOf()
            }
            if (months != previousMonths) {
                result.add(Months(
                    selectors = previousRange.months.orEmpty(),
                    weekdaysList = currentWeekdaysList
                ))
                currentWeekdaysList = mutableListOf()
            }
        }

        currentTimeSelectors.addAll(range.times.orEmpty())

        previousRule = rule
    }

    return HierarchicOpeningHours(result)
}

private val RuleType.isClosed: Boolean get() =
    this == RuleType.Closed || this == RuleType.Off
