package de.westnordost.streetcomplete.osm.opening_hours

import de.westnordost.osm_opening_hours.model.OpeningHours
import de.westnordost.osm_opening_hours.model.Range
import de.westnordost.osm_opening_hours.model.RuleType
import de.westnordost.osm_opening_hours.model.TimesSelector
import de.westnordost.streetcomplete.util.ktx.asSequenceOfPairs

/** returns null if the list of rules cannot be displayed by the opening hours widget */
fun OpeningHours.toHierarchicOpeningHours(
    allowTimePoints: Boolean = false
): HierarchicOpeningHours? {
    if (rules.isEmpty()) {
        return null
    }
    if (!isSupported(allowTimePoints)) {
        // parsable, but not supported by StreetComplete
        return null
    }
    val result = mutableListOf<Months>()
    var currentWeekdaysList = mutableListOf<Weekdays>()
    var currentTimeSelectors = mutableListOf<TimesSelector>()

    for ((rule, nextRule) in (rules + null).asSequenceOfPairs()) {
        val nextRange = nextRule?.let { it.selector as Range }

        checkNotNull(rule)
        val range = rule.selector as Range
        val isClosed = rule.ruleType?.isClosed == true

        currentTimeSelectors.addAll(range.times.orEmpty())

        if (
            nextRange == null ||
            nextRange.months != range.months ||
            nextRange.weekdays != range.weekdays ||
            nextRange.holidays != range.holidays ||
            isClosed
        ) {
            currentWeekdaysList.add(Weekdays(
                weekdaysSelectors = range.weekdays.orEmpty(),
                holidaysSelectors = range.holidays.orEmpty(),
                times = if (isClosed) Off else Times(currentTimeSelectors)
            ))
            currentTimeSelectors = mutableListOf()
        }
        if (
            nextRange == null ||
            nextRange.months != range.months
        ) {
            result.add(Months(
                selectors = range.months.orEmpty(),
                weekdaysList = currentWeekdaysList
            ))
            currentWeekdaysList = mutableListOf()
        }
    }

    return HierarchicOpeningHours(result)
}

private val RuleType.isClosed: Boolean get() =
    this == RuleType.Closed || this == RuleType.Off
