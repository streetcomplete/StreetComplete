package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale

/**
 * Platform-specific currency formatter
 */
expect class CurrencyFormatter (currencyCode: String?) {
    /**
     * Get formatting information for a given currency code
     * @param currencyCode ISO 4217 currency code (e.g. "EUR", "USD", "JPY")
     * @return formatting information for this currency in the current locale
     */
    fun format(sampleValue: Double): String
    val currencyCode: String?
    /**
     * @param countryCode the ISO 3166-1 alpha-2 code of the country
     * @return the ISO 4217 code of the currency in the given country
     */
    fun getCurrencyCodeFromLocale(countryCode: Locale?): String? // TODO the input could be a string
}
