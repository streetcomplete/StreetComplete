package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.matches
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HasTagLessOrEqualThanTest {
    val c = HasTagLessOrEqualThan("width", 3.5f)

    @Test fun matches() {
        assertFalse(c.matches(mapOf()))
        assertFalse(c.matches(mapOf("width" to "broad")))
        assertFalse(c.matches(mapOf("width" to "3.6")))
        assertTrue(c.matches(mapOf("width" to "3.5")))
        assertTrue(c.matches(mapOf("width" to "3.4")))
    }

    @Test fun toStringMethod() {
        assertEquals("width <= 3.5", c.toString())
    }
}
