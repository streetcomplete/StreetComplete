package de.westnordost.streetcomplete.util

import org.junit.Assert.*
import org.junit.Test

class ReverseIteratorTest {

    @Test fun reverse() {
        val it = ReverseIterator(mutableListOf("a", "b", "c"))
        assertEquals("c", it.next())
        assertEquals("b", it.next())
        assertEquals("a", it.next())
        assertFalse(it.hasNext())
    }
}
