package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.matches
import org.junit.Assert.*
import org.junit.Test

class HasTagGreaterThanTest {

    @Test fun matches() {
        val c = HasTagGreaterThan("width", 3.5f)

        assertFalse(c.matches(mapOf()))
        assertFalse(c.matches(mapOf("width" to "broad")))
        assertTrue(c.matches(mapOf("width" to "3.6")))
        assertFalse(c.matches(mapOf("width" to "3.5")))
        assertFalse(c.matches(mapOf("width" to "3.4")))
    }

    @Test fun `to string`() {
        assertEquals(
            "[width](if: number(t['width']) > 3.5)",
            HasTagGreaterThan("width", 3.5f).toOverpassQLString()
        )
        assertEquals(
            "['wid th'](if: number(t['wid th']) > 3.5)",
            HasTagGreaterThan("wid th", 3.5f).toOverpassQLString()
        )
    }
}