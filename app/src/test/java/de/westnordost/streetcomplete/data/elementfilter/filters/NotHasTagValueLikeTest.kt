package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.matches
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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

    @Test fun toStringMethod() {
        assertEquals("highway !~ .*", NotHasTagValueLike("highway", ".*").toString())
    }
}
