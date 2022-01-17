package de.westnordost.streetcomplete.data.osm.edits.update_tags

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StringMapEntryAddTest {

    @Test fun `conflicts if already added with different value`() {
        assertTrue(StringMapEntryAdd("a", "b").conflictsWith(mutableMapOf("a" to "c")))
    }

    @Test fun `does not conflict if already added with different value`() {
        assertFalse(StringMapEntryAdd("a", "b").conflictsWith(mutableMapOf("a" to "b")))
    }

    @Test fun `does not conflict if not added yet`() {
        assertFalse(StringMapEntryAdd("a", "b").conflictsWith(mutableMapOf()))
    }

    @Test fun `toString is as expected`() {
        assertEquals(
            "ADD \"a\"=\"b\"",
            StringMapEntryAdd("a", "b").toString()
        )
    }

    @Test fun apply() {
        val m = mutableMapOf<String, String>()
        StringMapEntryAdd("a", "b").applyTo(m)
        assertEquals("b", m["a"])
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
