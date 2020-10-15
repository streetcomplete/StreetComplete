package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.matches
import org.junit.Assert.*
import org.junit.Test

class NotHasTagValueLikeTest {

    @Test fun `matches not like dot`() {
        val f = NotHasTagValueLike("highway", ".*")

        assertFalse(f.matches(mapOf("highway" to "anything")))
        assertTrue(f.matches(mapOf()))
    }

    @Test fun `matches not like or`() {
        val f = NotHasTagValueLike("noname", "yes")

        assertFalse(f.matches(mapOf("noname" to "yes")))
        assertTrue(f.matches(mapOf("noname" to "no")))
        assertTrue(f.matches(mapOf()))
    }

    @Test fun `groups values properly`() {
        val f = NotHasTagValueLike("highway", "residential|unclassified")

        assertEquals(
            "[highway !~ '^(residential|unclassified)$']",
            f.toOverpassQLString()
        )
    }

    @Test fun `key not value to string`() {
        val f = NotHasTagValueLike("highway", ".*")

        assertEquals(
            "[highway !~ '^(.*)$']",
            f.toOverpassQLString()
        )
    }
}