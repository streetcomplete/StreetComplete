package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.matches
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HasTagLikeTest {

    @Test fun `matches regex key and value`() {
        val f = HasTagLike(".ame", "y.s")

        assertTrue(f.matches(mapOf("name" to "yes")))
        assertTrue(f.matches(mapOf("lame" to "yos")))
        assertFalse(f.matches(mapOf("lame" to "no")))
        assertFalse(f.matches(mapOf("good" to "yes")))
        assertFalse(f.matches(mapOf("neme" to "no")))
        assertFalse(f.matches(mapOf("names" to "yess"))) // only entire string is matched
        assertFalse(f.matches(mapOf()))
    }

    @Test fun `matches exact value of tag if without regexp`() {
        val f = HasTagLike("shop", "cheese")

        assertTrue(f.matches(mapOf("shop" to "cheese")))
        assertFalse(f.matches(mapOf("shop" to "cheese_frog_swamp")))
    }

    @Test fun `matches any exact value of pipelid list and otherwise without regexp`() {
        val f = HasTagLike("shop", "cheese|greengrocer")

        assertTrue(f.matches(mapOf("shop" to "cheese")))
        assertTrue(f.matches(mapOf("shop" to "greengrocer")))
        assertFalse(f.matches(mapOf("shop" to "cheese_frog_swamp")))
        assertFalse(f.matches(mapOf("shop" to "cheese|greengrocer")))
    }

    @Test fun toStringMethod() {
        assertEquals("~.ame ~ y.s", HasTagLike(".ame", "y.s").toString())
    }
}
