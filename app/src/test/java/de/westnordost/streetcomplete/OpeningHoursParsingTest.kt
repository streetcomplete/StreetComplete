package de.westnordost.streetcomplete

import ch.poole.openinghoursparser.YearRange
import de.westnordost.streetcomplete.osm.opening_hours.parser.isSupported
import de.westnordost.streetcomplete.osm.opening_hours.parser.isSupportedOpeningHours
import de.westnordost.streetcomplete.osm.opening_hours.parser.toOpeningHoursRows
import de.westnordost.streetcomplete.osm.opening_hours.parser.toOpeningHoursRules
import de.westnordost.streetcomplete.osm.opening_hours.parser.weekdaysCollideWithAnother
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

fun main() = runBlocking {
    val limit: Int? = null

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

    // or go to https://sophox.org/#select%20%3Fopening_hours%20where%20%7B%3Felement%20osmt%3Aopening_hours%20%3Fopening_hours%7D
    // execute query, download as CSV and use
    // File(path to downloaded CSV).bufferedReader().useLines { lines ->

    val limitStr = limit?.let { "%0D%0Alimit+$limit" } ?: ""
    val url = URL("https://sophox.org/sparql?query=select%20%3Fopening_hours%20where%20%7B%3Felement%20osmt%3Aopening_hours%20%3Fopening_hours%7D%20$limitStr")
    val connection = url.openConnection() as HttpURLConnection
    try {
        connection.setRequestProperty("Accept", "text/csv")
        connection.setRequestProperty("User-Agent", "StreetComplete opening hours test")
        connection.setRequestProperty("charset", StandardCharsets.UTF_8.name())
        connection.doOutput = true
        connection.inputStream.bufferedReader().useLines { lines -> runBlocking {
            for (line in lines) { launch {
                total++
                var oh = line
                // CSV output wraps string in "..." if it contains a ,
                if (oh.contains(',') && oh.startsWith('"')) {
                    oh = oh.trim { it == '"' }.replace("\"\"", "\"")
                }
                val rules = oh.toOpeningHoursRules()
                if (rules != null) {
                    parsed++
                    val oh2 = rules.toOpeningHoursRows()?.toOpeningHoursRules()?.toString()
                    if (oh2 != null) {
                        supported++
                    } else {
                        val r = rules.rules
                        if (r.any { it.isTwentyfourseven }) {
                            containsTwentyfourseven++
                        }
                        if (r.any { it.comment != null }) {
                            containsComments++
                        }
                        if (r.any { it.isFallBack }) {
                            containsFallback++
                        }
                        if (r.any { it.modifier != null }) {
                            containingOff++
                        }
                        if (r.any { rule -> rule.years != null || rule.dates?.any { it.startDate.year != YearRange.UNDEFINED_YEAR } == true }) {
                            containsYears++
                        }
                        if (r.any { it.weeks != null }) {
                            containsWeeks++
                        }
                        if (r.any { it.times == null && !it.isTwentyfourseven && it.modifier == null }) {
                            noHours++
                        }
                        if (r.any { rule -> rule.holidays?.any { !it.isSupported() } == true }) {
                            complicatedHolidays++
                        }
                        if (r.any { rule -> rule.days?.any { !it.isSupported() } == true }) {
                            complicatedWeekdayRanges++
                        }
                        if (r.any { rule -> rule.times?.any { it.startEvent != null || it.endEvent != null } == true }) {
                            timeEvents++
                        }
                        if (r.any { rule -> rule.times?.any { !it.isSupportedOpeningHours() && it.startEvent == null && it.endEvent == null } == true }) {
                            complicatedTimes++
                        }
                        if (r.any { rule -> rule.dates?.any { !it.isSupportedOpeningHours() } == true }) {
                            complicatedDates++
                        }
                        if (r.all { it.isSupportedOpeningHours() } && r.weekdaysCollideWithAnother()) {
                            selfColliding++
                        }
                    }
                }
                if (total % 100000 == 0) print(".")
            } }
        } }
    } finally {
        connection.disconnect()
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
