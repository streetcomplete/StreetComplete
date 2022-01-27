package de.westnordost.streetcomplete.osm.opening_hours.parser

import ch.poole.openinghoursparser.Holiday
import ch.poole.openinghoursparser.Rule
import ch.poole.openinghoursparser.Util
import ch.poole.openinghoursparser.WeekDayRange

/* intermediate data structures for opening hours parsing and writing */

data class OpeningHoursRuleList(val rules: List<Rule>) {
    override fun toString(): String = Util.rulesToOpeningHoursString(rules)
}

data class WeekDayRangesAndHolidays(
    val weekdayRanges: List<WeekDayRange>? = null,
    val holidays: List<Holiday>? = null
)
