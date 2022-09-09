package de.westnordost.streetcomplete.osm.address

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HouseNumbersParserKtTest {

    @Test
    fun `test all, I am lazy today`() {
        assertParses("1")
        assertParses("12345")
        assertParses("123a")
        assertParses("123B")
        assertParses("123 a")
        assertParses("123 / a")
        assertParses("123/ a")
        assertParses("123 /a")
        assertParses("123/1")
        assertParses("123 /1")
        assertParses("123/ 1")
        assertParses("123 / 1")

        assertParses("1,12,30")
        assertParses("1c-10a")
        assertParses("1D-10B")
        assertParses("1,3,5-8,12,21-24a")
        assertParses("1,3,5-8,12,21-24B")

        assertParses("10-4")

        assertParsingFails("10-b")
        assertParsingFails("-1")
        assertParsingFails("123456")
        assertParsingFails("123ad")
        assertParsingFails("123AE")
        assertParsingFails("123 1")
    }

    @Test fun `StructuredHouseNumber comparisons`() {
        val hs_1 = SimpleHouseNumber(1)
        val hs_4 = SimpleHouseNumber(4)
        val hs_4c = HouseNumberWithLetter(4, "", "c")
        val hs_4d = HouseNumberWithLetter(4, "", "d")
        val hs_1_4 = HouseNumberWithNumber(1, "", 4)
        val hs_1_6 = HouseNumberWithNumber(1, "", 6)

        assertTrue(hs_4 > hs_1)
        assertTrue(hs_1 < hs_4)

        assertFalse(hs_4 > hs_4c)
        assertFalse(hs_4 < hs_4c)
        assertTrue(hs_4d > hs_4c)
        assertTrue(hs_4c < hs_4d)

        assertFalse(hs_1 > hs_1_4)
        assertFalse(hs_1 < hs_1_4)
        assertTrue(hs_1_6 > hs_1_4)
        assertTrue(hs_1_4 < hs_1_6)
    }

    @Test fun `HouseNumbersPart comparisons`() {
        val hs_5 = single(SimpleHouseNumber(5))
        val hs_8 = single(SimpleHouseNumber(8))
        val hs_7to10 = range(SimpleHouseNumber(7), SimpleHouseNumber(10))
        val hs_10to7 = range(SimpleHouseNumber(10), SimpleHouseNumber(7))
        val hs_12to14 = range(SimpleHouseNumber(12), SimpleHouseNumber(14))
        val hs_9to50 = range(SimpleHouseNumber(9), SimpleHouseNumber(50))

        assertTrue(hs_5 < hs_8)
        assertTrue(hs_8 > hs_5)

        assertTrue(hs_5 < hs_7to10)
        assertTrue(hs_5 < hs_10to7)
        assertTrue(hs_7to10 > hs_5)
        assertTrue(hs_10to7 > hs_5)

        assertFalse(hs_8 < hs_7to10)
        assertFalse(hs_8 < hs_10to7)
        assertFalse(hs_7to10 > hs_8)
        assertFalse(hs_10to7 > hs_8)

        assertTrue(hs_10to7 < hs_12to14)
        assertTrue(hs_7to10 < hs_12to14)
        assertTrue(hs_12to14 > hs_10to7)
        assertTrue(hs_12to14 > hs_7to10)

        assertFalse(hs_9to50 > hs_7to10)
        assertFalse(hs_9to50 > hs_10to7)
        assertFalse(hs_9to50 < hs_7to10)
        assertFalse(hs_9to50 < hs_10to7)
    }
}

private fun range(
    start: StructuredHouseNumber,
    end: StructuredHouseNumber
) = HouseNumbersPartsRange(start, end)

private fun single(s: StructuredHouseNumber) =
    SingleHouseNumbersPart(s)

private fun assertParses(str: String) {
    assertEquals(str, parseHouseNumbers(str).toString())
}

private fun assertParsingFails(str: String) {
    assertNull(parseHouseNumbers(str))
}
