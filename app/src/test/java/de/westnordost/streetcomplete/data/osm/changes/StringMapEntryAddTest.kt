package de.westnordost.streetcomplete.data.osm.changes

import org.junit.Test

import org.junit.Assert.*

class StringMapEntryAddTest {

    @Test fun add() {
        val c = StringMapEntryAdd("a", "b")
        val m = mutableMapOf<String, String>()

        assertEquals("ADD \"a\"=\"b\"", c.toString())

        assertFalse(c.conflictsWith(m))

        c.applyTo(m)
        assertEquals("b", m["a"])

        assertTrue(c.conflictsWith(m))
    }

    @Test fun reverse() {
        val m = HashMap<String, String>()

        val add = StringMapEntryAdd("a", "b")
        val reverseAdd = add.reversed()

        add.applyTo(m)
        reverseAdd.applyTo(m)

        assertTrue(m.isEmpty())
    }
}
