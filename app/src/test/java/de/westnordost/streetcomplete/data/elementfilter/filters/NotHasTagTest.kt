package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.matches
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotHasTagTest {
    val c = NotHasTag("highway", "residential")

    @Test fun matches() {
        assertFalse(c.matches(mapOf("highway" to "residential")))
        assertTrue(c.matches(mapOf("highway" to "residental")))
        assertTrue(c.matches(mapOf("hipway" to "residential")))
        assertTrue(c.matches(mapOf()))
    }

    @Test fun toStringMethod() {
        assertEquals("highway != residential", c.toString())
    }
}
