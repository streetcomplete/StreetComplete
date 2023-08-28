package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.matches
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
