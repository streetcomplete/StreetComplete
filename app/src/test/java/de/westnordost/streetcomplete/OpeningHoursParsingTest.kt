package de.westnordost.streetcomplete

import de.westnordost.osm_opening_hours.model.CalendarDate
import de.westnordost.osm_opening_hours.model.ClockTime
import de.westnordost.osm_opening_hours.model.Date
import de.westnordost.osm_opening_hours.model.DateRange
import de.westnordost.osm_opening_hours.model.DatesInMonth
import de.westnordost.osm_opening_hours.model.MonthRange
import de.westnordost.osm_opening_hours.model.MonthsOrDateSelector
import de.westnordost.osm_opening_hours.model.Range
import de.westnordost.osm_opening_hours.model.RuleOperator
import de.westnordost.osm_opening_hours.model.RuleType
import de.westnordost.osm_opening_hours.model.SingleMonth
import de.westnordost.osm_opening_hours.model.SpecificWeekdayDate
import de.westnordost.osm_opening_hours.model.StartingAtDate
import de.westnordost.osm_opening_hours.model.StartingAtTime
import de.westnordost.osm_opening_hours.model.TimeIntervals
import de.westnordost.osm_opening_hours.model.TimeSpan
import de.westnordost.osm_opening_hours.model.TimesSelector
import de.westnordost.osm_opening_hours.model.TwentyFourSeven
import de.westnordost.osm_opening_hours.model.VariableDate
import de.westnordost.osm_opening_hours.model.VariableTime
import de.westnordost.osm_opening_hours.parser.toOpeningHoursOrNull
import de.westnordost.streetcomplete.osm.opening_hours.parser.hasCollidingWeekdays
import de.westnordost.streetcomplete.osm.opening_hours.parser.isSupported
import de.westnordost.streetcomplete.osm.opening_hours.parser.isSupportedOpeningHours
import de.westnordost.streetcomplete.osm.opening_hours.parser.toOpeningHours
import de.westnordost.streetcomplete.osm.opening_hours.parser.toOpeningHoursRows
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

private const val qleverBaseUrl = "https://qlever.cs.uni-freiburg.de/api/osm-planet"

private val qleverQuery = """
    PREFIX osmkey: <https://www.openstreetmap.org/wiki/Key:>
    SELECT ?times (COUNT(?times) as ?count)
    WHERE {?osm_id osmkey:opening_hours ?times}
    GROUP BY ?times
    ORDER BY DESC(?count) DESC(?times)
""".trimIndent().replace("\n", " ").replace("\r", "")

