package de.westnordost.streetcomplete.data.elementfilter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NumberWithUnitParserKtTest {

    @Test fun empty() {
        assertNull(parse(""))
    }

    @Test fun number() {
        assertEquals(1.0, parse("1.0"))
        assertEquals(1.0, parse("1"))
        assertEquals(1.0, parse("1.00"))
        assertEquals(0.1, parse("0.1"))
        assertEquals(0.1, parse(".1"))
    }

    @Test fun `feet and inches`() {
        val ft5in8 = 5 * 0.3048 + 8 * 0.0254
        assertEquals(ft5in8, parse("5'8\""))
        assertEquals(ft5in8, parse("5' 8\""))
        assertEquals(ft5in8, parse("5  '  8 \""))
        assertEquals(ft5in8, parse("5 ft 8 in"))
        assertEquals(ft5in8, parse("5ft8in"))
    }

    @Test fun `standard units`() {
        assertEquals(1.0, parse("1m"))
        assertEquals(1.0, parse("1 m"))
        assertEquals(1.0, parse("1 km/h"))
        assertEquals(1.0, parse("1 kph"))
        assertEquals(1.0, parse("1 t"))
    }

    @Test fun feet() {
        val ft = 0.3048
        assertEquals(ft, parse("1 ft"))
        assertEquals(ft, parse("1 '"))
    }

    @Test fun inches() {
        val inch = 0.0254
        assertEquals(inch, parse("1 in"))
        assertEquals(inch, parse("1 \""))
    }

    @Test fun yards() {
        val yd = 0.9144
        assertEquals(yd, parse("1 yd"))
        assertEquals(yd, parse("1 yds"))
    }

    @Test fun pounds() {
        val lb = 0.00045359237
        assertEquals(lb, parse("1 lb"))
        assertEquals(lb, parse("1 lbs"))
    }

    @Test fun `other units`() {
        assertEquals(0.001, parse("1 mm"))
        assertEquals(0.01, parse("1 cm"))
        assertEquals(1000.0, parse("1 km"))
        assertEquals(0.001, parse("1 kg"))
        assertEquals(1.609344, parse("1 mph"))
        assertEquals(0.90718474, parse("1 st"))
        assertEquals(1.0160469, parse("1 lt"))
        assertEquals(0.05080234544, parse("1 cwt"))
    }

    @Test fun `unknown units`() {
        assertNull(parse("1 bananas"))
        assertNull(parse("1 bananas 3 feet"))
        assertNull(parse("speed 1 mph"))
    }
}

private fun parse(string: String) = string.withOptionalUnitToDoubleOrNull()
