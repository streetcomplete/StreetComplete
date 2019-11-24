package de.westnordost.streetcomplete.quests.opening_hours

import ch.poole.openinghoursparser.*
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningMonthsRow
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningWeekdaysRow
import de.westnordost.streetcomplete.quests.opening_hours.model.TimeRange
import de.westnordost.streetcomplete.quests.opening_hours.model.Weekdays
import java.io.ByteArrayInputStream


object OpeningHoursTagParser {
    // returns null for values that are invalid or not representable in
    // StreetComplete opening hours edit widget
    // otherwise returns data structure that can be directly used to
    // initialize this editing widget
    fun parse(openingHours: String): List<OpeningMonthsRow>? {
        val rules: ArrayList<Rule>
        try {
            val input = ByteArrayInputStream(openingHours.toByteArray())
            val parser = OpeningHoursParser(input)
            rules = parser.rules(false)
            for (rule in rules) {
                if(reduceRuleToStreetCompleteSupported(rule) == null) {
                    // parsable, not handled by StreetComplete
                    return null
                }
            }
        } catch (e: ParseException) {
            // parsing failed, value is malformed
            return null
        }


        val data = listOf(OpeningMonthsRow())

        for (rule in rules) {
            val dayData = BooleanArray(7) {false}
            assert(rule.days.size == 1)
            val startDay = rule.days[0].startDay
            val endDay = rule.days[0].endDay ?: startDay // endDay will be null for single day ranges
            assert(startDay <= endDay ) //TODO: add support for not requiring it
            for(day in WeekDay.values()) {
                if(day >= startDay) {
                    if(day <= endDay) {
                        //TODO: check whatever indexing of both is from Monday
                        dayData[day.ordinal] = true
                    }
                }
            }
            assert(rule.times.size == 1) // TODO eliminate this asssert and fix code that requires it
            data[0].weekdaysList.add(OpeningWeekdaysRow(Weekdays(dayData), TimeRange(rule.times[0].start, rule.times[0].end)))
        }

        return data
    }

    // Reduces rule to a subset supported by StreetComplete
    // in case of any info that would be lost it return null
    // null is also returned in cases where conversion would be necessary
    // and there is any risk of loss of any data
    fun reduceRuleToStreetCompleteSupported(rule: Rule): Rule? { // following are ignored:
        val returned = emptyRule()
        if(rule.days == null) {
            return null // SC requires explicit specification of days of a week
        } else {
            val simplifiedWeekDayRanges: MutableList<WeekDayRange> = ArrayList()
            for (weekDayRange in rule.days) {
                val simplifiedDateRange = reduceWeekDayRangeToSimpleDays(weekDayRange) ?: return null
                simplifiedWeekDayRanges.add(simplifiedDateRange)
            }
            if(simplifiedWeekDayRanges.size > 1){
                return null // TODO - how this may happen? Is it representable in SC?
            }
            returned.days = simplifiedWeekDayRanges // copy days of the week from the input rule
        }
        if (rule.dates != null) {
            val simplifiedDateRanges: MutableList<DateRange> = ArrayList()
            for (dateRange in rule.dates) {
                val simplifiedDateRange = reduceDateRangeToFullMonths(dateRange) ?: return null
                simplifiedDateRanges.add(simplifiedDateRange)
            }
            if(simplifiedDateRanges.size > 1){
                return null // TODO - how this may happen? Is it representable in SC?
            }
            // TODO: replace by setDates from https://github.com/simonpoole/OpeningHoursParser/releases/tag/0.17.0 once available
            returned.setMonthdays(simplifiedDateRanges)
        }
        if (rule.times == null) {
            // explicit opening hours are required by SC
            return null
        } else {
            val simplifiedTimespans: ArrayList<TimeSpan> = ArrayList()
            for (time in rule.times) {
                val simplifiedTimespan = reduceTimeRangeToSimpleTime(time) ?: return null
                simplifiedTimespans.add(simplifiedTimespan)
            }
            if(simplifiedTimespans.size > 1){
                return null // TODO - how this may happen? Is it representable in SC?
            }
            returned.times = simplifiedTimespans
        }
        if (rule.modifier != null) {
            // public holidays with "off" specified explicitly are incompatible with SC due to
            // https://github.com/westnordost/StreetComplete/issues/276
            // other opening hours using "off" are rare and would require automated conversion
            // that would drop off part, what may cause issues in weird cases
            if (rule.modifier.modifier != RuleModifier.Modifier.OPEN) {
                return null
            }
        }
        return if (rule == returned) {
            // original rule is representable in SC UI without any loss
            returned
        } else {
            // not representable in SC UI
            null
        }
    }

    // StreetComplete is not supporting offsets, indexing by nth day of week etc
    // function may return identical or modified object or null
    // null or modified object indicates that original object was not representable in SC
    private fun reduceWeekDayRangeToSimpleDays(weekDayRange: WeekDayRange): WeekDayRange? {
        val returned = WeekDayRange()
        if(weekDayRange.startDay == null){
            // invalid range
            return null
        }
        // returned.endDay may be null for range containing just a single day
        returned.endDay = weekDayRange.endDay
        returned.startDay = weekDayRange.startDay
        return returned
    }

    // StreetComplete supports solely date changing based on month
    // without any support for any other data ranges
    // function may return identical or modified object or null
    // null or modified object indicates that original object was not representable in SC
    private fun reduceDateRangeToFullMonths(dateRange: DateRange): DateRange? {
        for (date in arrayOf(dateRange.startDate, dateRange.endDate)) {
            if (date.isOpenEnded) {
                return null //TODO: it may be supported by StreetComplete
            }
            if (date.isWeekDayOffsetPositive) {
                return null
            }
        }
        val newDateRange = DateRange()

        val startDate = DateWithOffset()
        startDate.month = dateRange.startDate.month
        newDateRange.startDate = startDate

        val endDate = DateWithOffset()
        endDate.month = dateRange.endDate.month
        newDateRange.endDate = endDate
        return newDateRange
    }

    // StreetComplete has no support for times like "from sunrise to sunset"
    // this function throws away any info over "from hour X to hour Y"
    // function may return identical or modified object or null
    // null or modified object indicates that original object was not representable in SC
    private fun reduceTimeRangeToSimpleTime(timeSpan: TimeSpan): TimeSpan? {
        val simplifiedTimespan = TimeSpan()
        if (timeSpan.startEvent != null) {
            return null
        }
        if (timeSpan.endEvent != null) {
            return null
        }
        val startInMinutesSinceMidnight = timeSpan.start
        if (startInMinutesSinceMidnight < 0) {
            return null
        }
        if (startInMinutesSinceMidnight > 24 * 60) {
            return null
        }
        simplifiedTimespan.start = startInMinutesSinceMidnight
        val endInMinutesSinceMidnight = timeSpan.end
        if (endInMinutesSinceMidnight < 0) {
            return null
        }
        if (endInMinutesSinceMidnight > 24 * 60) {
            return null
        }
        simplifiedTimespan.end = endInMinutesSinceMidnight
        return simplifiedTimespan
    }

    private fun emptyRule(): Rule {
        // workaround needed to construct empty Rule object
        // proposal to allow creation of Rule objects is at
        // https://github.com/simonpoole/OpeningHoursParser/pull/24
        val input = ByteArrayInputStream("".toByteArray())
        val parser = OpeningHoursParser(input)
        try {
            val rules = parser.rules(true)
            return rules[0]
        } catch (e: ParseException) {
            e.printStackTrace()
            throw RuntimeException()
        }
    }

}
