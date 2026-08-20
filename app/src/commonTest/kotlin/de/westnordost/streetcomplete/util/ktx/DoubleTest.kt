package de.westnordost.streetcomplete.util.ktx

import kotlin.test.Test
import kotlin.test.assertEquals

class DoubleTest {
    @Test
    fun testFormatPadded() {
        assertEquals("12.30", 12.3.formatPadded(2))
        assertEquals("12.30", 12.301.formatPadded(2))
        assertEquals("12.40", 12.40.formatPadded(2)) // typical rounding error candidate
        assertEquals("12.80", 12.80.formatPadded(2))
        assertEquals("16", 16.0.formatPadded(2, true))
    }
}
