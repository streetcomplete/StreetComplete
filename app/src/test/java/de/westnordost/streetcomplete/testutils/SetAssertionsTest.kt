package de.westnordost.streetcomplete.testutils

import kotlin.test.Test
import kotlin.test.assertFailsWith

class SetAssertionsTest {
    @Test fun `assertSetsAreEqual`() {
        // No assertion failures
        assertSetsAreEqual(setOf("A"), setOf("A"))
        assertSetsAreEqual(emptySet<Int>(), emptySet<Int>())
        assertSetsAreEqual("clint eastwood".toSet(), "old west action".toSet())

        // Failures
        assertFailsWith<AssertionError> { assertSetsAreEqual(setOf("A"), setOf()) }
        assertFailsWith<AssertionError> { assertSetsAreEqual(setOf("A"), setOf("B")) }
        assertFailsWith<AssertionError> { assertSetsAreEqual(setOf("A", "B"), setOf("C", "A")) }
        assertFailsWith<AssertionError> { assertSetsAreEqual(setOf("A", "C"), setOf("B", "A")) }
    }
}
