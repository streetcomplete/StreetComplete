package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.matches
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.datetime.LocalDate

class HasDateTagGreaterThanTest {
    private val date = LocalDate(2000, 11, 11)
    private val c = HasDateTagGreaterThan("check_date", FixedDate(date))

    @Test fun matches() {
        assertFalse(c.matches(mapOf()))
        assertFalse(c.matches(mapOf("check_date" to "bla")))
        assertTrue(c.matches(mapOf("check_date" to "2000-11-12")))
        assertFalse(c.matches(mapOf("check_date" to "2000-11-11")))
        assertFalse(c.matches(mapOf("check_date" to "2000-11-10")))
    }

    @Test fun toStringMethod() {
        assertEquals("check_date > $date", c.toString())
    }
}
