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

/** hardcoded overrides in order to format and identify a country's currency correctly even if the
 *  CLDR data available on this device is outdated. See #7414 */
internal fun overrideCurrencyLocale(locale: Locale): Locale =
    when (locale.region) {
        "BG" -> Locale("bg-DE") // Bulgaria introduced Euro in 2026, formatted like in e.g. Germany
        "HR" -> Locale("hr-DE") // Croatia introduced Euro in 2023, formatted like in e.g. Germany
        else -> locale
    }
