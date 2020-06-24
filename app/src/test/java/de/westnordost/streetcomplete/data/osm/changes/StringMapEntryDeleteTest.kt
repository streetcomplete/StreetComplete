package de.westnordost.streetcomplete.data.osm.changes

import org.junit.Test

import org.junit.Assert.*

class StringMapEntryDeleteTest {

    @Test fun delete() {
        val c = StringMapEntryDelete("a", "b")
        val m = mutableMapOf("a" to "c")

        assertEquals("DELETE \"a\"=\"b\"", c.toString())

        assertTrue(c.conflictsWith(m))
        m["a"] = "b"
        assertFalse(c.conflictsWith(m))

        c.applyTo(m)
        assertFalse(m.containsKey("a"))
        assertTrue(c.conflictsWith(m))
    }

    @Test fun reverse() {
        val m = mutableMapOf("a" to "b")

        val delete = StringMapEntryDelete("a", "b")
        val reverseDelete = delete.reversed()

        delete.applyTo(m)
        reverseDelete.applyTo(m)

        assertEquals(1, m.size)
        assertEquals("b", m["a"])
    }
}
