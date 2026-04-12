package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.util.ktx.getDisplayName
import kotlinx.datetime.LocalDate

data class DateFormatElements(
    val order: List<DateComponent>,
    val separator: String,
) {
    companion object {
        fun of(locale: Locale?): DateFormatElements {
            val formatter = LocalDateFormatter(
                locale = locale,
                style = DateTimeFormatStyle.Short
            )
            return parseDateFormatElements(locale) { formatter.format(it) }
                ?: DateFormatElements(
                    order = listOf(DateComponent.Year, DateComponent.Month, DateComponent.Day),
                    separator = "/"
                )
        }
    }
}

/**
 * Probe the given [format] function with a known date
 * to discover the locale's component order and separator,
 * returning a [DateFormatElements].
 *
 * Returns `null` when any of the three components cannot be located in the formatted output,
 * so the caller can fall back to a sensible default.
 *
 * The [locale] is used only for the short-name fallback
 * when the numeric month does not appear in the formatted string.
 * Pass `null` when the formatted string is known to use numeric months.
 */
internal fun parseDateFormatElements(
    locale: Locale?,
    format: (LocalDate) -> String,
): DateFormatElements? {
    // The month and day digits must not appear anywhere in the year,
    // otherwise indexOf finds the wrong position in YMD locales
    // (for example, "2006/1/2" → indexOf("2") would not work
    // because it hits the year, not the day).
    val probe = LocalDate(2026, 3, 5)
    val formatted = format(probe)

    val yearPos = formatted.indexOf(probe.year.toString()).takeIf { it >= 0 }
        ?: formatted.indexOf((probe.year % 100).toString().padStart(2, '0')).takeIf { it >= 0 }
    val monthPos = formatted.indexOf(probe.monthNumber.toString()).takeIf { it >= 0 }
        ?: formatted.indexOf(
            probe.month.getDisplayName(DateTimeTextSymbolStyle.Short, locale)
        ).takeIf { it >= 0 }
    val dayPos = formatted.indexOf(probe.dayOfMonth.toString()).takeIf { it >= 0 }

    if (yearPos == null || monthPos == null || dayPos == null) return null

    val components = listOf(
        DateComponent.Year to yearPos,
        DateComponent.Month to monthPos,
        DateComponent.Day to dayPos,
    ).sortedBy { it.second }

    val order = components.map { it.first }

    val firstStart = components[0].second
    val secondStart = components[1].second
    val between = formatted.substring(firstStart, secondStart)
    val separator = between.replace(Regex("[\\dA-Za-z]"), "").trim()
        .ifEmpty { "/" }

    return DateFormatElements(order, separator)
}

/** Returns the system-default date format
 *  (for example, from iOS Language & Region settings),
 *  or `null` if the platform has no such per-user override. */
expect fun systemDefaultDateFormatElements(): DateFormatElements?

enum class DateComponent { Year, Month, Day }
