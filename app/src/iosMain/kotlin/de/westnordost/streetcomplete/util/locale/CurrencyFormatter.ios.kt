package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale
import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleCurrencyCode
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle
import platform.Foundation.availableLocaleIdentifiers
import platform.Foundation.currentLocale

actual class CurrencyFormatter actual constructor(currencyCode: String?) {
    actual val currencyCode: String? = currencyCode
    actual fun format(sampleValue: Double): String {
        val formatter = NSNumberFormatter()
        formatter.numberStyle = NSNumberFormatterCurrencyStyle
        if (currencyCode != null) {
            formatter.currencyCode = currencyCode
        }
        val locale = NSLocale.availableLocaleIdentifiers
            .mapNotNull { identifier -> NSLocale(identifier as String) }
            .firstOrNull { locale ->
                (locale.objectForKey(NSLocaleCurrencyCode) as? String) == currencyCode
            } ?: NSLocale.currentLocale

        formatter.locale = locale

        val formatted = formatter.stringFromNumber(NSNumber(sampleValue)) ?: ""

        return formatted
    }
    actual fun getCurrencyCodeFromLocale(countryCode: Locale?): String? = try {
            val formatter = NSNumberFormatter()
            formatter.numberStyle = NSNumberFormatterCurrencyStyle
        if (currencyCode != null) {
            formatter.currencyCode = currencyCode
        }

            // val locale = java.util.Locale.Builder().setRegion(countryCode).build()
            // val currency = java.util.Currency.getInstance(locale)
            val locale = NSLocale.availableLocaleIdentifiers
                .mapNotNull { identifier -> NSLocale(identifier as String) }
                .firstOrNull { locale ->
                    (locale.objectForKey(NSLocaleCurrencyCode) as? String) == currencyCode
                }
            if (locale != null) {
                formatter.locale = locale
            }

            formatter.currencyCode
        } catch (_: Exception) {
            null
        }
}
