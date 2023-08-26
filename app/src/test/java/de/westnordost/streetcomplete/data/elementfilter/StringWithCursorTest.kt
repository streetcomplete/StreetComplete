package de.westnordost.streetcomplete.data.elementfilter

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StringWithCursorTest {
    @Test fun advance() {
        val x = StringWithCursor("ab")
        assertEquals(0, x.cursorPos)
        assertEquals('a', x.advance())
        assertEquals(1, x.cursorPos)
        assertEquals('b', x.advance())
        assertEquals(2, x.cursorPos)

        assertFailsWith<IndexOutOfBoundsException> {
            x.advance()
        }
    }

    @Test fun advanceBy() {
        val x = StringWithCursor("wundertuete")
        assertEquals("wunder", x.advanceBy(6))
        assertEquals(6, x.cursorPos)
        assertEquals("", x.advanceBy(0))
        assertEquals(6, x.cursorPos)
        assertFailsWith<IndexOutOfBoundsException> {
            x.advanceBy(-1)
        }

        assertEquals("tuete", x.advanceBy(99999))
        assertEquals(11, x.cursorPos)
        assertTrue(x.isAtEnd())
    }

    @Test fun retreatBy() {
        val x = StringWithCursor("wundertuete")
        x.advanceBy(6)
        x.retreatBy(999)
        assertEquals("wunder", x.advanceBy(6))
        x.retreatBy(3)
        assertEquals("dertue", x.advanceBy(6))
        assertFailsWith<IndexOutOfBoundsException> {
            x.retreatBy(-1)
        }
    }

    @Test fun nextIsAndAdvance() {
        val x = StringWithCursor("test123")
        assertTrue(x.nextIsAndAdvance("te"))
        assertEquals(2, x.cursorPos)
        assertFalse(x.nextIsAndAdvance("te"))
        x.advanceBy(3)
        assertTrue(x.nextIsAndAdvance("23"))
        assertEquals(7, x.cursorPos)
        assertTrue(x.isAtEnd())
    }

    @Test fun nextIsAndAdvanceChar() {
        val x = StringWithCursor("test123")
        assertTrue(x.nextIsAndAdvance('t'))
        assertEquals(1, x.cursorPos)
        assertFalse(x.nextIsAndAdvance('t'))
        x.advanceBy(3)
        assertTrue(x.nextIsAndAdvance('1'))
        assertEquals(5, x.cursorPos)
    }

    @Test fun findNext() {
        val x = StringWithCursor("abc abc")
        assertEquals("abc abc".length, x.findNext("wurst"))

        assertEquals(0, x.findNext("abc"))
        x.advance()
        assertEquals(3, x.findNext("abc"))
    }

    @Test fun findNextChar() {
        val x = StringWithCursor("abc abc")
        assertEquals("abc abc".length, x.findNext('x'))

        assertEquals(0, x.findNext('a'))
        x.advance()
        assertEquals(3, x.findNext('a'))
    }

    @Test fun findNextRegex() {
        val x = StringWithCursor("abc abc")
        assertEquals("abc abc".length, x.findNext("x".toRegex()))
        assertEquals(0, x.findNext("[a-z]{3}".toRegex()))
        x.advance()
        assertEquals(3, x.findNext("[a-z]{3}".toRegex()))
    }

    @Test fun isAtEnd() {
        val x = StringWithCursor("abc")
        assertFalse(x.isAtEnd(2))
        assertTrue(x.isAtEnd(3))
        assertTrue(x.isAtEnd(4))
        x.advanceBy(3)
        assertFalse(x.isAtEnd(-1))
        assertTrue(x.isAtEnd(0))
        assertTrue(x.isAtEnd(1))
    }

    @Test fun nextIsChar() {
        val x = StringWithCursor("abc")
        assertTrue(x.nextIs('a'))
        assertFalse(x.nextIs('b'))
        x.advance()
        assertTrue(x.nextIs('b'))
        x.advance()
        assertTrue(x.nextIs('c'))
        x.advance()
        assertFalse(x.nextIs('c'))
    }

    @Test fun nextIsString() {
        val x = StringWithCursor("abc")
        assertTrue(x.nextIs("abc"))
        assertTrue(x.nextIs("ab"))
        assertFalse(x.nextIs("bc"))
        x.advance()
        assertTrue(x.nextIs("bc"))
        x.advance()
        assertTrue(x.nextIs("c"))
        x.advance()
        assertFalse(x.nextIs("c"))
    }

    @Test fun nextMatchesString() {
        val x = StringWithCursor("abc123")
        assertNotNull(x.nextMatches(Regex("abc[0-9]")))
        assertNotNull(x.nextMatches(Regex("abc[0-9]{3}")))
        assertNull(x.nextMatches(Regex("abc[0-9]{4}")))
        assertNull(x.nextMatches(Regex("bc[0-9]")))
        x.advance()
        assertNotNull(x.nextMatches(Regex("bc[0-9]")))
    }

    @Test fun nextMatchesStringAndAdvance() {
        val x = StringWithCursor("abc123")
        assertNotNull(x.nextMatchesAndAdvance(Regex("abc[0-9]")))
        assertEquals(4, x.cursorPos)
        assertNull(x.nextMatchesAndAdvance(Regex("[a-z]")))
        assertNull(x.nextMatchesAndAdvance(Regex("[0-9]{3}")))
        assertNotNull(x.nextMatchesAndAdvance(Regex("[0-9]{2}")))
        assertTrue(x.isAtEnd())
    }

    @Test fun toStringMethod() {
        val x = StringWithCursor("ab")
        assertEquals("►ab", x.toString())
        x.advance()
        assertEquals("a►b", x.toString())
        x.advance()
        assertEquals("ab►", x.toString())
    }
}
