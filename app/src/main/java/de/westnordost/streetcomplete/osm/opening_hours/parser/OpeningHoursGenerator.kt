package de.westnordost.streetcomplete.osm.opening_hours.parser

import ch.poole.openinghoursparser.DateRange
import ch.poole.openinghoursparser.DateWithOffset
import ch.poole.openinghoursparser.Holiday
import ch.poole.openinghoursparser.Month
import ch.poole.openinghoursparser.Rule
import ch.poole.openinghoursparser.RuleModifier
import ch.poole.openinghoursparser.TimeSpan
import ch.poole.openinghoursparser.WeekDay
import ch.poole.openinghoursparser.WeekDayRange
import de.westnordost.streetcomplete.osm.opening_hours.model.CircularSection
import de.westnordost.streetcomplete.osm.opening_hours.model.Months
import de.westnordost.streetcomplete.osm.opening_hours.model.NumberSystem
import de.westnordost.streetcomplete.osm.opening_hours.model.TimeRange
import de.westnordost.streetcomplete.osm.opening_hours.model.Weekdays
import de.westnordost.streetcomplete.osm.opening_hours.model.Weekdays.Companion.PUBLIC_HOLIDAY
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OffDaysRow
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningHoursRow
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningMonthsRow
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningWeekdaysRow
import de.westnordost.streetcomplete.quests.postbox_collection_times.CollectionTimesRow

@JvmName("openingHoursRowsToOpeningHoursRules")
fun List<OpeningHoursRow>.toOpeningHoursRules(): OpeningHoursRuleList {
    val rules = mutableListOf<Rule>()

    var currentDateRanges: List<DateRange>? = null
    var currentWeekdays: WeekDayRangesAndHolidays? = null
    var currentTimeSpans: MutableList<TimeSpan> = mutableListOf()

    for (row in this) when (row) {
        is OpeningMonthsRow -> {
            val dateRanges = row.months.toDateRanges()

            // new rule if we were constructing one
            if (currentWeekdays != null) {
                rules.add(createRule(currentDateRanges, currentWeekdays.weekdayRanges, currentWeekdays.holidays, currentTimeSpans))
                currentWeekdays = null
                currentTimeSpans = mutableListOf()
            }

            currentDateRanges = dateRanges
        }
        is OpeningWeekdaysRow -> {
            val timeSpan = row.timeRange.toTimeSpan()
            val weekdays =
                if (!row.weekdays.isSelectionEmpty()) {
                    row.weekdays.toWeekDayRangesAndHolidays()
                } else {
                    WeekDayRangesAndHolidays()
                }

            // new weekdays -> new rule
            if (currentWeekdays != null && weekdays != currentWeekdays) {
                rules.add(createRule(currentDateRanges, currentWeekdays.weekdayRanges, currentWeekdays.holidays, currentTimeSpans))
                currentTimeSpans = mutableListOf()
            }

            currentTimeSpans.add(timeSpan)
            currentWeekdays = weekdays
        }
        is OffDaysRow -> {
            // new rule if we were constructing one
            if (currentWeekdays != null) {
                rules.add(createRule(currentDateRanges, currentWeekdays.weekdayRanges, currentWeekdays.holidays, currentTimeSpans))
                currentWeekdays = null
                currentTimeSpans = mutableListOf()
            }

            val weekdays = row.weekdays.toWeekDayRangesAndHolidays()
            rules.add(createOffRule(currentDateRanges, weekdays.weekdayRanges, weekdays.holidays))
        }
    }
    if (currentWeekdays != null) {
        rules.add(createRule(currentDateRanges, currentWeekdays.weekdayRanges, currentWeekdays.holidays, currentTimeSpans))
    }

    /* if any rule collides with another, e.g. "Mo-Fr 10:00-12:00; We 14:00-16:00", switch to
       additive rules e.g. "Mo-Fr 10:00-12:00, We 14:00-16:00" */
    if (rules.weekdaysCollideWithAnother()) {
        rules.makeAdditive()
    }

    return OpeningHoursRuleList(rules)
}

