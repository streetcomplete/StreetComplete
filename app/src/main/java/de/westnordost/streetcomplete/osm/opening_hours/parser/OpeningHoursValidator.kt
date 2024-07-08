package de.westnordost.streetcomplete.osm.opening_hours.parser

import de.westnordost.osm_opening_hours.model.ClockTime
import de.westnordost.osm_opening_hours.model.ExtendedClockTime
import de.westnordost.osm_opening_hours.model.Holiday
import de.westnordost.osm_opening_hours.model.HolidaySelector
import de.westnordost.osm_opening_hours.model.MonthRange
import de.westnordost.osm_opening_hours.model.MonthsOrDateSelector
import de.westnordost.osm_opening_hours.model.OpeningHours
import de.westnordost.osm_opening_hours.model.Range
import de.westnordost.osm_opening_hours.model.Rule
import de.westnordost.osm_opening_hours.model.RuleOperator
import de.westnordost.osm_opening_hours.model.RuleType
import de.westnordost.osm_opening_hours.model.Selector
import de.westnordost.osm_opening_hours.model.SingleMonth
import de.westnordost.osm_opening_hours.model.SpecificWeekdays
import de.westnordost.osm_opening_hours.model.StartingAtTime
import de.westnordost.osm_opening_hours.model.TimeIntervals
import de.westnordost.osm_opening_hours.model.TimeSpan
import de.westnordost.osm_opening_hours.model.TimesSelector
import de.westnordost.osm_opening_hours.model.TwentyFourSeven
import de.westnordost.osm_opening_hours.model.Weekday
import de.westnordost.osm_opening_hours.model.WeekdayRange
import de.westnordost.osm_opening_hours.model.WeekdaysSelector
import de.westnordost.streetcomplete.osm.opening_hours.model.CircularSection

/** Returns true if supported by StreetComplete, i.e. can be displayed in the
 *  widget.
 *
 *  Supported are (plain)
 *  - months, month ranges
 *  - weekdays, weekday ranges, PH
 *  - "off" rules with exclusively weekdays, weekday ranges, PH
 *  - time spans, open end
 */
fun OpeningHours.isSupportedOpeningHours(): Boolean =
    rules.all { rule -> rule.isSupportedOpeningHours() } &&
        !containsTimePoints() &&
        !rules.hasCollidingWeekdays()

fun Rule.isSupportedOpeningHours(): Boolean =
    // comments not supported
    comment == null &&
    // fallback rules not supported
    ruleOperator != RuleOperator.Fallback &&
    when (ruleType) {
        // open and implictly open rules are supported, but times must be defined
        RuleType.Open, null ->
            selector.hasTimes()
        // unknown-rules are not supported
        RuleType.Unknown -> false
        // only closed-rules that specify weekdays are supported
        RuleType.Closed, RuleType.Off ->
            ruleOperator == RuleOperator.Normal &&
                !selector.hasTimes()
    } &&
    (selector as? Range)?.isSupportedOpeningHours() == true

private fun Selector.hasTimes(): Boolean =
    this is Range && !times.isNullOrEmpty()

// only months ranges, weekdays+holidays and times are supported
private fun Range.isSupportedOpeningHours(): Boolean =
    text == null &&
    years.isNullOrEmpty() &&
    weeks.isNullOrEmpty() &&
    months.orEmpty().all { it.isSupported() } &&
    weekdays.orEmpty().all { it.isSupported() } &&
    holidays.orEmpty().all { it.isSupported() } &&
    times.orEmpty().all { it.isSupported() } &&
    // "weekdays on holidays" not supported
    !isRestrictedByHolidays &&
    !isEmpty()

// only plain months or month ranges (without years) are supported
private fun MonthsOrDateSelector.isSupported(): Boolean = when (this) {
    is SingleMonth -> year == null
    is MonthRange -> year == null
    else -> false
}

// only plain weekdays, weekday ranges are supported
internal fun WeekdaysSelector.isSupported(): Boolean = when (this) {
    is Weekday, is WeekdayRange -> true
    is SpecificWeekdays -> false
}

