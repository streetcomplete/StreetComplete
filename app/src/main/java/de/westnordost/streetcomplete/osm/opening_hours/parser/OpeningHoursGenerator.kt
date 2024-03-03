package de.westnordost.streetcomplete.osm.opening_hours.parser

import de.westnordost.osm_opening_hours.model.ClockTime
import de.westnordost.osm_opening_hours.model.ExtendedClockTime
import de.westnordost.osm_opening_hours.model.Holiday
import de.westnordost.osm_opening_hours.model.Month
import de.westnordost.osm_opening_hours.model.MonthRange
import de.westnordost.osm_opening_hours.model.MonthsOrDateSelector
import de.westnordost.osm_opening_hours.model.OpeningHours
import de.westnordost.osm_opening_hours.model.Range
import de.westnordost.osm_opening_hours.model.Rule
import de.westnordost.osm_opening_hours.model.RuleOperator
import de.westnordost.osm_opening_hours.model.RuleType
import de.westnordost.osm_opening_hours.model.SingleMonth
import de.westnordost.osm_opening_hours.model.StartingAtTime
import de.westnordost.osm_opening_hours.model.Time
import de.westnordost.osm_opening_hours.model.TimeSpan
import de.westnordost.osm_opening_hours.model.TimeSpansSelector
import de.westnordost.osm_opening_hours.model.TimesSelector
import de.westnordost.osm_opening_hours.model.Weekday
import de.westnordost.osm_opening_hours.model.WeekdayRange
import de.westnordost.osm_opening_hours.model.WeekdaysSelector
import de.westnordost.streetcomplete.osm.opening_hours.model.CircularSection
import de.westnordost.streetcomplete.osm.opening_hours.model.Months
import de.westnordost.streetcomplete.osm.opening_hours.model.TimeRange
import de.westnordost.streetcomplete.osm.opening_hours.model.Weekdays
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OffDaysRow
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningHoursRow
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningMonthsRow
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningWeekdaysRow
import de.westnordost.streetcomplete.quests.postbox_collection_times.CollectionTimesRow

@JvmName("openingHoursRowsToOpeningHours")
fun List<OpeningHoursRow>.toOpeningHours(): OpeningHours {
    val rules = mutableListOf<Rule>()

    var currentMonths: List<MonthsOrDateSelector>? = null
    var currentWds: WeekdaysAndHolidays? = null
    var currentTimeSpans: MutableList<TimeSpansSelector> = mutableListOf()

    for (row in this) {
        when (row) {
            is OpeningMonthsRow -> {
                // new rule if we were constructing one
                if (currentWds != null) {
                    rules.add(createRule(currentMonths, currentWds, currentTimeSpans))
                    currentWds = null
                    currentTimeSpans = mutableListOf()
                }

                currentMonths = row.months.toMonthsSelectors()
            }
            is OpeningWeekdaysRow -> {
                val wds =
                    if (!row.weekdays.isSelectionEmpty()) {
                        row.weekdays.toWeekdaysAndHolidays()
                    } else {
                        WeekdaysAndHolidays(null, null)
                    }

                // new weekdays -> new rule
                if (currentWds != null && wds != currentWds) {
                    rules.add(createRule(currentMonths, currentWds, currentTimeSpans))
                    currentTimeSpans = mutableListOf()
                }

                currentTimeSpans.add(row.timeRange.toTimeSpansSelector())
                currentWds = wds
            }
            is OffDaysRow -> {
                // new rule if we were constructing one
                if (currentWds != null) {
                    rules.add(createRule(currentMonths, currentWds, currentTimeSpans))
                    currentWds = null
                    currentTimeSpans = mutableListOf()
                }

                val wds = row.weekdays.toWeekdaysAndHolidays()
                rules.add(createRule(currentMonths, wds, null, RuleType.Off))
            }
        }
    }
    if (currentWds != null) {
        rules.add(createRule(currentMonths, currentWds, currentTimeSpans))
    }

    return OpeningHours(rules.asNonColliding())
}

@JvmName("collectionTimesRowsToOpeningHours")
fun List<CollectionTimesRow>.toOpeningHours(): OpeningHours {
    val rules = mutableListOf<Rule>()

    var currentWds: WeekdaysAndHolidays? = null
    var currentTimes: MutableList<Time> = mutableListOf()

    for (row in this) {
        val wds =
            if (!row.weekdays.isSelectionEmpty()) {
                row.weekdays.toWeekdaysAndHolidays()
            } else {
                WeekdaysAndHolidays(null, null)
            }

        // new weekdays -> new rule
        if (currentWds != null && wds != currentWds) {
            rules.add(createRule(null, currentWds, currentTimes))
            currentTimes = mutableListOf()
        }

        currentTimes.add(row.time.toClockTime())
        currentWds = wds
    }
    if (currentWds != null) {
        rules.add(createRule(null, currentWds, currentTimes))
    }

    return OpeningHours(rules.asNonColliding())
}

/* if any rule collides with another, e.g. "Mo-Fr 10:00-12:00; We 14:00-16:00", switch to
   additive rules e.g. "Mo-Fr 10:00-12:00, We 14:00-16:00" */
private fun List<Rule>.asNonColliding(): List<Rule> =
    if (!hasCollidingWeekdays()) {
        this
    } else {
        map { rule ->
            // "off" rules stay non-additive
            if (rule.ruleType == RuleType.Off) {
                rule
            } else {
                rule.copy(ruleOperator = RuleOperator.Additional)
            }
        }
    }

private fun createRule(
    months: List<MonthsOrDateSelector>?,
    weekdaysAndHolidays: WeekdaysAndHolidays?,
    times: List<TimesSelector>?,
    ruleType: RuleType? = null
) = Rule(
    Range(
        months = months,
        weekdays = weekdaysAndHolidays?.weekdays,
        holidays = weekdaysAndHolidays?.holidays,
        times = times
    ),
    ruleType = ruleType
)

private fun Months.toMonthsSelectors(): List<MonthsOrDateSelector> =
    toCircularSections().map { it.toMonthsSelector() }

private fun CircularSection.toMonthsSelector(): MonthsOrDateSelector {
    val start = Month.entries[start]
    val end = Month.entries[end]
    return if (start != end) MonthRange(start, end) else SingleMonth(start)
}

private fun Weekdays.toWeekdaysAndHolidays(): WeekdaysAndHolidays {
    val weekdays = toCircularSections().flatMap { it.toWeekdayAndHolidaySelectors() }
    val holidays = if (selection[Weekdays.PUBLIC_HOLIDAY]) listOf(Holiday.PublicHoliday) else null
    return WeekdaysAndHolidays(weekdays, holidays)
}

private fun CircularSection.toWeekdayAndHolidaySelectors(): List<WeekdaysSelector> {
    val s = Weekday.entries[start]
    val e = Weekday.entries[end]
    return when {
        start == end -> listOf(s)
        (start + 1) % Weekday.entries.size == end -> listOf(s, e) // Mo,Tu better readable than Mo-Tu
        else -> listOf(WeekdayRange(s, e))
    }
}

private fun TimeRange.toTimeSpansSelector(): TimeSpansSelector {
    val startTime = ClockTime(start / 60, start % 60)
    val endTime = ExtendedClockTime(end / 60, end % 60)

    return if (start == end && isOpenEnded) {
        StartingAtTime(startTime)
    } else {
        TimeSpan(startTime, endTime, isOpenEnded)
    }
}

private fun Int.toClockTime() = ClockTime(this / 60, this % 60)
