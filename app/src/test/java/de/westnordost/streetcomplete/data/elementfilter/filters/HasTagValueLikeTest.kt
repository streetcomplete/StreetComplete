package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.matches
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HasTagValueLikeTest {

    @Test fun `matches like dot`() {
        val f = HasTagValueLike("highway", ".esidential")

        assertTrue(f.matches(mapOf("highway" to "residential")))
        assertTrue(f.matches(mapOf("highway" to "wesidential")))
        assertFalse(f.matches(mapOf("highway" to "rresidential")))
        assertFalse(f.matches(mapOf()))
    }

    @Test fun `matches like or`() {
        val f = HasTagValueLike("highway", "residential|unclassified")

        assertTrue(f.matches(mapOf("highway" to "residential")))
        assertTrue(f.matches(mapOf("highway" to "unclassified")))
        assertFalse(f.matches(mapOf("highway" to "blub")))
        assertFalse(f.matches(mapOf()))
    }

    @Test fun `matches like character class`() {
        val f = HasTagValueLike("maxspeed", "([1-9]|[1-2][0-9]|3[0-5]) mph")

        assertTrue(f.matches(mapOf("maxspeed" to "1 mph")))
        assertTrue(f.matches(mapOf("maxspeed" to "5 mph")))
        assertTrue(f.matches(mapOf("maxspeed" to "15 mph")))
        assertTrue(f.matches(mapOf("maxspeed" to "25 mph")))
        assertTrue(f.matches(mapOf("maxspeed" to "35 mph")))
        assertFalse(f.matches(mapOf("maxspeed" to "40 mph")))
        assertFalse(f.matches(mapOf("maxspeed" to "45 mph")))
        assertFalse(f.matches(mapOf("maxspeed" to "135 mph")))
        assertFalse(f.matches(mapOf()))
    }

    @Test fun toStringMethod() {
        assertEquals("highway ~ .esidential", HasTagValueLike("highway", ".esidential").toString())
    }
}
