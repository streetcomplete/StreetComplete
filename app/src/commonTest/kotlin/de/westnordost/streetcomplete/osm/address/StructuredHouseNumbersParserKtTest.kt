package de.westnordost.streetcomplete.osm.address

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StructuredHouseNumbersParserKtTest {

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
        assertParses("1,3,24a")
        assertParses("1,3,24B")

        assertParses("10-4")
        assertParses("10-99")

        assertParsingFails("10-b")
        assertParsingFails("-1")
        assertParsingFails("123456")
        assertParsingFails("123ad")
        assertParsingFails("123AE")
        assertParsingFails("123 1")
    }
}

private fun assertParses(str: String) {
    assertEquals(str, parseHouseNumbers(str).toString())
}

private fun assertParsingFails(str: String) {
    assertNull(parseHouseNumbers(str))
}