internal fun HolidaySelector.isSupported(): Boolean =
    this == Holiday.PublicHoliday

// only plain times and time spans with optional open end supported
internal fun TimesSelector.isSupported(): Boolean = when (this) {
    is ClockTime -> true
    is StartingAtTime -> start is ClockTime
    is TimeSpan -> start is ClockTime && (end is ClockTime || end is ExtendedClockTime)
    else -> false
}

/** Returns true if supported by StreetComplete, i.e. can be displayed in the
 *  widget.
 *
 *  Supported are (plain)
 *  - weekdays, weekday ranges, PH
 *  - time points
 */
fun OpeningHours.isSupportedCollectionTimes(): Boolean =
    rules.all { rule -> rule.isSupportedCollectionTimes() } &&
    containsTimePoints() &&
    !rules.hasCollidingWeekdays()

fun Rule.isSupportedCollectionTimes(): Boolean =
    // comments not supported
    comment == null &&
    // fallback rules not supported
    ruleOperator != RuleOperator.Fallback &&
    // no rule types supported
    ruleType == null &&
    (selector as? Range)?.isSupportedCollectionTimes() == true

// only weekdays+holidays and times are supported
private fun Range.isSupportedCollectionTimes(): Boolean =
    text == null &&
    years.isNullOrEmpty() &&
    weeks.isNullOrEmpty() &&
    // months not supported for collection times
    months.isNullOrEmpty() &&
    weekdays.orEmpty().all { it.isSupported() } &&
    holidays.orEmpty().all { it.isSupported() } &&
    !isRestrictedByHolidays &&
    !times.isNullOrEmpty() &&
    times.orEmpty().all { it.isSupported() } &&
    !isEmpty()

/** For example, "Mo-Fr 10:00-12:00; We 14:00-16:00" self-collides: Wednesday is overwritten
 *  to only be open 14:00 to 16:00. A rule collides with another whenever the days overlap. When
 *  non-additive rules and additive rules mix, it becomes a bit difficult to find it out */
fun List<Rule>.hasCollidingWeekdays(): Boolean {
    for (i in indices) {
        val rule1 = get(i)
        if (rule1.ruleType == RuleType.Off || rule1.ruleType == RuleType.Closed) {
            continue
        }
        var additive = true
        for (j in i + 1 until size) {
            val rule2 = get(j)
            // off/closed rules may collide because they overwrite their whole weekdays anyway
            // (because we do not support times for off-rules in StreetComplete)
            if (rule2.ruleType == RuleType.Off || rule2.ruleType == RuleType.Closed) {
                continue
            }
            // additive rules do not collide
            if (rule2.ruleOperator != RuleOperator.Additional) {
                additive = false
            }
            if (!additive) {
                if (rule1.selector.collidesWith(rule2.selector)) return true
            }
        }
    }
    return false
}

private fun Selector.collidesWith(other: Selector): Boolean = when (this) {
    is Range -> when (other) {
        is Range -> collidesWith(other)
        TwentyFourSeven -> true
    }
    TwentyFourSeven -> true
}

private fun Range.collidesWith(other: Range): Boolean {
    // don't check for unsupported things
    require(years.isNullOrEmpty())
    require(other.years.isNullOrEmpty())
    require(weeks.isNullOrEmpty())
    require(other.weeks.isNullOrEmpty())
    require(text == null)
    require(other.text == null)
    require(!isRestrictedByHolidays)
    require(!other.isRestrictedByHolidays)

    // checks assume that times are actually specified
    require(!times.isNullOrEmpty())
    require(!other.times.isNullOrEmpty())

    // if both rules have date ranges and no date range of the first collides with any date range
    // of the second, they do not collide
    val months = months
    val otherMonths = other.months
    if (!months.isNullOrEmpty() && !otherMonths.isNullOrEmpty() && !months.intersectWith(otherMonths)) {
        return false
    }

    // if no weekdays specified: collides with everything
    if (weekdays.isNullOrEmpty() && holidays.isNullOrEmpty()) return true
    if (other.weekdays.isNullOrEmpty() && other.holidays.isNullOrEmpty()) return true

    // handle times looping into next day
    val compareWeekdays = weekdays?.expandedToNextDayIfAnyTimeExpandsIntoNextDay(times)
    val compareOtherWeekdays = other.weekdays?.expandedToNextDayIfAnyTimeExpandsIntoNextDay(other.times)

    if (holidays.orEmpty().intersectWith(other.holidays.orEmpty())) return true
    if (compareWeekdays.orEmpty().intersectWith(compareOtherWeekdays.orEmpty())) return true

    return false
}

