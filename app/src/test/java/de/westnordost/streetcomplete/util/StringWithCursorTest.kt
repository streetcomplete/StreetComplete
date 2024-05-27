package de.westnordost.streetcomplete.util

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
        assertEquals(0, x.cursor)
        assertEquals('a', x.advance())
        assertEquals(1, x.cursor)
        assertEquals('b', x.advance())
        assertEquals(2, x.cursor)

        assertFailsWith<IndexOutOfBoundsException> {
            x.advance()
        }
    }

    @Test fun advanceBy() {
        val x = StringWithCursor("wundertuete")
        assertEquals("wunder", x.advanceBy(6))
        assertEquals(6, x.cursor)
        assertEquals("", x.advanceBy(0))
        assertEquals(6, x.cursor)
        assertFailsWith<IndexOutOfBoundsException> {
            x.advanceBy(-1)
        }

        assertEquals("tuete", x.advanceBy(99999))
        assertEquals(11, x.cursor)
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
        assertEquals(2, x.cursor)
        assertFalse(x.nextIsAndAdvance("te"))
        x.advanceBy(3)
        assertTrue(x.nextIsAndAdvance("23"))
        assertEquals(7, x.cursor)
        assertTrue(x.isAtEnd())
    }

    @Test fun nextIsAndAdvance_ignoreCase() {
        val x = StringWithCursor("teST123")
        assertTrue(x.nextIsAndAdvance("TesT", true))
    }

    @Test fun nextIsAndAdvanceChar() {
        val x = StringWithCursor("test1  23")
        assertFalse(x.nextIsAndAdvance('T'))
        assertTrue(x.nextIsAndAdvance('T', ignoreCase = true))
        assertEquals(1, x.cursor)
        assertFalse(x.nextIsAndAdvance('t'))
        x.advanceBy(3)
        assertTrue(x.nextIsAndAdvance('1'))
        assertEquals(5, x.cursor)
        assertFalse(x.nextIsAndAdvance('2'))
    }

    @Test fun findNext() {
        val x = StringWithCursor("abc abc")
        assertEquals("abc abc".length, x.findNext("wurst"))

        assertEquals(0, x.findNext("abc"))
        assertEquals("abc abc".length, x.findNext("ABC"))
        x.advance()
        assertEquals(3, x.findNext("abc"))
    }

    @Test fun findNext_ignoreCase() {
        val x = StringWithCursor("abc abc")
        assertEquals(1, x.findNext("Bc A", 0, true))
    }

    @Test fun findNextChar() {
        val x = StringWithCursor("abc abc")
        assertEquals("abc abc".length, x.findNext('x'))
        assertEquals("abc abc".length, x.findNext('A'))

        assertEquals(0, x.findNext('a'))
        x.advance()
        assertEquals(3, x.findNext('a'))
    }

    @Test fun findNextChar_ignoreCase() {
        val x = StringWithCursor("abc abc")
        assertEquals(1, x.findNext('B', 0, true))
    }

    @Test fun findNextRegex() {
        val x = StringWithCursor("abc abc")
        assertEquals("abc abc".length, x.findNext("x".toRegex()))
        assertEquals(0, x.findNext("[a-z]{3}".toRegex()))
        x.advance()
        assertEquals(3, x.findNext("[a-z]{3}".toRegex()))
    }

    @Test fun findNextFn() {
        val x = StringWithCursor("abc abc")
        assertEquals("abc abc".length, x.findNext { it == 'x' })

        assertEquals(0, x.findNext { it == 'a' })
        x.advance()
        assertEquals(3, x.findNext { it == 'a' })
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
        assertFalse(x.nextIs('A', false))
        assertTrue(x.nextIs('A', true))
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
        assertFalse(x.nextIs("AB"))
        assertFalse(x.nextIs("bc"))
        x.advance()
        assertTrue(x.nextIs("bc"))
        x.advance()
        assertTrue(x.nextIs("c"))
        x.advance()
        assertFalse(x.nextIs("c"))
    }

    @Test fun nextIsString_ignoreCase() {
        val x = StringWithCursor("abc")
        assertTrue(x.nextIs("AB", true))
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
        assertEquals(4, x.cursor)
        assertNull(x.nextMatchesAndAdvance(Regex("[a-z]")))
        assertNull(x.nextMatchesAndAdvance(Regex("[0-9]{3}")))
        assertNotNull(x.nextMatchesAndAdvance(Regex("[0-9]{2}")))
        assertTrue(x.isAtEnd())
    }

    @Test fun advanceWhile() {
        val x = StringWithCursor("hello you   !")
        assertEquals(0, x.advanceWhile { it == ' ' })
        x.advanceBy(5)
        assertEquals(1, x.advanceWhile { it == ' ' })
        x.advanceBy(3)
        assertEquals(3, x.advanceWhile { it == ' ' })
    }

    @Test fun retreatWhile() {
        val x = StringWithCursor(" ello you   !")
        x.advanceBy(13)
        assertEquals(0, x.retreatWhile { it == ' ' })
        x.retreatBy(1)
        assertEquals(3, x.retreatWhile { it == ' ' })
        x.retreatBy(3)
        assertEquals(1, x.retreatWhile { it == ' ' })
        x.retreatBy(4)
        assertEquals(1, x.retreatWhile { it == ' ' })
    }

    @Test fun toStringMethod() {
        val x = StringWithCursor("ab")
        assertEquals("►ab", x.toString())
        x.advance()
        assertEquals("a►b", x.toString())
        x.advance()
        assertEquals("ab►", x.toString())
    }

    @Test fun getNextWord() {
        val x = StringWithCursor("abc9def ghi")
        val isLetter: (Char) -> Boolean = { it in 'a'..'z' }
        assertEquals(null, x.getNextWord(2, isLetter))
        assertEquals("abc", x.getNextWord(null, isLetter))
        assertEquals("abc", x.getNextWord(3, isLetter))
        x.advanceBy(1)
        assertEquals("bc", x.getNextWord(null, isLetter))
        assertEquals("bc", x.getNextWord(2, isLetter))
        x.advanceBy(2)
        assertEquals(null, x.getNextWord(null, isLetter))
        x.advanceBy(1)
        assertEquals("def", x.getNextWord(10, isLetter))
        assertEquals("def", x.getNextWord(null, isLetter))
        x.advanceBy(4)
        assertEquals("ghi", x.getNextWord(null, isLetter))
    }
}
