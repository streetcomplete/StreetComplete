package de.westnordost.streetcomplete.util.ktx

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StringTest {
    private val testString = "abc foo foo"

    @Test fun `indicesOf returns correct indices`() {
        assertEquals(0, testString.indicesOf("abc").elementAt(0))
        assertEquals(listOf(4, 8), testString.indicesOf("foo").toList())
        assertEquals(emptyList(), testString.indicesOf("xyz").toList())
    }

    @Test fun `indicesOf throws error if index is not found`() {
        assertFailsWith<IndexOutOfBoundsException> {
            testString.indicesOf("xyz").elementAt(0)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            testString.indicesOf("abc").elementAt(1)
        }
    }
}
