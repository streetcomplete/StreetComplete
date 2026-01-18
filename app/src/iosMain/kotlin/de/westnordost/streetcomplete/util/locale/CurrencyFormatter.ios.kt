package de.westnordost.streetcomplete.util.locale

import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleCurrencyCode
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle
import platform.Foundation.availableLocaleIdentifiers
import platform.Foundation.currentLocale

actual class CurrencyFormatter {
    actual fun getFormatInfo(currencyCode: String): CurrencyFormatInfo {
        val formatter = NSNumberFormatter()
        formatter.numberStyle = NSNumberFormatterCurrencyStyle
        formatter.currencyCode = currencyCode

        val locale = NSLocale.availableLocaleIdentifiers
            .mapNotNull { identifier -> NSLocale(identifier as String) }
            .firstOrNull { locale ->
                (locale.objectForKey(NSLocaleCurrencyCode) as? String) == currencyCode
            } ?: NSLocale.currentLocale

        formatter.locale = locale

        val testValue = 9.0
        val formatted = formatter.stringFromNumber(NSNumber(testValue)) ?: ""

        val currencySymbol = formatter.currencySymbol

        val numberIndex = formatted.indexOf('9')
        val symbolIndex = formatted.indexOf(currencySymbol)
        val symbolBeforeAmount = symbolIndex < numberIndex

        val decimalPlaces = formatter.maximumFractionDigits.toInt()

        return CurrencyFormatInfo(
            symbol = currencySymbol,
            symbolBeforeAmount = symbolBeforeAmount,
            decimalPlaces = decimalPlaces
        )
    }
}
