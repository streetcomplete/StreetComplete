package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

class NumberFormatterTest {
    val en = Locale("en-US")
    val fr = Locale("fr-FR")
    val de = Locale("de-DE")
    val ar = Locale("ar-SA")

    @Test fun format() {
        assertEquals("1", NumberFormatter(en).format(1))
        assertEquals("1", NumberFormatter(en).format(1f))
        assertEquals("1", NumberFormatter(en).format(1.0))
        assertEquals("1.5", NumberFormatter(en).format(1.5))
        assertEquals("1,5", NumberFormatter(fr).format(1.5))
    }

    @Test fun `format digit limits`() {
        assertEquals("1.12", NumberFormatter(en, maxFractionDigits = 2).format(1.12345))
        assertEquals("1.00", NumberFormatter(en, minFractionDigits = 2).format(1))
        assertEquals("23", NumberFormatter(en, maxIntegerDigits = 2).format(123))
        assertEquals("01", NumberFormatter(en, minIntegerDigits = 2).format(1))
    }

    @Test fun `format with grouping`() {
        assertEquals("1,000,000.5", NumberFormatter(en, useGrouping = true).format(1_000_000.5))
        assertEquals("1 000 000,5", NumberFormatter(fr, useGrouping = true).format(1_000_000.5))
        assertEquals("1.000.000,5", NumberFormatter(de, useGrouping = true).format(1_000_000.5))
        assertEquals("١٬٠٠٠٬٠٠٠٫٥", NumberFormatter(ar, useGrouping = true).format(1_000_000.5))
    }

    @Test fun `format without grouping`() {
        assertEquals("1000000.5", NumberFormatter(en, useGrouping = false).format(1_000_000.5))
        assertEquals("1000000,5", NumberFormatter(fr, useGrouping = false).format(1_000_000.5))
        assertEquals("1000000,5", NumberFormatter(de, useGrouping = false).format(1_000_000.5))
        assertEquals("١٠٠٠٠٠٠٫٥", NumberFormatter(ar, useGrouping = false).format(1_000_000.5))
    }

    @Test fun `format defaults`() {
        assertEquals("0.112", NumberFormatter(en).format(0.112233))
        assertEquals("123", NumberFormatter(en).format(123))
        assertEquals("10000", NumberFormatter(en).format(10_000))
    }

    @Test fun parse() {
        assertEquals(1L, NumberFormatter(en).parse("1"))
        assertEquals(1L, NumberFormatter(en).parse("1.0"))
        assertEquals(1.5, NumberFormatter(en).parse("1.5"))
        assertEquals(1.5, NumberFormatter(fr).parse("1,5"))

        assertEquals(null, NumberFormatter(en).parse("1,5"))
        assertEquals(null, NumberFormatter(fr).parse("1.5"))
        assertEquals(null, NumberFormatter(en, useGrouping = false).parse("15,000,000.5"))

        assertEquals(1_000_000.5, NumberFormatter(en, useGrouping = true).parse("1000000.5"))
        assertEquals(1_000_000.5, NumberFormatter(en, useGrouping = true).parse("1,000,000.5"))
    }

    @Test fun separators() {
        assertEquals(',', NumberFormatter(en).groupingSeparator)
        assertEquals('.', NumberFormatter(en).decimalSeparator)

        assertEquals('.', NumberFormatter(de).groupingSeparator)
        assertEquals(',', NumberFormatter(de).decimalSeparator)

        assertEquals(' ', NumberFormatter(fr).groupingSeparator)
        assertEquals(',', NumberFormatter(fr).decimalSeparator)

        assertEquals('٬', NumberFormatter(ar).groupingSeparator)
        assertEquals('٫', NumberFormatter(ar).decimalSeparator)
    }
}
