package de.westnordost.streetcomplete.osm.opening_hours

import de.westnordost.osm_opening_hours.model.ClockTime
import de.westnordost.osm_opening_hours.model.ExtendedClockTime
import de.westnordost.osm_opening_hours.model.ExtendedTime
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
import de.westnordost.osm_opening_hours.model.Time
import de.westnordost.osm_opening_hours.model.TimeIntervals
import de.westnordost.osm_opening_hours.model.TimeSpan
import de.westnordost.osm_opening_hours.model.TimesSelector
import de.westnordost.osm_opening_hours.model.TwentyFourSeven
import de.westnordost.osm_opening_hours.model.VariableTime
import de.westnordost.osm_opening_hours.model.Weekday
import de.westnordost.osm_opening_hours.model.WeekdayRange
import de.westnordost.osm_opening_hours.model.WeekdaysSelector
import kotlin.jvm.JvmName

/** Returns true if supported by StreetComplete, i.e. can be displayed in the
 *  widget.
 *
 *  Supported are:
 *  - months, month ranges
 *  - weekdays, weekday ranges, PH
 *  - "off" rules with exclusively weekdays, weekday ranges, PH
 *  - clock time spans with optional open end, or clock time points
 *
 *  Weekdays that collide (Mo-Fr 8:30-12:30; We 14:00-18:00) are considered ambiguous - they are
 *  likely tagging mistakes (Maybe user meant to use "," instead of ";"?).
 *
 *  Also, when any rule has months defined, all rules must have months defined, for clarity,
 *  otherwise they are not supported at all (not even considered ambiguous, see code comment below)
 */
fun OpeningHours.isSupported(
    allowTimePoints: Boolean = false,
    allowAmbiguity: Boolean = false,
): Boolean =
    rules.all { rule -> rule.isSupported() } &&
        (allowTimePoints || !containsTimePoints()) &&
        (allowAmbiguity || !rules.hasCollidingWeekdays()) &&
        /*
          in #6175 we decided to not classify opening hours that have incomplete months as
          ambiguous (=> ask user to clarify) and instead just don't support them at all. Reason:

          Ambiguous opening hours are treated the same as invalid opening hours: they are not shown
          to the user in the widget, as if they haven't been defined at all yet. Apart from
          implementation effort, the reason for not displaying those at all is that the widget
          doesn't show the difference between ";" (overwriting) and "," (additive) rules which are
          usually the cause for the ambiguity. So, users would be unable to detect that something is
          wrong with the currently mapped opening hours if they were displayed.

          E.g.
          `Mo-We 10:00-12:00; Tu 14:00-18:00` would be displayed the same as
          `Mo-We 10:00-12:00, Tu 14:00-18:00` while the first is likely a mistake (and if it is not,
          writing `Mo,We 10:00-12:00; Tu 14:00-18:00` would be not ambiguous)

          For ambiguous opening hours with incomplete months like
          `10:00-12:00; Apr-Sep 14:00-18:00` in particular, the concern has been that the user might
          input only the hours for summer or only for winter in case the opening hours plates are
          just swapped twice a year by the shop since he is unable to see in the app what was
          already mapped.
        */
        !rules.hasIncompleteMonths()

fun Rule.isSupported(): Boolean =
    // comments not supported
    comment == null &&
    // fallback rules not supported
    ruleOperator != RuleOperator.Fallback &&
    when (ruleType) {
        // open and implicitly open rules are supported, but times must be defined
        RuleType.Open, null ->
            selector.hasTimes()
        // unknown-rules are not supported
        RuleType.Unknown -> false
        // only closed-rules that specify weekdays are supported
        RuleType.Closed, RuleType.Off ->
            ruleOperator == RuleOperator.Normal &&
                !selector.hasTimes()
    } &&
    (selector as? Range)?.isSupported() == true

private fun Selector.hasTimes(): Boolean =
    this is Range && !times.isNullOrEmpty()

// only months ranges, weekdays+holidays and times are supported
private fun Range.isSupported(): Boolean =
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
    is StartingAtTime -> start.isSupported()
    is TimeSpan -> start.isSupported() && end.isSupported()
    else -> false
}

internal fun Time.isSupported(): Boolean = when (this) {
    is ClockTime -> true
    is VariableTime -> false
}

internal fun ExtendedTime.isSupported(): Boolean = when(this) {
    is ExtendedClockTime -> true
    is ClockTime -> true
    is VariableTime -> false
}

/** False if no rules have months or all rules have months, true if some rules have months but
 *  others not. */
private fun List<Rule>.hasIncompleteMonths(): Boolean {
    val rulesWithMonths = count { it.selector.hasMonths() }
    return rulesWithMonths > 0 && rulesWithMonths < size
}

private fun Selector.hasMonths(): Boolean =
    when (this) {
        is Range -> hasMonths()
        TwentyFourSeven -> false
    }

private fun Range.hasMonths(): Boolean =
    months.orEmpty().any { it is SingleMonth || it is MonthRange }

/** For example, "Mo-Fr 10:00-12:00; We 14:00-16:00" self-collides: Wednesday is overwritten
 *  to only be open 14:00 to 16:00. A rule collides with another whenever the days overlap. When
 *  non-additive rules and additive rules mix, it becomes a bit difficult to find it out
 *
 *  @throws IllegalArgumentException if any of the rules is not supported
 *  */
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
private fun Collection<MonthsOrDateSelector>.intersectWith(
    other: Collection<MonthsOrDateSelector>
): Boolean {
    val months = getMonths()
    val otherMonths = other.getMonths()
    return months.any { it in otherMonths } || otherMonths.any { it in months }
}

@JvmName("weekdaysSelectorsIntersectWith")
private fun Collection<WeekdaysSelector>.intersectWith(
    other: Collection<WeekdaysSelector>
): Boolean {
    val weekdays = getWeekdays()
    val otherWeekdays = other.getWeekdays()
    return weekdays.any { it in otherWeekdays } || otherWeekdays.any { it in weekdays }
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

private fun ExtendedTime.toMinutesOfDay(): Int = when (this) {
    is ClockTime -> hour * 60 + minutes
    is ExtendedClockTime -> hour * 60 + minutes
    is VariableTime -> throw IllegalArgumentException()

}
