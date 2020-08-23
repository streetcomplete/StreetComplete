package de.westnordost.streetcomplete.quests.opening_hours.parser

import ch.poole.openinghoursparser.*
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningHoursRow
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningMonthsRow
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningWeekdaysRow
import de.westnordost.streetcomplete.quests.opening_hours.model.*
import de.westnordost.streetcomplete.quests.opening_hours.model.Weekdays.Companion.PUBLIC_HOLIDAY

@JvmName("openingHoursRowsToOpeningHoursRules")
fun List<OpeningHoursRow>.toOpeningHoursRules(): OpeningHoursRuleList {
    val rules = mutableListOf<Rule>()

    var currentDateRange: DateRange? = null
    var currentWeekdays: WeekDayRangesAndHolidays? = null
    var currentTimeSpans: MutableList<TimeSpan> = mutableListOf()

    for (row in this) {
        if (row is OpeningMonthsRow) {
            val dateRange = row.months?.toDateRange()

            // new rule if we were constructing one
            if (currentWeekdays != null) {
                rules.add(createRule(currentDateRange, currentWeekdays.weekdayRanges, currentWeekdays.holidays, currentTimeSpans))
                currentWeekdays = null
                currentTimeSpans = mutableListOf()
            }

            currentDateRange = dateRange
        } else if (row is OpeningWeekdaysRow) {
            val timeSpan = row.timeRange.toTimeSpan()
            val weekdays = row.weekdays?.toWeekDayRangesAndHolidays() ?: WeekDayRangesAndHolidays()

            // new weekdays -> new rule
            if (currentWeekdays != null && weekdays != currentWeekdays) {
                rules.add(createRule(currentDateRange, currentWeekdays.weekdayRanges, currentWeekdays.holidays, currentTimeSpans))
                currentTimeSpans = mutableListOf()
            }

            currentTimeSpans.add(timeSpan)
            currentWeekdays = weekdays
        }
    }
    if (currentWeekdays != null) {
        rules.add(createRule(currentDateRange, currentWeekdays.weekdayRanges, currentWeekdays.holidays, currentTimeSpans))
    }

    // if any rule collides with another, f.e. "Mo-Fr 10:00-12:00; We 14:00-16:00", switch to
    // additive rules f.e. "Mo-Fr 10:00-12:00, We 14:00-16:00"
    if (rules.collidesWithItself()) {
        for (rule in rules) {
            rule.isAdditive = true
        }
    }

    return OpeningHoursRuleList(rules)
}

private fun createRule(
    dateRange: DateRange?,
    weekDayRanges: List<WeekDayRange>?,
    holidays: List<Holiday>?,
    timeSpans: List<TimeSpan>) = Rule().also { r ->

    require(timeSpans.isNotEmpty())

    r.dates = dateRange?.let { mutableListOf(it) }
    r.days = weekDayRanges?.toMutableList()
    r.holidays = holidays?.toMutableList()
    r.times = timeSpans.toMutableList()
}

private fun TimeRange.toTimeSpan() = TimeSpan().also {
    it.start = start
    val tEnd = if (end == 0) 24 * 60 else end
    if (start != tEnd) it.end = tEnd
    it.isOpenEnded = isOpenEnded
}

private fun Weekdays.toWeekDayRangesAndHolidays(): WeekDayRangesAndHolidays {
    val weekdayRanges = toCircularSections().flatMap { it.toWeekDayRanges() }

    val holidays: List<Holiday>? =
        if (selection[PUBLIC_HOLIDAY]) listOf(Holiday().also { it.type = Holiday.Type.PH })
        else null

    return WeekDayRangesAndHolidays(weekdayRanges.takeIf { it.isNotEmpty() }, holidays)
}

private fun CircularSection.toWeekDayRanges(): List<WeekDayRange> {
    val size = NumberSystem(0, 6).getSize(this)
    // if the range is very short (f.e. Mo-Tu), rather save it as Mo,Tu
    return if (size == 2) {
        listOf(
            WeekDayRange().also { it.startDay = WeekDay.values()[start] },
            WeekDayRange().also { it.startDay = WeekDay.values()[end] }
        )
    } else {
        listOf(WeekDayRange().also {
            it.startDay = WeekDay.values()[start]
            it.endDay = if (start != end) WeekDay.values()[end] else null
        })
    }
}

private fun CircularSection.toDateRange() = DateRange().also {
    it.startDate = createMonthDate(Month.values()[start])
    it.endDate = if (start != end) createMonthDate(Month.values()[end]) else null
}

private fun createMonthDate(month: Month) = DateWithOffset().also { it.month = month }