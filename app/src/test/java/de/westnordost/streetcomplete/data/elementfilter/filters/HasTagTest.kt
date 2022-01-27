package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.matches
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HasTagTest {

    @Test fun matches() {
        val f = HasTag("highway", "residential")

        assertTrue(f.matches(mapOf("highway" to "residential")))
        assertFalse(f.matches(mapOf("highway" to "residental")))
        assertFalse(f.matches(mapOf("hipway" to "residential")))
        assertFalse(f.matches(mapOf()))
    }
}