@JvmName("makeAdditiveInRuleList")
private fun List<Rule>.makeAdditive() {
    for (rule in this) {
        // "off" rules stay non-additive
        if (rule.modifier?.isSimpleOff() != true) {
            rule.isAdditive = true
        }
    }
}

@JvmName("collectionTimesRowsToOpeningHoursRules")
fun List<CollectionTimesRow>.toOpeningHoursRules(): OpeningHoursRuleList {
    val rules = mutableListOf<Rule>()

    var currentWeekdays: WeekDayRangesAndHolidays? = null
    var currentTimeSpans: MutableList<TimeSpan> = mutableListOf()

    for (row in this) {
        val time = row.time
        val weekdays =
            if (!row.weekdays.isSelectionEmpty()) {
                row.weekdays.toWeekDayRangesAndHolidays()
            } else {
                WeekDayRangesAndHolidays()
            }

        // new weekdays -> new rule
        if (currentWeekdays != null && weekdays != currentWeekdays) {
            rules.add(createRule(null, currentWeekdays.weekdayRanges, currentWeekdays.holidays, currentTimeSpans))
            currentTimeSpans = mutableListOf()
        }

        currentTimeSpans.add(time.toTimeSpan())
        currentWeekdays = weekdays
    }
    if (currentWeekdays != null) {
        rules.add(createRule(null, currentWeekdays.weekdayRanges, currentWeekdays.holidays, currentTimeSpans))
    }

    /* if any rule collides with another, e.g. "Mo-Fr 10:00; We 14:00", switch to
       additive rules e.g. "Mo-Fr 10:00, We 14:00" */
    if (rules.weekdaysCollideWithAnother()) {
        rules.makeAdditive()
    }

    return OpeningHoursRuleList(rules)
}

private fun createRule(
    dateRanges: List<DateRange>?,
    weekDayRanges: List<WeekDayRange>?,
    holidays: List<Holiday>?,
    timeSpans: List<TimeSpan>
) = Rule().also { r ->

    require(timeSpans.isNotEmpty())

    r.dates = dateRanges?.toMutableList()
    r.days = weekDayRanges?.toMutableList()
    r.holidays = holidays?.toMutableList()
    r.times = timeSpans.toMutableList()
}

private fun createOffRule(
    dateRanges: List<DateRange>?,
    weekDayRanges: List<WeekDayRange>?,
    holidays: List<Holiday>?
) = Rule().also { r ->

    r.dates = dateRanges?.toMutableList()
    r.days = weekDayRanges?.toMutableList()
    r.holidays = holidays?.toMutableList()
    r.modifier = RuleModifier().also { it.modifier = RuleModifier.Modifier.OFF }
}

private fun Int.toTimeSpan() = TimeSpan().also {
    it.start = this
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
        if (selection[PUBLIC_HOLIDAY]) {
            listOf(Holiday().also { it.type = Holiday.Type.PH })
        } else {
            null
        }

    return WeekDayRangesAndHolidays(weekdayRanges.takeIf { it.isNotEmpty() }, holidays)
}

private fun CircularSection.toWeekDayRanges(): List<WeekDayRange> {
    val size = NumberSystem(0, Weekdays.WEEKDAY_COUNT - 1).getSize(this)
    // if the range is very short (e.g. Mo-Tu), rather save it as Mo,Tu
    return if (size == 2) {
        listOf(
            WeekDayRange().also { it.startDay = WeekDay.entries[start] },
            WeekDayRange().also { it.startDay = WeekDay.entries[end] }
        )
    } else {
        listOf(WeekDayRange().also {
            it.startDay = WeekDay.entries[start]
            it.endDay = if (start != end) WeekDay.entries[end] else null
        })
    }
}

private fun Months.toDateRanges(): List<DateRange> {
    return toCircularSections().map { it.toDateRange() }
}

private fun CircularSection.toDateRange() = DateRange().also {
    it.startDate = createMonthDate(Month.entries[start])
    it.endDate = if (start != end) createMonthDate(Month.entries[end]) else null
}

private fun createMonthDate(month: Month) = DateWithOffset().also { it.month = month }
