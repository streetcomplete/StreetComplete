package de.westnordost.streetcomplete.data.elementfilter.filters

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RegexOrSetTest {
    @Test fun pipesMatch() {
        val r = RegexOrSet.from("a|b|c")
        assertTrue(r.matches("a"))
        assertTrue(r.matches("b"))
        assertTrue(r.matches("c"))
        assertFalse(r.matches("d"))
        assertFalse(r.matches("a|b"))
    }
}
