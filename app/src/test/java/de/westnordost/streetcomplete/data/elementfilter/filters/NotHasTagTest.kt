package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.matches
import org.junit.Assert.*
import org.junit.Test

class NotHasTagTest {

    @Test fun matches() {
        val f = NotHasTag("highway", "residential")

        assertFalse(f.matches(mapOf("highway" to "residential")))
        assertTrue(f.matches(mapOf("highway" to "residental")))
        assertTrue(f.matches(mapOf("hipway" to "residential")))
        assertTrue(f.matches(mapOf()))
    }

    @Test fun toOverpassQLString() {
        assertEquals(
            "[highway != residential]",
            NotHasTag("highway", "residential").toOverpassQLString()
        )
        assertEquals(
            "['high:way' != residential]",
            NotHasTag("high:way", "residential").toOverpassQLString()
        )
        assertEquals(
            "[highway != 'resi:dential']",
            NotHasTag("highway", "resi:dential").toOverpassQLString()
        )
    }
}