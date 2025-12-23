package de.westnordost.streetcomplete.osm.opening_hours.model

import android.content.res.Resources
import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.util.ktx.getDisplayName
import de.westnordost.streetcomplete.util.ktx.getShortDisplayName
import de.westnordost.streetcomplete.util.locale.DateFormatStyle
import de.westnordost.streetcomplete.util.locale.LocalTimeFormatter
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month

fun Months.Companion.getNames(locale: Locale? = null): Array<String> =
    (1..12).map { Month(it).getDisplayName(locale) }.toTypedArray()

fun Weekdays.Companion.getNames(r: Resources, locale: Locale? = null): Array<String> {
    val weekdayNames = (1..7).map { DayOfWeek(it).getDisplayName(locale) }
    val phName = r.getString(R.string.quest_openingHours_public_holidays)
    return (weekdayNames + phName).toTypedArray()
}

fun Weekdays.Companion.getShortNames(r: Resources, locale: Locale? = null): Array<String> {
    val weekdayNames = (1..7).map { DayOfWeek(it).getShortDisplayName(locale) }
    val phName = r.getString(R.string.quest_openingHours_public_holidays_short)
    return (weekdayNames + phName).toTypedArray()
}

fun Months.toLocalizedString(locale: Locale? = null): String =
    toStringUsing(Months.getNames(locale), ", ", "–")

fun Weekdays.toLocalizedString(r: Resources, locale: Locale? = null) =
    toStringUsing(Weekdays.getShortNames(r, locale), ", ", "–")

fun TimeRange.toLocalizedString(locale: Locale? = null): String {
    val formatter = LocalTimeFormatter(locale, style = DateFormatStyle.Short)
    val sb = StringBuilder()
    val startTime = LocalTime.fromSecondOfDay((start % (24 * 60)) * 60)
    sb.append(formatter.format(startTime))
    if (start != end || !isOpenEnded) {
        sb.append("–")
        val endTime = LocalTime.fromSecondOfDay((end % (24 * 60)) * 60)
        // so a range from 0:00 to 0:00 (=next day) is shown as 0:00–24:00
        // this doesn't affect the 12 hour clock, as 0:00 is formatted as 12:00 AM
        var displayEnd = formatter.format(endTime)
        if (endTime.hour == 0 && endTime.minute == 0 && displayEnd.startsWith("0")) {
            displayEnd = displayEnd.replaceFirst("00?".toRegex(), "24")
        }
        sb.append(displayEnd)
    }
    if (isOpenEnded) sb.append("+")
    return sb.toString()
}
