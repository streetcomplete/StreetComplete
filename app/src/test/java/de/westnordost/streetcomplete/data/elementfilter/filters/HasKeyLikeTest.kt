package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.matches
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HasKeyLikeTest {
    private val key = HasKeyLike("n.[ms]e")

    @Test fun matches() {
        assertTrue(key.matches(mapOf("name" to "adsf")))
        assertTrue(key.matches(mapOf("nase" to "fefff")))
        assertTrue(key.matches(mapOf("neme" to "no")))
        assertFalse(key.matches(mapOf("a name yo" to "no")))
        assertTrue(key.matches(mapOf("n(se" to "no")))
        assertFalse(key.matches(mapOf()))
    }

    @Test fun toStringMethod() {
        assertEquals("~n.[ms]e", key.toString())
    }
}
