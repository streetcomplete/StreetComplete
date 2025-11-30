package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.LayoutDirection.Ltr
import androidx.compose.ui.unit.LayoutDirection.Rtl

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
