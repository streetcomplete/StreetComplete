package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.matches
import org.junit.Assert.*
import org.junit.Test

class HasTagValueLikeTest {

    @Test fun `matches like dot`() {
        val f = HasTagValueLike("highway",".esidential")

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

    @Test fun `groups values properly`() {
        val f = HasTagValueLike("highway", "residential|unclassified")

        assertEquals(
            "[highway ~ '^(residential|unclassified)$']",
            f.toOverpassQLString()
        )
    }

    @Test fun `key value to string`() {
        val f = HasTagValueLike("highway",".*")
        assertEquals(
            "[highway ~ '^(.*)$']",
            f.toOverpassQLString()
        )
    }
}