package de.westnordost.streetcomplete.osm.opening_hours.model

import android.content.res.Resources
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.util.timeOfDayToString
import java.text.DateFormatSymbols
import java.util.Locale
import kotlin.text.StringBuilder

fun Months.Companion.getNames(locale: Locale): Array<String> {
    val symbols = DateFormatSymbols.getInstance(locale)
    val result = symbols.months.copyOf(MONTHS_COUNT)
    return result.requireNoNulls()
}

fun Weekdays.Companion.getNames(r: Resources, locale: Locale): Array<String> {
    val symbols = DateFormatSymbols.getInstance(locale)
    val result = symbols.weekdays.toIso8601Order().copyOf(OSM_ABBR_WEEKDAYS.size)
    result[PUBLIC_HOLIDAY] = r.getString(R.string.quest_openingHours_public_holidays)
    return result.requireNoNulls()
}

fun Weekdays.Companion.getShortNames(r: Resources, locale: Locale): Array<String> {
    val symbols = DateFormatSymbols.getInstance(locale)
    val result = symbols.shortWeekdays.toIso8601Order().copyOf(OSM_ABBR_WEEKDAYS.size)
    result[PUBLIC_HOLIDAY] = r.getString(R.string.quest_openingHours_public_holidays_short)
    return result.requireNoNulls()
}

private fun Array<String>.toIso8601Order() = Array(7) { this[1 + (it + 1) % 7] }

fun Months.toLocalizedString(locale: Locale): String =
    toStringUsing(Months.Companion.getNames(locale), ", ", "–")

fun Weekdays.toLocalizedString(r: Resources, locale: Locale) =
    toStringUsing(Weekdays.Companion.getShortNames(r, locale), ", ", "–")

fun TimeRange.toLocalizedString(locale: Locale): String {
    val sb = StringBuilder()
    sb.append(timeOfDayToString(locale, start))
    if (start != end || !isOpenEnded) {
        sb.append("–")
        var displayEnd = timeOfDayToString(locale, end % (24 * 60))
        if (displayEnd == "00:00") displayEnd = "24:00"
        sb.append(displayEnd)
    }
    if (isOpenEnded) sb.append("+")
    return sb.toString()
}
