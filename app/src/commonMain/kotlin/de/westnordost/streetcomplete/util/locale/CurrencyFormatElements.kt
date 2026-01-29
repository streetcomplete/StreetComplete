package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale

/**
 * Information about how a currency should be formatted in the current locale
 */
data class CurrencyFormatElements(
    /** The currency symbol (e.g. "€", "$", "£") */
    val symbol: String,
    /** Whether the symbol comes before the amount (true for "$10", false for "10€") */
    val symbolBeforeAmount: Boolean,
    /** Number of decimal places (e.g. 2 for EUR/USD, 0 for JPY) */
    val decimalPlaces: Int
) {
    companion object {
        fun of(locale: Locale?): CurrencyFormatElements {
            val sampleValue = 7.0
            val currencyCode = CurrencyFormatter("").getCurrencyCodeFromLocale(locale)
            val currencyFormatter = CurrencyFormatter(currencyCode)
            val formattedString = currencyFormatter.format(sampleValue)

            // Extract currency symbol using regex
            // Matches non-digit, non-whitespace, non-punctuation characters (except comma/period)
            val symbolRegex = Regex("[^\\d\\s.,]+")
            val symbol = symbolRegex.find(formattedString)?.value ?: currencyCode
            if (symbol != null) {
                // Determine if symbol comes before amount
                val digitRegex = Regex("\\d")

                val symbolIndex = formattedString.indexOf(symbol)
                val firstDigitIndex = digitRegex.find(formattedString)?.range?.first ?: 0
                val symbolBeforeAmount = symbolIndex < firstDigitIndex

                // Determine decimal places from formatted string
                val decimalSeparatorRegex = Regex("[.,](\\d+)")
                val decimalMatch = decimalSeparatorRegex.find(formattedString)
                val decimalPlaces = decimalMatch?.groupValues?.get(1)?.length ?: 0

                return CurrencyFormatElements(
                    symbol = symbol,
                    symbolBeforeAmount = symbolBeforeAmount,
                    decimalPlaces = decimalPlaces
                )
            } else {
                return CurrencyFormatElements(
                    symbol = currencyCode.toString(),
                    symbolBeforeAmount = true,
                    decimalPlaces = 2
                )
            }
        }
        fun of(currencyCode: String): CurrencyFormatElements {
            val sampleValue = 7.0
            val currencyFormatter = CurrencyFormatter(currencyCode)
            val formattedString = currencyFormatter.format(sampleValue)

            // Extract currency symbol using regex
            // Matches non-digit, non-whitespace, non-punctuation characters (except comma/period)
            val symbolRegex = Regex("[^\\d\\s.,]+")
            val symbol = symbolRegex.find(formattedString)?.value ?: currencyCode
            if (symbol != null) {
                // Determine if symbol comes before amount
                val digitRegex = Regex("\\d")

                val symbolIndex = formattedString.indexOf(symbol)
                val firstDigitIndex = digitRegex.find(formattedString)?.range?.first ?: 0
                val symbolBeforeAmount = symbolIndex < firstDigitIndex

                // Determine decimal places from formatted string
                val decimalSeparatorRegex = Regex("[.,](\\d+)")
                val decimalMatch = decimalSeparatorRegex.find(formattedString)
                val decimalPlaces = decimalMatch?.groupValues?.get(1)?.length ?: 0

                return CurrencyFormatElements(
                    symbol = symbol,
                    symbolBeforeAmount = symbolBeforeAmount,
                    decimalPlaces = decimalPlaces
                )
            } else {
                return CurrencyFormatElements(
                    symbol = currencyCode,
                    symbolBeforeAmount = true,
                    decimalPlaces = 2
                )
            }
        }
    }
}
