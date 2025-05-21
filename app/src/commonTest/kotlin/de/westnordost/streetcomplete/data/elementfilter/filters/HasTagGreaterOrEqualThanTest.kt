package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.matches
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HasTagGreaterOrEqualThanTest {
    private val c = HasTagGreaterOrEqualThan("width", 3.5f)

    @Test fun matches() {
        assertFalse(c.matches(mapOf()))
        assertFalse(c.matches(mapOf("width" to "broad")))
        assertTrue(c.matches(mapOf("width" to "3.6")))
        assertTrue(c.matches(mapOf("width" to "3.5")))
        assertFalse(c.matches(mapOf("width" to "3.4")))
    }

    @Test fun toStringMethod() {
        assertEquals("width >= 3.5", c.toString())
    }
}
