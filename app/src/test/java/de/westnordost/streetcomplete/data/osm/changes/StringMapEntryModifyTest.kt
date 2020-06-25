package de.westnordost.streetcomplete.data.osm.changes

import org.junit.Test

import org.junit.Assert.*

class StringMapEntryModifyTest {

    @Test fun modify() {
        val c = StringMapEntryModify("a", "b", "c")
        val m = mutableMapOf("a" to "b")

        assertEquals("MODIFY \"a\"=\"b\" -> \"a\"=\"c\"", c.toString())

        assertFalse(c.conflictsWith(m))
        c.applyTo(m)
        assertTrue(c.conflictsWith(m))
    }

    @Test fun reverse() {
        val modify = StringMapEntryModify("a", "b", "c")
        val reverseModify = modify.reversed()

        val m = mutableMapOf("a" to "b")

        modify.applyTo(m)
        reverseModify.applyTo(m)

        assertEquals(1, m.size)
        assertEquals("b", m["a"])
    }
}