@JvmName("monthsOrDateSelectorsIntersectWith")
private fun Collection<MonthsOrDateSelector>.intersectWith(other: Collection<MonthsOrDateSelector>): Boolean =
    any { dates1 -> other.any { dates2 -> dates1.intersectsWith(dates2) } }

private fun MonthsOrDateSelector.intersectsWith(other: MonthsOrDateSelector): Boolean =
    toCircularSection().intersects(other.toCircularSection())

private fun MonthsOrDateSelector.toCircularSection(): CircularSection =
    when (this) {
        is SingleMonth -> {
            require(year == null)
            CircularSection(month.ordinal, month.ordinal)
        }
        is MonthRange -> {
            require(year == null)
            CircularSection(start.ordinal, end.ordinal)
        }
        else -> throw IllegalArgumentException()
    }

@JvmName("weekdaysSelectorsIntersectWith")
private fun Collection<WeekdaysSelector>.intersectWith(other: Collection<WeekdaysSelector>): Boolean =
    any { weekdays1 -> other.any { weekdays2 -> weekdays1.intersectsWith(weekdays2) } }

private fun WeekdaysSelector.intersectsWith(other: WeekdaysSelector): Boolean =
    toCircularSection().intersects(other.toCircularSection())

private fun WeekdaysSelector.toCircularSection(): CircularSection = when (this) {
    is Weekday -> CircularSection(ordinal, ordinal)
    is WeekdayRange -> CircularSection(start.ordinal, end.ordinal)
    is SpecificWeekdays -> throw IllegalArgumentException()
}

@JvmName("holidaySelectorsIntersectWith")
private fun Collection<HolidaySelector>.intersectWith(other: Collection<HolidaySelector>): Boolean =
    any { holiday1 -> other.any { holiday2 -> holiday1.intersectsWith(holiday2) } }

private fun HolidaySelector.intersectsWith(other: HolidaySelector): Boolean =
    this == other

private fun List<WeekdaysSelector>.expandedToNextDayIfAnyTimeExpandsIntoNextDay(
    times: Collection<TimesSelector>?
): List<WeekdaysSelector> =
    if (times?.any { it.expandsIntoNextDay() } == true) {
        map { it.expandedToNextDay() }
    } else {
        this
    }

private fun WeekdaysSelector.expandedToNextDay(): WeekdaysSelector {
    val s: Weekday
    val e: Weekday
    when (this) {
        is Weekday -> {
            s = this
            e = this
        }
        is WeekdayRange -> {
            // already goes full-circle
            if (start.ordinal == end.ordinal + 1 ||
                start.ordinal == 0 && end.ordinal == Weekday.entries.size - 1) {
                return this
            }

            s = start
            e = end
        }
        is SpecificWeekdays -> throw IllegalArgumentException()
    }
    return WeekdayRange(s, Weekday.entries[(e.ordinal + 1) % Weekday.entries.size])
}

private fun TimesSelector.expandsIntoNextDay(): Boolean {
    val s: Int
    val e: Int
    when (this) {
        is TimeIntervals -> {
            s = start.toMinutesOfDay()
            e = end.toMinutesOfDay()
        }
        is TimeSpan -> {
            s = start.toMinutesOfDay()
            e = end.toMinutesOfDay()
        }
        else -> return false
    }
    return e < s || e > 24 * 60
}
