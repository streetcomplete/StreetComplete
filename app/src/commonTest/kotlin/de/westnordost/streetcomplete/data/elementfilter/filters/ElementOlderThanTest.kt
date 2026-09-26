package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.dateDaysAgo
import de.westnordost.streetcomplete.data.elementfilter.matches
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ElementOlderThanTest {
    val c = ElementOlderThan(RelativeDate(-10f))

    @Test fun `matches older element`() {
        assertTrue(c.matches(mapOf(), dateDaysAgo(12f)))
    }

    @Test fun `does not match newer element`() {
        assertFalse(c.matches(mapOf(), dateDaysAgo(8f)))
    }

    @Test fun toStringMethod() {
        assertEquals("older ${c.dateFilter}", c.toString())
    }
}
