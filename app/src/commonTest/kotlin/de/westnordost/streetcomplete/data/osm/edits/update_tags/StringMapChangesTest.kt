package de.westnordost.streetcomplete.data.osm.edits.update_tags

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StringMapChangesTest {

    @Test fun empty() {
        val changes = StringMapChanges(emptyList())
        assertEquals("", changes.toString())
        assertTrue(changes.changes.isEmpty())

        // executable without error:
        val map = mutableMapOf("a" to "b")
        changes.applyTo(map)

        assertFalse(changes.hasConflictsTo(map))
    }

    @Test fun one() {
        val changes = StringMapChanges(listOf(StringMapEntryAdd("x", "y")))
        val map = mutableMapOf("a" to "b")

        assertFalse(changes.hasConflictsTo(map))
        changes.applyTo(map)

        assertEquals("ADD \"x\"=\"y\"", changes.toString())
        assertEquals(mapOf("a" to "b", "x" to "y"), map)
    }

    @Test fun two() {
        val changes = StringMapChanges(listOf(
            StringMapEntryAdd("a", "b"),
            StringMapEntryAdd("x", "y")
        ))
        val map = mutableMapOf<String, String>()
        changes.applyTo(map)

        assertEquals("ADD \"a\"=\"b\", ADD \"x\"=\"y\"", changes.toString())
        assertEquals(mapOf("a" to "b", "x" to "y"), map)
    }

    @Test fun `applying with conflict fails`() {
        val changes = StringMapChanges(listOf(StringMapEntryAdd("a", "c")))
        val map = mutableMapOf("a" to "b")

        assertTrue(changes.hasConflictsTo(map))
        assertFailsWith<IllegalStateException> {
            changes.applyTo(map)
        }
    }

    @Test fun getConflicts() {
        val change1 = StringMapEntryAdd("a", "c")
        val change2 = StringMapEntryAdd("x", "y")
        val changes = StringMapChanges(listOf(change1, change2))
        val map = mutableMapOf("a" to "b", "x" to "z")

        assertTrue(changes.hasConflictsTo(map))
        assertEquals(
            setOf(change1, change2),
            changes.getConflictsTo(map).toSet()
        )
    }

    @Test fun equals() {
        val change1 = StringMapEntryAdd("a", "c")
        val change2 = StringMapEntryAdd("x", "y")
        val changes1 = StringMapChanges(listOf(change1, change2))
        val changes2 = StringMapChanges(listOf(change2, change1))

        assertEquals(changes2, changes1)
    }
}
