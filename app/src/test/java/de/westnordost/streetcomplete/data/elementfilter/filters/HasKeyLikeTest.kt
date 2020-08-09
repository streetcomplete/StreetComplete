package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.matches
import org.junit.Assert.*
import org.junit.Test

class HasKeyLikeTest {

    @Test fun matches() {
        val key = HasKeyLike("n.[ms]e")

        assertTrue(key.matches(mapOf("name" to "adsf")))
        assertTrue(key.matches(mapOf("nase" to "fefff")))
        assertTrue(key.matches(mapOf("neme" to "no")))
        assertFalse(key.matches(mapOf("a name yo" to "no")))
        assertTrue(key.matches(mapOf("n(se" to "no")))
        assertFalse(key.matches(mapOf()))
    }

    @Test fun toOverpassQLString() {
        assertEquals(
            "[~'^(na[ms]e)$' ~ '.*']",
            HasKeyLike("na[ms]e").toOverpassQLString()
        )
    }
}