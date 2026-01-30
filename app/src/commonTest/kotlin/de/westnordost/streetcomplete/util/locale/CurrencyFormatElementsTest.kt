package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

class CurrencyFormatElementsTest {
    @Test fun `of de-DE euro`() {
        val german = Locale("de-DE")
        val eurFormatElements = CurrencyFormatElements(
            " €",
            symbolBeforeAmount = false,
            decimalPlaces = 2
        )
        val actualFormatElement = CurrencyFormatElements.of(german)
        assertEquals(
            eurFormatElements.symbolBeforeAmount,
            actualFormatElement.symbolBeforeAmount
        )
        assertEquals(
            eurFormatElements.symbol.trim(),
            actualFormatElement.symbol.trim()
        )
        assertEquals(
            eurFormatElements.decimalPlaces,
            actualFormatElement.decimalPlaces
        )
    }
    @Test fun `of ja-JP yen`() {
        val japanese = Locale("ja-JP")
        val expected = CurrencyFormatElements(
            "￥",
            symbolBeforeAmount = true,
            decimalPlaces = 0
        )
        val actual = CurrencyFormatElements.of(japanese)
        assertEquals(expected, actual)
    }
    @Test fun `of en-US USD`() {
        val usa = Locale("en-US")
        val expected = CurrencyFormatElements(
            "US$",
            symbolBeforeAmount = true,
            decimalPlaces = 2
        )
        val actual = CurrencyFormatElements.of(usa)
        assertEquals(expected, actual)
    }
    @Test fun `of no-NO NOK`() {
        val norwegian = Locale("no-NO")
        val nokFormatElements = CurrencyFormatElements(
            " kr",
            symbolBeforeAmount = false,
            decimalPlaces = 2
        )
        val actualFormatElement = CurrencyFormatElements.of(norwegian)
        assertEquals(
            nokFormatElements.symbolBeforeAmount,
            actualFormatElement.symbolBeforeAmount
        )
        assertEquals(
            nokFormatElements.symbol.trim(),
            actualFormatElement.symbol.trim()
        )
        assertEquals(
            nokFormatElements.decimalPlaces,
            actualFormatElement.decimalPlaces
        )
    }
}
