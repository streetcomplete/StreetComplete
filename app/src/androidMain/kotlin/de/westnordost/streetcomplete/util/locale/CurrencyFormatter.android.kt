package de.westnordost.streetcomplete.util.locale

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

actual class CurrencyFormatter {
    actual fun getFormatInfo(currencyCode: String): CurrencyFormatInfo {
        val currency = Currency.getInstance(currencyCode)

        val locale = Locale.getAvailableLocales().firstOrNull {
            try {
                Currency.getInstance(it)?.currencyCode == currencyCode
            } catch (e: Exception) {
                false
            }
        } ?: Locale.getDefault()

        val formatter = NumberFormat.getCurrencyInstance(locale)
        formatter.currency = currency

        val testValue = 9.0
        val formatted = formatter.format(testValue)

        val symbol = currency.getSymbol(locale)

        val numberIndex = formatted.indexOf('9')
        val symbolIndex = formatted.indexOf(symbol)
        val symbolBeforeAmount = symbolIndex < numberIndex

        val decimalPlaces = currency.defaultFractionDigits

        return CurrencyFormatInfo(
            symbol = symbol,
            symbolBeforeAmount = symbolBeforeAmount,
            decimalPlaces = decimalPlaces
        )
    }
}
