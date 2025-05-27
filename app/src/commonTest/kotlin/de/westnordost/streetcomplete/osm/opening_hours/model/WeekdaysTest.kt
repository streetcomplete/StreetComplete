package de.westnordost.streetcomplete.osm.opening_hours.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WeekdaysTest {

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
}
