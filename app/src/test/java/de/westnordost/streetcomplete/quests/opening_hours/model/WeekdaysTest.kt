package de.westnordost.streetcomplete.quests.opening_hours.model

import org.junit.Test

import org.junit.Assert.*

class WeekdaysTest {

    @Test fun intersects() {
        assertFalse(weekdays(0b10101010).intersects(weekdays(0b01010101)))
        assertTrue(weekdays(0b00000010).intersects(weekdays(0b00000010)))
    }

    @Test fun isSelectionEmpty() {
        assertTrue(weekdays(0b00000000).isSelectionEmpty())
        assertFalse(weekdays(0b00010000).isSelectionEmpty())
    }

    @Test fun `toString works`() {
        assertEquals("Mo,Tu", weekdays(0b11000000).toString())
        assertEquals("Tu-Th", weekdays(0b01110000).toString())
        assertEquals("Tu-Th,Sa,Su,PH", weekdays(0b01110111).toString())
        assertEquals("Su,Mo", weekdays(0b10000010).toString())
        assertEquals("Sa-Th,PH", weekdays(0b11110111).toString())
    }

    private fun weekdays(bits8: Int) = Weekdays(bits8.toBitField(8))

    private fun Int.toBitField(bits: Int) = (bits-1 downTo 0).map { this and (1 shl it) != 0 }.toBooleanArray()
}
