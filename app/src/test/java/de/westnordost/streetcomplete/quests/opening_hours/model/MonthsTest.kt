package de.westnordost.streetcomplete.quests.opening_hours.model

import org.junit.Test

import org.junit.Assert.*

class MonthsTest {

    @Test fun isSelectionEmpty() {
        assertTrue(months(0b000000000000).isSelectionEmpty())
        assertFalse(months(0b000100000000).isSelectionEmpty())
    }

    @Test fun `toString works`() {
        assertEquals("Jan-Feb", months(0b110000000000).toString())
        assertEquals("Feb-Apr", months(0b011100000000).toString())
        assertEquals("May-Jun,Aug-Sep,Nov", months(0b000011011010).toString())
        assertEquals("Dec-Jan", months(0b100000000001).toString())
        assertEquals("Jul-May", months(0b111110111111).toString())
    }

    private fun months(bits12: Int) = Months(bits12.toBitField(12))

    private fun Int.toBitField(bits: Int) = (bits-1 downTo 0).map { this and (1 shl it) != 0 }.toBooleanArray()
}
