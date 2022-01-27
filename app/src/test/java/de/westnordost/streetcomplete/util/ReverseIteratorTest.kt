package de.westnordost.streetcomplete.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
