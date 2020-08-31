package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.matches
import org.junit.Assert.*
import org.junit.Test

class HasTagLikeTest {

    @Test fun `matches regex key and value`() {
        val f = HasTagLike(".ame", "y.s")

        assertTrue(f.matches(mapOf("name" to "yes")))
        assertTrue(f.matches(mapOf("lame" to "yos")))
        assertFalse(f.matches(mapOf("lame" to "no")))
        assertFalse(f.matches(mapOf("good" to "yes")))
        assertFalse(f.matches(mapOf("neme" to "no")))
        assertFalse(f.matches(mapOf("names" to "yess")))
        assertFalse(f.matches(mapOf()))
    }

    @Test fun `to string`() {
        assertEquals(
            "[~'^(.ame)$' ~ '^(y.s)$']",
            HasTagLike(".ame","y.s").toOverpassQLString()
        )
    }
}