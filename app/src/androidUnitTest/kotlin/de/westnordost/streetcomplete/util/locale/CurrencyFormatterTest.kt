package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CurrencyFormatterTest {
    @Test fun `format from currency code de-DE`() {
        val currencyFormatter = CurrencyFormatter("EUR")
        val locale = Locale("de-DE")
        assertEquals("EUR", currencyFormatter.getCurrencyCodeFromLocale(locale))
    }
    @Test fun `format from currency code ja-JP`() {
        val currencyFormatter = CurrencyFormatter("EUR")
        val locale = Locale("ja-JP")
        assertEquals("JPY", currencyFormatter.getCurrencyCodeFromLocale(locale))
    }
    @Test fun `format from currency code en-US`() {
        val currencyFormatter = CurrencyFormatter("USD")
        val locale = Locale("en-US")
        assertEquals("USD", currencyFormatter.getCurrencyCodeFromLocale(locale))
    }
    @Test fun `format from currency code no-NO`() {
        val currencyFormatter = CurrencyFormatter("NOK")
        val locale = Locale("no-NO")
        assertEquals("NOK", currencyFormatter.getCurrencyCodeFromLocale(locale))
    }
}