fun main() {
    var total = 0
    var parsed = 0
    var supported = 0

    var containingOff = 0
    var containsFallback = 0
    var containsComments = 0
    var containsYears = 0
    var containsWeeks = 0
    var selfColliding = 0
    var containsTwentyfourseven = 0
    var complicatedTimes = 0
    var complicatedDates = 0
    var complicatedHolidays = 0
    var complicatedWeekdayRanges = 0
    var noHours = 0
    var timeEvents = 0

    val url = URL("$qleverBaseUrl?query=${URLEncoder.encode(qleverQuery, "UTF-8")}&action=tsv_export")
    val connection = url.openConnection() as HttpURLConnection
    val lines = try {
        connection.setRequestProperty("User-Agent", "StreetComplete opening hours test")
        connection.doOutput = true

        connection.inputStream.bufferedReader()
            .lineSequence()
            .drop(1)
            .filterNot { it.isBlank() }
            .map { line ->
                val t = line.lastIndexOf('\t')
                val oh = line.substring(1, t - 1)
                val count = line.substring(t + 1).toInt()
                oh to count
            }.toList()
    } finally {
        connection.disconnect()
    }

    for ((oh, count) in lines) {
        total += count

        val rules = oh.toOpeningHoursOrNull(lenient = true)
        if (rules != null) {
            parsed += count
            val rows = rules.toOpeningHoursRows()?.toOpeningHours()?.toString()
            if (rows != null) {
                supported += count
            } else {
                val r = rules.rules
                if (r.any { it.selector is TwentyFourSeven }) {
                    containsTwentyfourseven += count
                }
                if (r.any { it.comment != null }) {
                    containsComments += count
                }
                if (r.any { it.ruleOperator == RuleOperator.Fallback }) {
                    containsFallback += count
                }
                if (r.any { it.ruleType == RuleType.Off || it.ruleType == RuleType.Closed }) {
                    containingOff += count
                }
                if (r.any { rule ->
                    val selector = rule.selector
                    selector is Range && (!selector.years.isNullOrEmpty() || selector.months.orEmpty().any { it.hasYear() })
                }) {
                    containsYears += count
                }
                if (r.any { !(it.selector as? Range)?.weeks.isNullOrEmpty() }) {
                    containsWeeks += count
                }
                if (r.any { it.selector is Range && it.ruleType == null && (it.selector as? Range)?.times == null }) {
                    noHours += count
                }
                if (r.any { rule ->
                    (rule.selector as? Range)?.holidays.orEmpty().any { !it.isSupported() }
                }) {
                    complicatedHolidays += count
                }
                if (r.any { rule ->
                    (rule.selector as? Range)?.weekdays.orEmpty().any { !it.isSupported() }
                }) {
                    complicatedWeekdayRanges += count
                }
                if (r.any { rule ->
                    (rule.selector as? Range)?.times.orEmpty().any { it.hasVariableTime() }
                }) {
                    timeEvents += count
                }
                if (r.any { rule ->
                    (rule.selector as? Range)?.times.orEmpty().any { !it.isSupported() && !it.hasVariableTime() }
                }) {
                    complicatedTimes += count
                }
                if (r.any { rule ->
                    (rule.selector as? Range)?.months.orEmpty().any { it !is SingleMonth && it !is MonthRange }
                }) {
                    complicatedDates += count
                }
                if (r.all { it.isSupportedOpeningHours() } && r.hasCollidingWeekdays()) {
                    selfColliding += count
                }
            }
        }
        if (total % 100000 == 0) print(".")
    }

    println()
    print("$total opening hours")
    if (total > 0) {
        print(", ${percent(1.0 * parsed / total)} parseable. ")
    }
    if (parsed > 0) {
        print("Of these ${percent(1.0 * supported / parsed)} are supported.")
    }
    println()
    println()
    println("Of the unsupported opening hours, ")
    val unsupported = parsed - supported
    println("${percent(1.0 * containsTwentyfourseven / unsupported)} are \"24/7\"")
    println("${percent(1.0 * containingOff / unsupported)} rules with \"off\", \"closed\" etc. modifier with additional comment")
    println("${percent(1.0 * selfColliding / unsupported)} collide with themselves (likely an error)")
    println("${percent(1.0 * containsYears / unsupported)} contain years")
    println("${percent(1.0 * containsWeeks / unsupported)} contain week numbers")
    println("${percent(1.0 * complicatedHolidays / unsupported)} contain unsupported holiday definitions")
    println("${percent(1.0 * complicatedWeekdayRanges / unsupported)} contain unsupported weekday ranges")
    println("${percent(1.0 * timeEvents / unsupported)} contain time events like sunset, sunrise, dusk, ...")
    println("${percent(1.0 * complicatedTimes / unsupported)} contain otherwise unsupported time ranges")
    println("${percent(1.0 * complicatedDates / unsupported)} contain unsupported date ranges")
    println("${percent(1.0 * noHours / unsupported)} contain no hours")
    println("${percent(1.0 * containsFallback / unsupported)} contain fallback rules")
    println("${percent(1.0 * containsComments / unsupported)} contain comments")
}

private fun percent(v: Double): String = "%.1f".format(100 * v) + "%"

private fun TimesSelector.hasVariableTime(): Boolean = when (this) {
    is ClockTime -> false
    is VariableTime -> true
    is TimeIntervals -> start is VariableTime || end is VariableTime
    is StartingAtTime -> start is VariableTime
    is TimeSpan -> start is VariableTime || end is VariableTime
}

private fun MonthsOrDateSelector.hasYear(): Boolean = when (this) {
    is Date -> hasYear()
    is DateRange -> start.hasYear() || end.hasYear()
    is DatesInMonth -> year != null
    is MonthRange -> year != null
    is SingleMonth -> year != null
    is StartingAtDate -> start.hasYear()
}

private fun Date.hasYear(): Boolean = when (this) {
    is CalendarDate -> year != null
    is SpecificWeekdayDate -> year != null
    is VariableDate -> year != null
}
