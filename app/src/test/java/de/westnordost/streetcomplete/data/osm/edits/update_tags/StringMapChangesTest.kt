package de.westnordost.streetcomplete.data.osm.edits.update_tags

import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.on
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.verify
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
        val someMap = mutableMapOf("a" to "b")
        changes.applyTo(someMap)

        assertFalse(changes.hasConflictsTo(someMap))
    }

    @Test fun one() {
        val change: StringMapEntryChange = mock()
        on(change.toString()).thenReturn("x")

        val changes = StringMapChanges(listOf(change))
        val someMap = mutableMapOf("a" to "b")

        assertEquals("x", changes.toString())

        changes.applyTo(someMap)
        verify(change).applyTo(someMap)

        changes.hasConflictsTo(someMap)
        verify(change, atLeastOnce()).conflictsWith(someMap)
    }

    @Test fun two() {
        val change1: StringMapEntryChange = mock()
        on(change1.toString()).thenReturn("a")
        val change2: StringMapEntryChange = mock()
        on(change2.toString()).thenReturn("b")

        val changes = StringMapChanges(listOf(change1, change2))
        val someMap = mutableMapOf("a" to "b")

        assertEquals("a, b", changes.toString())

        changes.applyTo(someMap)
        verify(change1).applyTo(someMap)
        verify(change2).applyTo(someMap)

        changes.hasConflictsTo(someMap)
        verify(change1, atLeastOnce()).conflictsWith(someMap)
        verify(change2, atLeastOnce()).conflictsWith(someMap)
    }

    @Test
    fun `applying with conflict fails`() {
        val someMap = mutableMapOf<String, String>()

        val conflict: StringMapEntryChange = mock()
        on(conflict.conflictsWith(someMap)).thenReturn(true)

        val changes = StringMapChanges(listOf(conflict))

        assertFailsWith<IllegalStateException> {
            changes.applyTo(someMap)
        }
    }

    @Test fun getConflicts() {
        val someMap = emptyMap<String, String>()

        val conflict: StringMapEntryChange = mock()
        on(conflict.conflictsWith(someMap)).thenReturn(true)

        val conflict2: StringMapEntryChange = mock()
        on(conflict2.conflictsWith(someMap)).thenReturn(true)

        val changes = StringMapChanges(listOf(mock(), mock(), conflict, mock(), conflict2))

        changes.getConflictsTo(someMap)

        val conflicts = changes.getConflictsTo(someMap).toSet()
        val expectedConflicts = setOf(conflict, conflict2)
        assertEquals(expectedConflicts, conflicts)
    }

    @Test fun equals() {
        val a: StringMapEntryChange = mock()
        val b: StringMapEntryChange = mock()
        val one = StringMapChanges(listOf(a, b))
        val anotherOne = StringMapChanges(listOf(a, b))
        val two = StringMapChanges(listOf(b, a))

        assertEquals(one, anotherOne)
        // but the order does not matter
        assertEquals(one, two)
    }
}
