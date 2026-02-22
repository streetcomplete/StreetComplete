package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CurrencyFormatterTest {
    @Test fun `euro in Germany`() {
        val f = formatter("de-DE")
        assertEquals("1.538,00\u00A0€", f.format(1538.0))
        assertEquals("EUR", f.currencyCode)
    }

    @Test fun `Ireland euro in Ireland`() {
        val f = formatter("en-IE")
        assertEquals("€1,538.00", f.format(1538.0))
        assertEquals("EUR", f.currencyCode)
    }

    @Test fun `yen in Japan`() {
        val f = formatter("ja-JP")
        assertEquals("￥1,538", f.format(1538.00))
        assertEquals("JPY", f.currencyCode)
    }

    @Test fun `dollar in US`() {
        val f = formatter("en-US")
        assertEquals("$1,538.00", f.format(1538.00))
        assertEquals("USD", f.currencyCode)
    }

    @Test fun `krona in Norway`() {
        val f = formatter("nb-NO")
        assertEquals("kr\u00A01\u00A0538,00", f.format(1538.00))
        assertEquals("NOK", f.currencyCode)
    }

    @Test fun `riyal in Saudi Arabia`() {
        val f = formatter("ar-SA")
        assertEquals("\u200F١٬٥٣٨٫٠٠\u00A0ر.س.\u200F", f.format(1538.00))
        assertEquals("SAR", f.currencyCode)
    }

    private fun formatter(localeTag: String) =
        CurrencyFormatter(Locale(localeTag))
}
