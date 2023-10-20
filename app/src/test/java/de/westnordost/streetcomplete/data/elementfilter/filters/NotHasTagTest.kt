package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.matches
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
