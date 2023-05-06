package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.dateDaysAgo
import de.westnordost.streetcomplete.data.elementfilter.matches
import de.westnordost.streetcomplete.osm.toCheckDateString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TagOlderThanTest {
    private val oldDate = dateDaysAgo(101f)
    private val newDate = dateDaysAgo(99f)

    val c = TagOlderThan("opening_hours", RelativeDate(-100f))

    @Test fun `matches old element with tag`() {
        assertTrue(c.matches(mapOf("opening_hours" to "tag"), oldDate))
    }

    @Test fun `does not match new element with tag`() {
        assertFalse(c.matches(mapOf("opening_hours" to "tag"), newDate))
    }

    @Test fun `matches new element with tag and old check_date`() {
        assertTrue(c.matches(mapOf(
            "opening_hours" to "tag",
            "opening_hours:check_date" to oldDate.toCheckDateString()
        ), newDate))

        assertTrue(c.matches(mapOf(
            "opening_hours" to "tag",
            "check_date:opening_hours" to oldDate.toCheckDateString()
        ), newDate))
    }

    @Test fun `matches new element with tag and old lastcheck`() {
        assertTrue(c.matches(mapOf(
            "opening_hours" to "tag",
            "opening_hours:lastcheck" to oldDate.toCheckDateString()
        ), newDate))

        assertTrue(c.matches(mapOf(
            "opening_hours" to "tag",
            "lastcheck:opening_hours" to oldDate.toCheckDateString()
        ), newDate))
    }

    @Test fun `matches new element with tag and old last_checked`() {
        assertTrue(c.matches(mapOf(
            "opening_hours" to "tag",
            "opening_hours:last_checked" to oldDate.toCheckDateString()
        ), newDate))

        assertTrue(c.matches(mapOf(
            "opening_hours" to "tag",
            "last_checked:opening_hours" to oldDate.toCheckDateString()
        ), newDate))
    }

    @Test fun `matches new element with tag and different check date tags of which only one is old`() {
        assertTrue(c.matches(mapOf(
            "opening_hours" to "tag",
            "opening_hours:last_checked" to newDate.toCheckDateString(),
            "opening_hours:lastcheck" to newDate.toCheckDateString(),
            "opening_hours:check_date" to newDate.toCheckDateString(),
            "last_checked:opening_hours" to oldDate.toCheckDateString(),
            "lastcheck:opening_hours" to newDate.toCheckDateString(),
            "check_date:opening_hours" to newDate.toCheckDateString()
        ), newDate))
    }

    @Test fun `does not match new element with tag only as a prefix`() {
        assertFalse(c.matches(mapOf("opening_hours:signed" to "tag"), newDate))
    }

    @Test fun `matches any old element without relevant tags`() {
        assertTrue(c.matches(mapOf("foo" to "bar"), oldDate))
        assertTrue(c.matches(mapOf(), oldDate))
    }

    @Test fun `does not match any new element without relevant tags`() {
        assertFalse(c.matches(mapOf("foo" to "bar"), newDate))
        assertFalse(c.matches(mapOf(), newDate))
    }

    @Test fun toStringMethod() {
        assertEquals("opening_hours older ${c.dateFilter}", c.toString())
    }
}
