package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.matches
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotHasKeyTest {

    @Test fun matches() {
        val f = NotHasKey("name")

        assertFalse(f.matches(mapOf("name" to "yes")))
        assertFalse(f.matches(mapOf("name" to "no")))
        assertTrue(f.matches(mapOf("neme" to "no")))
        assertTrue(f.matches(mapOf()))
    }
}
