package de.westnordost.streetcomplete.util

import org.junit.Test

import org.junit.Assert.assertEquals

class FlattenIterableTest {
    @Test fun `empty list`() {
        val itb = FlattenIterable(String::class.java)
        itb.add(emptyList<String>())
        assertEquals("", itb.joinToString(" "))
    }

    @Test fun `already flat list`() {
        val itb = FlattenIterable(String::class.java)
        itb.add(listOf("a", "b", "c"))
        assertEquals("a b c", itb.joinToString(" "))
    }

    @Test fun `list allows nulls`() {
        val itb = FlattenIterable(String::class.java)
        itb.add(listOf("a", null, "c"))
        assertEquals("a null c", itb.joinToString(" "))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `list with different types fails`() {
        val itb = FlattenIterable(String::class.java)
        itb.add(listOf("a", 4))
        itb.joinToString(" ")
    }

    @Test fun `nested list`() {
        val itb = FlattenIterable(String::class.java)
        itb.add(listOf("a", listOf("b", "c"), "d"))
        assertEquals("a b c d", itb.joinToString(" "))
    }

    @Test fun `deeper nested list`() {
        val itb = FlattenIterable(String::class.java)
        itb.add(listOf("a", listOf("b", listOf("c", "d")), "e"))
        assertEquals("a b c d e", itb.joinToString(" "))
    }

    @Test fun `multiple lists`() {
        val itb = FlattenIterable(String::class.java)
        itb.add(listOf("a", "b", listOf("c", "d")))
        itb.add(listOf("e", "f"))
        assertEquals("a b c d e f", itb.joinToString(" "))
    }
}
