package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.dateDaysAgo
import de.westnordost.streetcomplete.data.elementfilter.matches
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ElementNewerThanTest {
    val c = ElementNewerThan(RelativeDate(-10f))

    @Test fun `does not match older element`() {
        assertFalse(c.matches(mapOf(), dateDaysAgo(11f)))
    }

    @Test fun `matches newer element`() {
        assertTrue(c.matches(mapOf(), dateDaysAgo(9f)))
    }

    @Test fun `does not match element from same day`() {
        assertFalse(c.matches(mapOf(), dateDaysAgo(10f)))
    }

    @Test fun toStringMethod() {
        assertEquals("newer ${c.dateFilter}", c.toString())
    }
}
