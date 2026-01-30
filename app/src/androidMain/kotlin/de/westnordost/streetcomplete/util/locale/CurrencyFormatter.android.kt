package de.westnordost.streetcomplete.util.locale

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * @param currencyCode the ISO 4217 code of the currency
 * */
actual class CurrencyFormatter actual constructor(currencyCode: String?) {
    actual val currencyCode: String? = currencyCode
    /**
     * @param sampleValue the value to format
     * @return the formatted input value
     */
    actual fun format(sampleValue: Double): String {
        val currency = Currency.getInstance(currencyCode)

        val locale = Locale.getAvailableLocales().firstOrNull {
            try {
                Currency.getInstance(it)?.currencyCode == currencyCode
            } catch (_: Exception) {
                false
            }
        } ?: Locale.getDefault()

        val formatter = NumberFormat.getCurrencyInstance(locale)
        formatter.currency = currency

        val formatted = formatter.format(sampleValue)

        return formatted
    }
    actual fun getCurrencyCodeFromLocale(countryCode: androidx.compose.ui.text.intl.Locale?): String? = try {
            val countryCode = countryCode?.region ?: return null
            val locale = Locale.Builder().setRegion(countryCode).build()
            val currency = java.util.Currency.getInstance(locale)
            currency.currencyCode
        } catch (_: Exception) {
            null
        }
}
