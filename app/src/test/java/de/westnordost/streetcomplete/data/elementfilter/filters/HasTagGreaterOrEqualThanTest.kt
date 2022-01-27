package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.matches
import org.junit.Assert.*
import org.junit.Test

class HasTagGreaterOrEqualThanTest {

    @Test fun matches() {
        val c = HasTagGreaterOrEqualThan("width", 3.5f)

        assertFalse(c.matches(mapOf()))
        assertFalse(c.matches(mapOf("width" to "broad")))
        assertTrue(c.matches(mapOf("width" to "3.6")))
        assertTrue(c.matches(mapOf("width" to "3.5")))
        assertFalse(c.matches(mapOf("width" to "3.4")))
    }
}
