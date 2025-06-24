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
        assertEquals("1.12", NumberFormatter(en).format(1.12345, maxFractionDigits = 2))
        assertEquals("1.00", NumberFormatter(en).format(1, minFractionDigits = 2))
    }

    @Test fun `format with grouping`() {
        assertEquals("1,000,000.5", NumberFormatter(en).format(1_000_000.5, useGrouping = true))
        assertEquals("1 000 000,5", NumberFormatter(fr).format(1_000_000.5, useGrouping = true))
        assertEquals("1.000.000,5", NumberFormatter(de).format(1_000_000.5, useGrouping = true))
        assertEquals("١٬٠٠٠٬٠٠٠٫٥", NumberFormatter(ar).format(1_000_000.5, useGrouping = true))
    }

    @Test fun `format without grouping`() {
        assertEquals("1000000.5", NumberFormatter(en).format(1_000_000.5, useGrouping = false))
        assertEquals("1000000,5", NumberFormatter(fr).format(1_000_000.5, useGrouping = false))
        assertEquals("1000000,5", NumberFormatter(de).format(1_000_000.5, useGrouping = false))
        assertEquals("١٠٠٠٠٠٠٫٥", NumberFormatter(ar).format(1_000_000.5, useGrouping = false))
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
        assertEquals(null, NumberFormatter(en).parse("15,000,000.5", allowGrouping = false))

        assertEquals(1_000_000.5, NumberFormatter(en).parse("1000000.5", allowGrouping = true))
        assertEquals(1_000_000.5, NumberFormatter(en).parse("1,000,000.5", allowGrouping = true))
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
