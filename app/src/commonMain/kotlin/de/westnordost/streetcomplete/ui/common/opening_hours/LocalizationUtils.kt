package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.LayoutDirection.Ltr
import androidx.compose.ui.unit.LayoutDirection.Rtl
import de.westnordost.osm_opening_hours.model.ClockTime
import de.westnordost.osm_opening_hours.model.ExtendedClockTime
import de.westnordost.osm_opening_hours.model.ExtendedTime
import de.westnordost.osm_opening_hours.model.Holiday
import de.westnordost.osm_opening_hours.model.Month
import de.westnordost.osm_opening_hours.model.Time
import de.westnordost.osm_opening_hours.model.Weekday
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_openingHours_public_holidays
import de.westnordost.streetcomplete.resources.quest_openingHours_public_holidays_short
import de.westnordost.streetcomplete.util.ktx.getDisplayName
import de.westnordost.streetcomplete.util.locale.DateTimeTextSymbolStyle
import de.westnordost.streetcomplete.util.locale.LocalTimeFormatter
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.StringResource

fun List<CharSequence>.joinToLocalizedString(
    locale: Locale? = null,
    layoutDirection: LayoutDirection = Ltr,
): String =
    when (layoutDirection) {
        Ltr -> this
        Rtl -> asReversed()
    }.joinToString(
        separator = enumerationSeparator(locale)
    )

/** Separation-character used in enumerations, e.g. Monday, Tuesday
 *  Source: https://en.wikipedia.org/wiki/Comma */
fun enumerationSeparator(locale: Locale?): String =
    when (locale?.script) {
        "Arab", "Aran" -> {
            if (locale.language == "sd") " ⹁"
            else " ،"
        }
        "Jpan", "Hani", "Hant", "Hans" -> "、"
        else -> when (locale?.language) {
            "ja", "zh" -> "、"
            "sd" -> " ⹁"
            // languages written in Arabic script in their default/native region
            "ar", "fa", "ur", "ps", "ug" -> " ،"
            else -> ", "
        }
    }

fun localizedRange(
    start: String,
    end: String,
    locale: Locale? = null,
    layoutDirection: LayoutDirection = Ltr,
): String {
    val to = rangeSeparator(locale)
    return when (layoutDirection) {
        Ltr -> start + to + end
        Rtl -> end + to + start
    }
}

/** Character used for ranges, e.g. Monday–Friday */
private fun rangeSeparator(locale: Locale?): Char =
    when (locale?.script) {
        "Jpan" -> '～'
        else -> when (locale?.language) {
            "ja" -> '～'
            else -> '–' // en-dash
        }
    }

fun Month.getDisplayName(
    style: DateTimeTextSymbolStyle = DateTimeTextSymbolStyle.Full,
    locale: Locale? = null
): String = kotlinx.datetime.Month(ordinal + 1).getDisplayName(style, locale)

fun Weekday.getDisplayName(
    style: DateTimeTextSymbolStyle = DateTimeTextSymbolStyle.Full,
    locale: Locale? = null
): String = DayOfWeek(ordinal + 1).getDisplayName(style, locale)

fun Holiday.getDisplayNameResource(
    style: DateTimeTextSymbolStyle = DateTimeTextSymbolStyle.Full
): StringResource = when (this) {
    Holiday.PublicHoliday -> when (style) {
        DateTimeTextSymbolStyle.Full -> Res.string.quest_openingHours_public_holidays
        else ->                         Res.string.quest_openingHours_public_holidays_short
    }
    Holiday.SchoolHoliday -> throw UnsupportedOperationException()
}
