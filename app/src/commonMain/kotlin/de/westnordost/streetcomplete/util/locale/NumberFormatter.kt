package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale

/**
 * Locale-aware formatting and parsing of numbers
 *
 * @param locale Locale to use. If [locale] is `null`, the default locale (for formatting) will be
 *   used.
 * */
expect class NumberFormatter(locale: Locale? = null) {

    /**
     * Format the given [value].
     *
     * @param minFractionDigits If the given [value] has less fraction digits, zeroes will be
     *   inserted instead
     *
     * @param maxFractionDigits If the given [value] has more fraction digits, those will be cut off
     *
     * @param useGrouping Whether to use grouping. E.g. in the United States, "10000" can be written
     *   as "10,000" whereas in France it can be written as "10 000".
     */
    fun format(
        value: Number,
        minFractionDigits: Int = 0,
        maxFractionDigits: Int = 3,
        useGrouping: Boolean = false,
    ): String

    /**
     * Parse the given [text]. Returns `null` if it cannot be parsed as a number
     *
     * @param allowGrouping Whether to understand grouped values. E.g. in the United States, "10000"
     *   can be written as "10,000" whereas in France it can be written as "10 000".
     */
    fun parse(
        text: String,
        allowGrouping: Boolean = false
    ): Number?

    /** Return the decimal separator */
    val decimalSeparator: Char

    /** Return the grouping separator */
    val groupingSeparator: Char
}
