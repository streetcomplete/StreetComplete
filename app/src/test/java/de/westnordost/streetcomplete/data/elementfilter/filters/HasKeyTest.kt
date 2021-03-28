package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.matches
import org.junit.Assert.*
import org.junit.Test

class HasKeyTest {

    @Test fun matches() {
        val key = HasKey("name")

        assertTrue(key.matches(mapOf("name" to "yes")))
        assertTrue(key.matches(mapOf("name" to "no")))
        assertFalse(key.matches(mapOf("neme" to "no")))
        assertFalse(key.matches(mapOf()))
    }

    @Test fun toOverpassQLString() {
        assertEquals(
            "[name]",
            HasKey("name").toOverpassQLString()
        )
        assertEquals(
            "['name:old']",
            HasKey("name:old").toOverpassQLString()
        )
    }
}