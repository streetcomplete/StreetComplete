package de.westnordost.streetcomplete.util.locale

/**
 * Information about how a currency should be formatted in the current locale
 */
data class CurrencyFormatInfo(
    /** The currency symbol (e.g. "€", "$", "£") */
    val symbol: String,
    /** Whether the symbol comes before the amount (true for "$10", false for "10€") */
    val symbolBeforeAmount: Boolean,
    /** Number of decimal places (e.g. 2 for EUR/USD, 0 for JPY) */
    val decimalPlaces: Int
)

/**
 * Platform-specific currency formatter
 */
expect class CurrencyFormatter {
    /**
     * Get formatting information for a given currency code
     * @param currencyCode ISO 4217 currency code (e.g. "EUR", "USD", "JPY")
     * @return formatting information for this currency in the current locale
     */
    fun getFormatInfo(currencyCode: String): CurrencyFormatInfo
}
