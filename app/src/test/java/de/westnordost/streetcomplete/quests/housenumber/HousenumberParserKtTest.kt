package de.westnordost.streetcomplete.quests.housenumber

import org.junit.Assert.*
import org.junit.Test

class HousenumberParserKtTest {

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

        assertParsingFails("-1")
        assertParsingFails("123456")
        assertParsingFails("123ad")
        assertParsingFails("123AE")
        assertParsingFails("123 1")
    }
}

private fun assertParses(str: String) {
    assertEquals(str, parseHouseNumber(str).toString())
}

private fun assertParsingFails(str: String) {
    assertNull(parseHouseNumber(str))
}
