package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.util.ktx.getDisplayName
import kotlinx.datetime.LocalDate


/** Date format symbols used in a locale */
data class DateFormatElements(
    /** Date component order */
    val order: List<DateComponent> = listOf(DateComponent.Year, DateComponent.Month, DateComponent.Day),
    /** Separator between year/month/day */
    val separator: String = "/",
    /** Text before date */
    val before: String = "",
    /** Text after date */
    val after: String = "",
) {
    companion object {
        fun of(locale: Locale?): DateFormatElements {
            val formatter = LocalDateFormatter(
                locale = locale,
                style = DateTimeFormatStyle.Short
            )

            val probe = LocalDate(2016, 3, 5) // Use unique numbers for year, month, day
            val formatted = formatter.format(probe)

            val yearRange =
                formatted.find("2016")
                ?: formatted.find("16")

            val monthRange =
                formatted.find("03")
                ?: formatted.find("3")
                ?: formatted.find(probe.month.getDisplayName(DateTimeTextSymbolStyle.Short, locale))

            val dayRange =
                formatted.find("05")
                ?: formatted.find("5")

            if (yearRange == null || monthRange == null || dayRange == null) {
                return DateFormatElements()
            }

            val components = listOf(
                DateComponent.Year to yearRange,
                DateComponent.Month to monthRange,
                DateComponent.Day to dayRange,
            ).sortedBy { it.second.first }

            val separator = formatted
                .substring(components[0].second.last + 1, components[1].second.first)

            val order = components.map { it.first }

            val before = formatted.substring(0, components.first().second.first)

            val lastDateIndex = components.last().second.last
            val after =
                if (lastDateIndex < formatted.lastIndex) formatted.substring(lastDateIndex + 1)
                else ""

            return DateFormatElements(order, separator, before, after)
        }
    }
}

enum class DateComponent { Year, Month, Day }

/** Find the first occurence of the given [string] in this string.*/
private fun String.find(string: String): IntRange? {
    val startIndex = indexOf(string)
    if (startIndex == -1) return null
    return startIndex..<(startIndex + string.length)
}
