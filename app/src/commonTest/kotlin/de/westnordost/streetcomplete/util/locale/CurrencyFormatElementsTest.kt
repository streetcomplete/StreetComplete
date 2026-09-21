package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
        val expected = CurrencyFormatElements(
            symbol = "￥",
            isSymbolBeforeAmount = true,
            hasWhitespace = false,
            decimalDigits = 0,
            decimalSeparator = null,
            groupingSeparator = ',',
        )
        val actual = CurrencyFormatElements.of(Locale("ja-JP"))
        assertTrue(actual.symbol in listOf("￥", "¥"))
        assertEquals(expected.isSymbolBeforeAmount, actual.isSymbolBeforeAmount)
        assertEquals(expected.hasWhitespace, actual.hasWhitespace)
        assertEquals(expected.decimalDigits, actual.decimalDigits)
        assertEquals(expected.decimalSeparator, actual.decimalSeparator)
        assertEquals(expected.groupingSeparator, actual.groupingSeparator)
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
}
