package de.westnordost.streetcomplete.osm.opening_hours.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
}
