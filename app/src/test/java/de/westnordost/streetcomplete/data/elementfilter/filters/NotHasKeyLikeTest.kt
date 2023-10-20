package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.matches
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NotHasKeyLikeTest {

    @Test fun matches() {
        val key = NotHasKeyLike("n.[ms]e")

        assertFalse(key.matches(mapOf("name" to "adsf")))
        assertFalse(key.matches(mapOf("nase" to "fefff")))
        assertFalse(key.matches(mapOf("neme" to "no")))
        assertTrue(key.matches(mapOf("a name yo" to "no", "another name yo" to "no")))
        assertFalse(key.matches(mapOf("n(se" to "no")))
        assertTrue(key.matches(mapOf()))
    }

    @Test fun toStringMethod() {
        assertEquals("!~n.[ms]e", NotHasKeyLike("n.[ms]e").toString())
    }
}
