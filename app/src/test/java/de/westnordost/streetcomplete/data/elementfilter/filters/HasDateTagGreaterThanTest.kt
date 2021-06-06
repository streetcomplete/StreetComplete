package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.matches
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class HasDateTagGreaterThanTest {
    private val date = LocalDate.of(2000,11,11)

    @Test fun matches() {
        val c = HasDateTagGreaterThan("check_date", FixedDate(date))

        assertFalse(c.matches(mapOf()))
        assertFalse(c.matches(mapOf("check_date" to "bla")))
        assertTrue(c.matches(mapOf("check_date" to "2000-11-12")))
        assertFalse(c.matches(mapOf("check_date" to "2000-11-11")))
        assertFalse(c.matches(mapOf("check_date" to "2000-11-10")))
    }

    @Test fun `to string`() {
        val eq = HasDateTagGreaterThan("check_date", FixedDate(date))
        assertEquals(
            "[check_date](if: date(t['check_date']) > date('2000-11-11'))",
            eq.toOverpassQLString()
        )
    }
}
