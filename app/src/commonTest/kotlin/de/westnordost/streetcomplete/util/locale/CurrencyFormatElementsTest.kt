package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

class CurrencyFormatElementsTest {

    @Test fun `of Germany Euro`() {
        assertEquals(
            CurrencyFormatElements(
                symbol = "€",
                isSymbolBeforeAmount = false,
                hasWhitespace = true,
                decimalDigits = 2,
                decimalSeparator = ',',
                groupingSeparator = '.',
            ),
            CurrencyFormatElements.of(Locale("de-DE"))
        )
    }

    @Test fun `of Ireland Euro`() {
        assertEquals(
            CurrencyFormatElements(
                symbol = "€",
                isSymbolBeforeAmount = true,
                hasWhitespace = false,
                decimalDigits = 2,
                decimalSeparator = '.',
                groupingSeparator = ',',
            ),
            CurrencyFormatElements.of(Locale("en-IE"))
        )
    }

    @Test fun `of Japan Yen`() {
        assertEquals(
            CurrencyFormatElements(
                symbol = "￥",
                isSymbolBeforeAmount = true,
                hasWhitespace = false,
                decimalDigits = 0,
                decimalSeparator = null,
                groupingSeparator = ',',
            ),
            CurrencyFormatElements.of(Locale("ja-JP"))
        )
    }

    @Test fun `of US Dollar`() {
        assertEquals(
            CurrencyFormatElements(
                symbol = "$",
                isSymbolBeforeAmount = true,
                hasWhitespace = false,
                decimalDigits = 2,
                decimalSeparator = '.',
                groupingSeparator = ',',
            ),
            CurrencyFormatElements.of(Locale("en-US"))
        )
    }

    @Test fun `of Norway Krona`() {
        assertEquals(
            CurrencyFormatElements(
                symbol = "kr",
                isSymbolBeforeAmount = true,
                hasWhitespace = true,
                decimalDigits = 2,
                decimalSeparator = ',',
                groupingSeparator = '\u00A0',
            ),
            CurrencyFormatElements.of(Locale("nb-NO"))
        )
    }
}
