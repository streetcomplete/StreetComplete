package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.dateDaysAgo
import de.westnordost.streetcomplete.data.elementfilter.matches
import de.westnordost.streetcomplete.osm.toCheckDateString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TagNewerThanTest {
    private val oldDate = dateDaysAgo(101f)
    private val newDate = dateDaysAgo(99f)

    val c = TagNewerThan("opening_hours", RelativeDate(-100f))

    @Test fun `does not match old element with tag`() {
        assertFalse(c.matches(mapOf("opening_hours" to "tag"), oldDate))
    }

    @Test fun `matches new element with tag`() {
        assertTrue(c.matches(mapOf("opening_hours" to "tag"), newDate))
    }

    @Test fun `matches old element with tag and new check_date`() {
        assertTrue(c.matches(mapOf(
            "opening_hours" to "tag",
            "opening_hours:check_date" to newDate.toCheckDateString()
        ), oldDate))

        assertTrue(c.matches(mapOf(
            "opening_hours" to "tag",
            "check_date:opening_hours" to newDate.toCheckDateString()
        ), oldDate))
    }

    @Test fun `matches old element with tag and new lastcheck`() {
        assertTrue(c.matches(mapOf(
            "opening_hours" to "tag",
            "opening_hours:lastcheck" to newDate.toCheckDateString()
        ), oldDate))

        assertTrue(c.matches(mapOf(
            "opening_hours" to "tag",
            "lastcheck:opening_hours" to newDate.toCheckDateString()
        ), oldDate))
    }

    @Test fun `matches old element with tag and new last_checked`() {
        assertTrue(c.matches(mapOf(
            "opening_hours" to "tag",
            "opening_hours:last_checked" to newDate.toCheckDateString()
        ), oldDate))

        assertTrue(c.matches(mapOf(
            "opening_hours" to "tag",
            "last_checked:opening_hours" to newDate.toCheckDateString()
        ), oldDate))
    }

    @Test fun `matches old element with tag and different check date tags of which only one is new`() {
        assertTrue(c.matches(mapOf(
            "opening_hours" to "tag",
            "opening_hours:last_checked" to oldDate.toCheckDateString(),
            "opening_hours:lastcheck" to newDate.toCheckDateString(),
            "opening_hours:check_date" to oldDate.toCheckDateString(),
            "last_checked:opening_hours" to oldDate.toCheckDateString(),
            "lastcheck:opening_hours" to oldDate.toCheckDateString(),
            "check_date:opening_hours" to oldDate.toCheckDateString()
        ), oldDate))
    }

    @Test fun toStringMethod() {
        assertEquals("opening_hours newer ${c.dateFilter}", c.toString())
    }
}
