package de.westnordost.streetcomplete.data.osm.edits.update_tags

import de.westnordost.streetcomplete.testutils.verifyInvokedExactly
import de.westnordost.streetcomplete.testutils.verifyInvokedExactlyOnce
import io.mockative.Mock
import io.mockative.any
import io.mockative.classOf
import io.mockative.every
import io.mockative.mock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StringMapChangesTest {
    @Mock private lateinit var change: StringMapEntryAdd

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
        change = mock(classOf<StringMapEntryAdd>())
        every { change.toString() }.returns("x")
        every { change.conflictsWith(any()) }.returns(false)

        val changes = StringMapChanges(listOf(change))
        val someMap = mutableMapOf("a" to "b")

        assertEquals("x", changes.toString())

        changes.applyTo(someMap)
        verifyInvokedExactlyOnce { change.applyTo(someMap) }

        changes.hasConflictsTo(someMap)
        verifyInvokedExactly(2) { change.conflictsWith(someMap) }
    }

    @Test fun two() {
        val change1: StringMapEntryAdd = mock(classOf<StringMapEntryAdd>())
        every {change1.toString()}.returns("a")
        every {change1.key}.returns("a")
        every {change1.value}.returns("a")
        every { change1.conflictsWith(any()) }.returns(false)
        val change2: StringMapEntryAdd = mock(classOf<StringMapEntryAdd>())
        every {change2.toString()}.returns("b")
        every {change2.key}.returns("b")
        every {change2.value}.returns("b")
        every { change2.conflictsWith(any()) }.returns(false)

        val changes = StringMapChanges(listOf(change1, change2))
        val someMap = mutableMapOf("a" to "b")

        assertEquals("a, b", changes.toString())

        changes.applyTo(someMap)
        verifyInvokedExactlyOnce { change1.applyTo(someMap) }
        verifyInvokedExactlyOnce { change2.applyTo(someMap) }

        changes.hasConflictsTo(someMap)
        verifyInvokedExactly(2) { change1.conflictsWith(someMap) }
        verifyInvokedExactly(2) { change2.conflictsWith(someMap) }
    }

    @Test
    fun `applying with conflict fails`() {
        val someMap = mutableMapOf<String, String>()

        val conflict: StringMapEntryAdd = mock(classOf<StringMapEntryAdd>())
        every { conflict.conflictsWith(someMap) }.returns(true)

        val changes = StringMapChanges(listOf(conflict))

        assertFailsWith<IllegalStateException> {
            changes.applyTo(someMap)
        }
    }

    @Test fun getConflicts() {
        val someMap = emptyMap<String, String>()

        val conflict: StringMapEntryAdd = mock(classOf<StringMapEntryAdd>())
        every { conflict.conflictsWith(someMap) }.returns(true)
        every {conflict.key}.returns("a")
        every {conflict.value}.returns("a")

        val conflict2: StringMapEntryAdd = mock(classOf<StringMapEntryAdd>())
        every { conflict2.conflictsWith(someMap) }.returns(true)
        every {conflict2.key}.returns("b")
        every {conflict2.value}.returns("b")

        val noConflict1: StringMapEntryAdd = mock(classOf<StringMapEntryAdd>())
        every { noConflict1.conflictsWith(someMap) }.returns(false)
        every {noConflict1.key}.returns("c")
        every {noConflict1.value}.returns("c")

        val noConflict2: StringMapEntryAdd = mock(classOf<StringMapEntryAdd>())
        every { noConflict2.conflictsWith(someMap) }.returns(false)
        every {noConflict2.key}.returns("d")
        every {noConflict2.value}.returns("d")

        val noConflict3: StringMapEntryAdd = mock(classOf<StringMapEntryAdd>())
        every { noConflict3.conflictsWith(someMap) }.returns(false)
        every {noConflict3.key}.returns("e")
        every {noConflict3.value}.returns("e")

        val changes = StringMapChanges(listOf(noConflict1, noConflict2, conflict, noConflict3, conflict2))

        changes.getConflictsTo(someMap)

        val conflicts = changes.getConflictsTo(someMap).toSet()
        val expectedConflicts = setOf(conflict, conflict2)
        assertEquals(expectedConflicts, conflicts)
    }

    @Test fun equals() {
        val a: StringMapEntryAdd = mock(classOf<StringMapEntryAdd>())
        every {a.key}.returns("a")
        every {a.value}.returns("a")
        val b: StringMapEntryAdd = mock(classOf<StringMapEntryAdd>())
        every {b.key}.returns("b")
        every {b.value}.returns("b")
        val one = StringMapChanges(listOf(a, b))
        val anotherOne = StringMapChanges(listOf(a, b))
        val two = StringMapChanges(listOf(b, a))

        assertEquals(one, anotherOne)
        // but the order does not matter
        assertEquals(one, two)
    }
}
