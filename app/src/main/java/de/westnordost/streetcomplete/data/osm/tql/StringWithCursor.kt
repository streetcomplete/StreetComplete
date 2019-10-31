package de.westnordost.streetcomplete.data.osm.tql

import java.util.*
import kotlin.math.min

/** Convenience class to make it easier to go step by step through a string  */
class StringWithCursor(private val string: String, private val locale: Locale) {
    var cursorPos = 0
        private set

    private val char: Char?
        get() = if (cursorPos < string.length) string[cursorPos] else null

    /** Advances the cursor if str is the next thing at the cursor.
     *  Returns whether the next string was the str */
    fun nextIsAndAdvance(str: String): Boolean {
        if (!nextIs(str)) return false
        advanceBy(str.length)
        return true
    }

    fun nextIsAndAdvance(c: Char): Boolean {
        if (!nextIs(c)) return false
        advance()
        return true
    }

    /** Advances the cursor if str or str.toUpperCase() is the next thing at the cursor
     *
     *  Returns whether the next string was the str or str.toUpperCase
     */
    fun nextIsAndAdvanceIgnoreCase(str: String): Boolean {
        if (!nextIsIgnoreCase(str)) return false
        advanceBy(str.length)
        return true
    }

    /** returns whether the cursor reached the end */
    fun isAtEnd(x: Int = 0): Boolean = cursorPos + x >= string.length
    fun findNext(str: String): Int = toDelta(string.indexOf(str, cursorPos))
    fun findNext(c: Char, offs: Int = 0): Int = toDelta(string.indexOf(c, cursorPos + offs))

    /** Advance cursor by one and return the character that was at that position
     *
     * throws IndexOutOfBoundsException if cursor is already at the end
     */
    fun advance(): Char {
        val result = string[cursorPos]
        cursorPos = min(string.length, cursorPos + 1)
        return result
    }

    /** Advance cursor by x and return the string that inbetween the two positions.
     * If cursor+x is beyond the end of the string, the method will just return the string until
     * the end of the string
     *
     * throws IndexOutOfBoundsException if x < 0
     */
    fun advanceBy(x: Int): String {
        val end = cursorPos + x
        val result: String
        if (string.length < end) {
            result = string.substring(cursorPos)
            cursorPos = string.length
        } else {
            result = string.substring(cursorPos, end)
            cursorPos = end
        }
        return result
    }

    fun previousIs(c: Char): Boolean = c == string[cursorPos - 1]
    fun nextIs(c: Char): Boolean = c == char
    fun nextIs(str: String): Boolean = string.startsWith(str, cursorPos)
    fun nextIsIgnoreCase(str: String): Boolean =
        nextIs(str.toLowerCase(locale)) || nextIs(str.toUpperCase(locale))

    private fun toDelta(index: Int): Int =
        if (index == -1) string.length - cursorPos else index - cursorPos

    // good for debugging
    override fun toString(): String =
        string.substring(0, cursorPos) + "►" + string.substring(cursorPos)
}
