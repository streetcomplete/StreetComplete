package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale

/**
 * Information about how a currency should be formatted in the current locale
 */
data class CurrencyFormatElements(
    /** The currency symbol (e.g. "€", "$", "£") */
    val symbol: String,
    /** Whether the symbol comes before the amount (true for "$10", false for "10 €") */
    val isSymbolBeforeAmount: Boolean,
    /** Whether there is a whitespace between the currency symbol and the amount */
    val hasWhitespace: Boolean,
    /** Number of decimal places (e.g. 2 for EUR/USD, 0 for JPY) */
    val decimalDigits: Int,
    /** Decimal separator, e.g. the comma in "1.500,00$" */
    val decimalSeparator: Char?,
    /** Grouping separator, e.g. the dot in "1.500,00$" */
    val groupingSeparator: Char?
) {
    companion object {
        fun of(locale: Locale?): CurrencyFormatElements =
            ofOrNull(locale) ?: defaultFallback(locale)

        private fun ofOrNull(locale: Locale?): CurrencyFormatElements? {
            val formatter = CurrencyFormatter(locale)
            val d = "\\p{Nd}" // digit
            val a = "[^\\p{Nd}]" // not a digit
            // e.g.  US          $    1  ,   500      .  00
            // or    NO          kr   1  ␣   500      ,  00
            // or    DE               1  .   500      ,  00      €
            // or    JP          ￥   1  ,   500
            val regex = Regex("($a+)?$d($a)?$d{3}(?:($a)($d+))?($a+)?")
            val matchResult = regex.matchEntire(formatter.format(1500.00)) ?: return null
            val values = matchResult.groupValues
            val symbolBefore = values[1].takeIf { it.isNotEmpty() }
            val groupingSeparator = values[2].firstOrNull()
            val decimalSeparator = values[3].firstOrNull()
            val fractionDigits = values[4].length
            val symbolAfter = values[5].takeIf { it.isNotEmpty() }

            // huh, there's either something both in front and end or neither? Don't know what this is, then!
            if (symbolAfter != null && symbolBefore != null) return null
            val symbol = symbolBefore ?: symbolAfter ?: return null
            val symbolOnly = symbol.trim()

            return CurrencyFormatElements(
                symbol = symbolOnly,
                isSymbolBeforeAmount = symbolBefore != null,
                hasWhitespace = symbolOnly != symbol,
                decimalDigits = fractionDigits,
                decimalSeparator = decimalSeparator,
                groupingSeparator = groupingSeparator,
            )
        }

        private fun defaultFallback(locale: Locale?): CurrencyFormatElements {
            val numberFormatter = NumberFormatter(locale)
            return CurrencyFormatElements(
                symbol = "¤",
                isSymbolBeforeAmount = true,
                hasWhitespace = true,
                decimalDigits = 2,
                decimalSeparator = numberFormatter.decimalSeparator,
                groupingSeparator = numberFormatter.groupingSeparator
            )
        }
    }
}
