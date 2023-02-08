package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.matches
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotHasTagLikeTest {

    @Test fun `matches regex key and value`() {
        val f = NotHasTagLike(".ame", "y.s")

        assertFalse(f.matches(mapOf("name" to "yes")))
        assertFalse(f.matches(mapOf("lame" to "yos")))
        assertTrue(f.matches(mapOf("lame" to "no")))
        assertTrue(f.matches(mapOf("good" to "yes")))
        assertTrue(f.matches(mapOf("neme" to "no")))
        assertTrue(f.matches(mapOf("names" to "yess"))) // only entire string is matched
        assertTrue(f.matches(mapOf()))
    }

    @Test fun `matches exact value of tag if without regexp`() {
        val f = NotHasTagLike("shop", "cheese")

        assertFalse(f.matches(mapOf("shop" to "cheese")))
        assertTrue(f.matches(mapOf("shop" to "cheese_frog_swamp")))
    }

    @Test fun `matches any exact value of pipelid list and otherwise without regexp`() {
        val f = NotHasTagLike("shop", "cheese|greengrocer")

        assertFalse(f.matches(mapOf("shop" to "cheese")))
        assertFalse(f.matches(mapOf("shop" to "greengrocer")))
        assertTrue(f.matches(mapOf("shop" to "cheese_frog_swamp")))
        assertTrue(f.matches(mapOf("shop" to "cheese|greengrocer")))
    }

    @Test fun toStringMethod() {
        assertEquals("~.ame !~ y.s", NotHasTagLike(".ame", "y.s").toString())
    }
}
