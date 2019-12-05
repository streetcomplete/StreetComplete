package de.westnordost.streetcomplete.quests.opening_hours

import ch.poole.openinghoursparser.*
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningMonthsRow
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningWeekdaysRow
import de.westnordost.streetcomplete.quests.opening_hours.model.CircularSection
import de.westnordost.streetcomplete.quests.opening_hours.model.OpeningMonths
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
        } catch (e: ParseException) {
            // parsing failed, value is malformed
            return null
        }
        if (!isRulesetToStreetCompleteSupported(rules)) {
            // parsable, not handled by StreetComplete
            return null
        }
        return transformCompatibleRulesetToInternalForm(rules)
    }

    // transforms output of a Vespucci parser (assumed to not caontain weird constructs)
    // into SC internal format
    private fun transformCompatibleRulesetToInternalForm(rules: ArrayList<Rule>): List<OpeningMonthsRow>? {
        var data = mutableListOf(OpeningMonthsRow())
        for (rule in rules) {
            if (rule.dates != null) {
                // month based rules, so we need OpeningMonthsRow objects that will be created
                // and added later rather than a single catch-all row
                data = mutableListOf()
            }
        }

        for (rule in rules) {
            var index = 0
            val dates = rule.dates
            if (dates != null) {
                require(dates.size == 1)
                val start = dates[0].startDate
                val end = dates[0].endDate ?: start
                index = getIndexOfOurMonthsRow(data, start.month.ordinal, end.month.ordinal)
                if (index == -1) {
                    // there is no reusable row matching out entry, we need to create a new one
                    data.add(OpeningMonthsRow(CircularSection(start.month.ordinal, end.month.ordinal)))
                    index = data.size - 1
                }
            }
            for (time in rule.times!!) {
                val dayData = daysWhenRuleApplies(rule)
                if(time.end >= 48 * 50) {
                    return null
                }
                data[index].weekdaysList.add(OpeningWeekdaysRow(Weekdays(dayData), TimeRange(time.start, time.end % (24 * 60))))
            }
        }

        return data
    }

    private fun getIndexOfOurMonthsRow(monthRows: List<OpeningMonthsRow>, startMonth: Int, endMonth: Int): Int {
        for ((index, row) in monthRows.withIndex()) {
            if (row.months.start == startMonth) {
                if (row.months.end == endMonth) {
                    return index
                }
            }
        }
        return -1
    }

    //returns array that can be used to initialize OpeningWeekdaysRow
    private fun daysWhenRuleApplies(rule: Rule): BooleanArray {
        val dayData = BooleanArray(8) { false }
        require(rule.holidays != null || rule.days!!.size >= 0)
        val days = rule.days
        if (days != null) {
            require(days.size == 1)
            val startDay = days[0].startDay
            val endDay = days[0].endDay
                    ?: startDay // endDay will be null for single day ranges
            if (startDay <= endDay) {
                // ranges like Tuesday-Saturday
                for (day in WeekDay.values()) {
                    if (day >= startDay) {
                        if (day <= endDay) {
                            dayData[day.ordinal] = true
                        }
                    }
                }
            } else {
                // ranges like Saturday-Tuesday
                for (day in WeekDay.values()) {
                    if (day <= endDay || day >= startDay) {
                        dayData[day.ordinal] = true
                    }
                }
            }
        }
        val holidays = rule.holidays
        if (holidays != null) {
            require(holidays.size == 1)
            require(holidays[0].type == Holiday.Type.PH)
            require(holidays[0].offset == 0)
            require(holidays[0].useAsWeekDay)
            dayData[7] = true
        }
        return dayData
    }

    // Returns true iff supported by StreetComplete
    // Returns false otherwise, in cases where it is not directly representable
    //
    // It is first checking each rule (parts of opening_hours tag separated by ; sign)
    // is it possible to recreate it by taking only supported parts
    // later it checks also some additional limitations imposed by SC
    private fun isRulesetToStreetCompleteSupported(ruleset: ArrayList<Rule>): Boolean {
        for (rule in ruleset) {
            if (reduceRuleToStreetCompleteSupported(rule) == null) {
                return false
            }
        }
        if (includesMonthsRangeCrossingNewYearBoundary(ruleset)) {
            // strictly speaking this kind of ranges are supported, but not in an obvious way
            return false
        }
        if (areOnlySomeRulesMonthBased(ruleset)) {
            // StreetComplete can handle month based rules, but requires all of them to be month based
            return false
        }
        if (rulesAreOverridingOtherRules(ruleset)) {
            // this kind of opening hours specification likely require fix
            // anyway, it is not representable directly by SC
            return false
        }
        return true
    }

    private fun includesMonthsRangeCrossingNewYearBoundary(ruleset: ArrayList<Rule>): Boolean {
        for (rule in ruleset) {
            val dates = rule.dates
            if (dates != null) {
                require(dates.size == 1)
                val endDate = dates[0].endDate
                if (endDate != null) {
                    if (dates[0].startDate.month > endDate.month) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun rulesAreOverridingOtherRules(ruleset: ArrayList<Rule>): Boolean {
        for (checkedRuleIndex in 0 until ruleset.size) {
            for (competingRuleIndex in 0 until ruleset.size) {
                if (checkedRuleIndex != competingRuleIndex) {
                    if (ruleset[checkedRuleIndex].dates != null) {
                        require(ruleset[competingRuleIndex].dates != null)
                        val checkedRuleDate = ruleset[checkedRuleIndex].dates
                        val competingRuleDate = ruleset[competingRuleIndex].dates
                        require(checkedRuleDate!!.size == 1)
                        require(competingRuleDate!!.size == 1)
                        val firstDateRange = checkedRuleDate[0]
                        val secondDateRange = competingRuleDate[0]
                        if (areMonthRangesIntersecting(firstDateRange, secondDateRange)) {
                            return areDayRangesIntersecting(ruleset[checkedRuleIndex], ruleset[competingRuleIndex])
                        }
                    } else {
                        require(ruleset[competingRuleIndex].dates == null)
                        return areDayRangesIntersecting(ruleset[checkedRuleIndex], ruleset[competingRuleIndex])
                    }
                }
            }
        }
        return false
    }

    private fun areDayRangesIntersecting(ruleA: Rule, ruleB: Rule): Boolean {
        if (areHolidaysIntersecting(ruleA.holidays, ruleB.holidays)) {
            return true
        }
        val daysA = ruleA.days
        val daysB = ruleB.days
        if (daysA == null || daysB == null) {
            return false
        }
        require(daysA.size == 1)
        require(daysB.size == 1)
        val weekDayRangeA = daysA[0]
        val weekDayRangeB = daysB[0]
        val startA = weekDayRangeA.startDay
        val endA = weekDayRangeA.endDay ?: startA
        val startB = weekDayRangeB.startDay
        val endB = weekDayRangeB.endDay ?: startB
        val rangeA = CircularSection(startA.ordinal, endA.ordinal)
        val rangeB = CircularSection(startB.ordinal, endB.ordinal)
        return rangeA.intersects(rangeB)
    }

    private fun areHolidaysIntersecting(firstHolidays: MutableList<Holiday>?, secondHolidays: MutableList<Holiday>?): Boolean {
        if (firstHolidays == null || secondHolidays == null) {
            return false
        }
        for (holiday in firstHolidays) {
            for (holidayCompeting in secondHolidays) {
                require(holiday.useAsWeekDay)
                require(holidayCompeting.useAsWeekDay)
                require(holiday.offset == 0)
                require(holidayCompeting.offset == 0)
                if (holiday.type == holidayCompeting.type) {
                    return true
                }
            }
        }
        return false
    }

    // all info in dates, except months is ignored!
    private fun areMonthRangesIntersecting(aDateRange: DateRange?, bDateRange: DateRange?): Boolean {
        if (aDateRange == null || bDateRange == null) {
            return false
        }
        val startA = aDateRange.startDate
        val endA = aDateRange.endDate ?: aDateRange.startDate
        val startB = bDateRange.startDate
        val endB = bDateRange.endDate ?: bDateRange.startDate
        val rangeA = CircularSection(startA.month.ordinal, endA.month.ordinal)
        val rangeB = CircularSection(startB.month.ordinal, endB.month.ordinal)
        return rangeA.intersects(rangeB)
    }

    private fun areOnlySomeRulesMonthBased(ruleset: ArrayList<Rule>): Boolean {
        var rulesWithMonthLimits = 0
        for (rule in ruleset) {
            if (rule.dates != null) {
                rulesWithMonthLimits += 1
            }
        }
        if (rulesWithMonthLimits == 0) {
            return false
        }
        if (rulesWithMonthLimits == ruleset.size) {
            return false
        }
        return true
    }


    // Reduces rule to a subset supported by StreetComplete
    // in case of any info that would be lost it returns null
    // null is also returned in cases where conversion would be necessary
    // and there is any risk of loss of any data
    private fun reduceRuleToStreetCompleteSupported(rule: Rule): Rule? { // following are ignored:
        val returned = emptyRule()
        val days = rule.days
        if (days == null && rule.holidays == null) {
            // SC requires explicit specification of days of a week or PH
            // holidays may contain some other holidays, but such cases will
            // fail a holiday-specific check
            return null
        }
        if (days != null) {
            val simplifiedWeekDayRanges: MutableList<WeekDayRange> = ArrayList()
            for (weekDayRange in days) {
                val simplifiedDateRange = reduceWeekDayRangeToSimpleDays(weekDayRange)
                        ?: return null
                simplifiedWeekDayRanges.add(simplifiedDateRange)
            }
            if (simplifiedWeekDayRanges.size > 1) {
                //TODO: support also Fr,Sa 11:00-00:00 kind of rules
                return null
            }
            returned.days = simplifiedWeekDayRanges // copy days of the week from the input rule
        }
        val dates = rule.dates
        if (dates != null) {
            val simplifiedDateRanges: MutableList<DateRange> = ArrayList()
            for (dateRange in dates) {
                val simplifiedDateRange = reduceDateRangeToFullMonths(dateRange) ?: return null
                simplifiedDateRanges.add(simplifiedDateRange)
            }
            if (simplifiedDateRanges.size > 1) {
                // happens with rules such as `Mo-Fr 7:30-18:00, Sa-Su 9:00-18:00`
                // that are intentionally rejected as are not directly representable in SC
                // and handling them may result in unexpected silent transformation
                // what is unwanted
                return null
            }
            returned.setDates(simplifiedDateRanges)
        }
        val times = rule.times
        if (times == null) {
            // explicit opening hours are required by SC
            return null
        } else {
            val simplifiedTimespans: ArrayList<TimeSpan> = ArrayList()
            for (time in times) {
                val simplifiedTimespan = reduceTimeRangeToSimpleTime(time) ?: return null
                simplifiedTimespans.add(simplifiedTimespan)
            }
            // multiple timespans may happen for rules such as "Mo-Su 09:00-12:00, 13:00-14:00"
            returned.times = simplifiedTimespans
        }
        val modifier = rule.modifier
        if (modifier != null) {
            val reducedModifier = reduceModifierToAcceptedBySC(modifier) ?: return null
            returned.modifier = reducedModifier
        }
        val holidays = rule.holidays
        if (holidays != null) {
            val reducedHolidays = reduceHolidaysToAcceptedBySC(holidays) ?: return null
            returned.holidays = reducedHolidays
        }
        return if (rule == returned) {
            // original rule is matching reduced rule as no special constructions were used
            returned
        } else {
            // not representable given our limitations
            null
        }
    }

    private fun reduceModifierToAcceptedBySC(modifier: RuleModifier): RuleModifier? {
        // public holidays with "off" specified explicitly are incompatible with SC due to
        // https://github.com/westnordost/StreetComplete/issues/276
        // other opening hours using "off" are rare and would require automated conversion
        // that would drop off part, what may cause issues in weird cases
        if (modifier.modifier != RuleModifier.Modifier.OPEN) {
            return null
        }
        return modifier
    }

    private fun reduceHolidaysToAcceptedBySC(holidays: List<Holiday>): List<Holiday>? {
        // PH, with set opening hours variant is supported by SC
        // many other variants are not, holidays list longer than 1 entry
        // indicates unsupported use
        if (holidays.size > 1) {
            return null
        }
        val holiday = holidays[0]
        val returned = Holiday()
        if (!holiday.useAsWeekDay) {
            // SC is not supporting "public holidays on Mondays" combinations
            return null
        }
        returned.useAsWeekDay = true
        if (holiday.type != Holiday.Type.PH) {
            // SC is not supporting SH
            return null
        }
        returned.type = Holiday.Type.PH
        return listOf(returned)
    }

    // StreetComplete is not supporting offsets, indexing by nth day of week etc
    // function may return identical or modified object or null
    // null or modified object indicates that original object was not representable in SC
    private fun reduceWeekDayRangeToSimpleDays(weekDayRange: WeekDayRange): WeekDayRange? {
        val returned = WeekDayRange()
        if (weekDayRange.startDay == null) {
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
        for (date in arrayOf(dateRange.startDate, dateRange.endDate).filterNotNull()) {
            if (date.isOpenEnded) {
                return null //TODO: it may be supported by StreetComplete
            }
            if (date.weekDayOffset != null) {
                return null
            }
            if (date.dayOffset != 0) {
                return null
            }
        }
        val newDateRange = DateRange()

        val startDate = DateWithOffset()
        startDate.month = dateRange.startDate.month
        newDateRange.startDate = startDate

        val endDate = dateRange.endDate
        if (endDate != null) {
            // range with just single month will have endDate unset
            val newEndDate = DateWithOffset()
            newEndDate.month = endDate.month
            newDateRange.endDate = newEndDate
        }
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

    // turns format returned by opening hours editing widget into an OSM tag
    fun internalIntoTag(openingHours: List<OpeningMonths>): String {
        return openingHours.joinToString(";")
    }
}
