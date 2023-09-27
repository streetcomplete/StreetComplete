package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.matches
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotHasKeyTest {
    val c = NotHasKey("name")

    @Test fun matches() {
        assertFalse(c.matches(mapOf("name" to "yes")))
        assertFalse(c.matches(mapOf("name" to "no")))
        assertTrue(c.matches(mapOf("neme" to "no")))
        assertTrue(c.matches(mapOf()))
    }

    @Test fun toStringMethod() {
        assertEquals("!name", c.toString())
    }
}
