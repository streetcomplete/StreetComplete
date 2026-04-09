package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale

/**
 * Locale-aware formatting of currencies
 *
 * @param locale Locale to use. If [locale] is `null`, the default locale (for formatting) will be
 *   used. Note that the region **must** be specified in the locale for this formatter to format
 *   correctly, otherwise it doesn't know which currency to use
 */
expect class CurrencyFormatter(locale: Locale? = null) {
    /**
     * @param value the value to format, e.g. 3.0
     * @return the formatted input value, e.g. € 3.00
     */
    fun format(value: Double): String

    /** ISO 4217 currency code */
    val currencyCode: String?
}
