package de.westnordost.streetcomplete.quests.opening_hours.model

import ch.poole.openinghoursparser.Holiday
import ch.poole.openinghoursparser.Rule
import ch.poole.openinghoursparser.Util
import ch.poole.openinghoursparser.WeekDayRange

data class OpeningHoursRuleList(val rules: List<Rule>) {
    override fun toString(): String = Util.rulesToOpeningHoursString(rules)
}

data class WeekDayRangesAndHolidays(
    val weekdayRanges: List<WeekDayRange>? = null,
    val holidays: List<Holiday>? = null
)