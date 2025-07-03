package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale

/**
 * Locale-aware formatting and parsing of numbers
 *
 * @param locale Locale to use. If [locale] is `null`, the default locale (for formatting) will be
 *   used.
 * @param minIntegerDigits Minimum number of integer digits when formatting
 * @param maxIntegerDigits Maximum number of integer digits when formatting
 * @param minFractionDigits Minimum number of fraction digits when formatting
 * @param maxFractionDigits Maximum number of fraction digits when formatting
 * @param useGrouping Whether to use grouping when formatting and whether to understand grouping
 *   on parsing. E.g. in the United States, "10000" can be written as "10,000" whereas in France it
 *   can be written as "10 000".
 * */
expect class NumberFormatter(
    locale: Locale? = null,
    minIntegerDigits: Int = 1,
    maxIntegerDigits: Int = 42,
    minFractionDigits: Int = 0,
    maxFractionDigits: Int = 3,
    useGrouping: Boolean = false,
) {

    /** Format the given [value]. */
    fun format(value: Number): String

    /** Parse the given [text]. Returns `null` if it cannot be parsed as a number */
    fun parse(text: String): Number?

    /** Return the decimal separator */
    val decimalSeparator: Char

    /** Return the grouping separator */
    val groupingSeparator: Char
}
