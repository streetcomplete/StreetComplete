package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.matches
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HasKeyTest {
    private val key = HasKey("name")

    @Test fun matches() {
        assertTrue(key.matches(mapOf("name" to "yes")))
        assertTrue(key.matches(mapOf("name" to "no")))
        assertFalse(key.matches(mapOf("neme" to "no")))
        assertFalse(key.matches(mapOf()))
    }

    @Test fun toStringMethod() {
        assertEquals("name", key.toString())
    }
}
