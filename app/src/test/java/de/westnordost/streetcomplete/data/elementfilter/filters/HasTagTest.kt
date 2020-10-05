package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.matches
import org.junit.Assert.*
import org.junit.Test

class HasTagTest {

    @Test fun matches() {
        val f = HasTag("highway", "residential")

        assertTrue(f.matches(mapOf("highway" to "residential")))
        assertFalse(f.matches(mapOf("highway" to "residental")))
        assertFalse(f.matches(mapOf("hipway" to "residential")))
        assertFalse(f.matches(mapOf()))
    }

    @Test fun toOverpassQLString() {
        assertEquals(
            "[highway = residential]",
            HasTag("highway", "residential").toOverpassQLString()
        )
        assertEquals(
            "['high:way' = residential]",
            HasTag("high:way", "residential").toOverpassQLString()
        )
        assertEquals(
            "[highway = 'resi:dential']",
            HasTag("highway", "resi:dential").toOverpassQLString()
        )
    }
}