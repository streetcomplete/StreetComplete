package de.westnordost.streetcomplete.data.osm.changes

import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on
import org.junit.Test

import org.junit.Assert.*
import org.mockito.Mockito.*

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

    @Test(expected = IllegalStateException::class)
    fun `applying with conflict fails`() {
        val someMap = mutableMapOf<String, String>()

        val conflict: StringMapEntryChange = mock()
        on(conflict.conflictsWith(someMap)).thenReturn(true)

        val changes = StringMapChanges(listOf(conflict))

        changes.applyTo(someMap)
    }

    @Test fun getConflicts() {
        val someMap = emptyMap<String, String>()

        val conflict: StringMapEntryChange = mock()
        on(conflict.conflictsWith(someMap)).thenReturn(true)
        
        val changes = StringMapChanges(listOf(mock(), mock(), conflict, mock(), conflict))

        changes.getConflictsTo(someMap)

        val it = changes.getConflictsTo(someMap).iterator()

        assertSame(conflict, it.next())
        assertSame(conflict, it.next())
    }
}
