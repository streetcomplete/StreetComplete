package de.westnordost.streetcomplete.util

import org.junit.Test

import org.junit.Assert.*

class MultiIterableTest {
    @Test fun `empty list`() {
        val itb = MultiIterable<String>()
        itb.add(emptyList())
        assertEquals("", itb.joinToString(" "))
    }

    @Test fun `one list`() {
        val itb = MultiIterable<String>()
        itb.add(listOf("a", "b", "c"))
        assertEquals("a b c", itb.joinToString(" "))
    }

    @Test fun `list allows nulls`() {
        val itb = MultiIterable<String>()
        itb.add(listOf("a", null, "c"))
        assertEquals("a null c", itb.joinToString(" "))
    }

    @Test fun `multiple lists`() {
        val itb = MultiIterable<String>()
        itb.add(listOf("a", "b"))
        itb.add(listOf("c", "d"))
        assertEquals("a b c d", itb.joinToString(" "))
    }
}
