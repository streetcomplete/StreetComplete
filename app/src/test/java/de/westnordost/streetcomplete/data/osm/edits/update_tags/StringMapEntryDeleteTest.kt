package de.westnordost.streetcomplete.data.osm.edits.update_tags

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StringMapEntryDeleteTest {

    @Test fun `conflicts if already changed to different value`() {
        assertTrue(StringMapEntryDelete("a", "b").conflictsWith(mutableMapOf("a" to "c")))
    }

    @Test fun `does not conflict if already deleted key`() {
        assertFalse(StringMapEntryDelete("a", "b").conflictsWith(mutableMapOf()))
    }

    @Test fun `does not conflict if not deleted yet`() {
        assertFalse(StringMapEntryDelete("a", "b").conflictsWith(mutableMapOf("a" to "b")))
    }

    @Test fun `toString is as expected`() {
        assertEquals(
            "DELETE \"a\"=\"b\"",
            StringMapEntryDelete("a", "b").toString()
        )
    }

    @Test fun apply() {
        val m = mutableMapOf("a" to "b")
        StringMapEntryDelete("a", "b").applyTo(m)
        assertFalse(m.containsKey("a"))
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
