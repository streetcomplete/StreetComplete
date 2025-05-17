package de.westnordost.streetcomplete.data.osm.edits.update_tags

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StringMapEntryModifyTest {

    @Test fun `conflicts if already changed to different value`() {
        assertTrue(StringMapEntryModify("a", "b", "c").conflictsWith(mutableMapOf("a" to "d")))
    }

    @Test fun `does not conflict if already changed to the new value`() {
        assertFalse(StringMapEntryModify("a", "b", "c").conflictsWith(mutableMapOf("a" to "c")))
    }

    @Test fun `does not conflict if not changed yet`() {
        assertFalse(StringMapEntryModify("a", "b", "c").conflictsWith(mutableMapOf("a" to "b")))
    }

    @Test fun `toString is as expected`() {
        assertEquals(
            "MODIFY \"a\"=\"b\" -> \"a\"=\"c\"",
            StringMapEntryModify("a", "b", "c").toString()
        )
    }

    @Test fun apply() {
        val m = mutableMapOf("a" to "b")
        StringMapEntryModify("a", "b", "c").applyTo(m)
        assertEquals("c", m["a"])
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
