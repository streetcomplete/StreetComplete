package de.westnordost.streetcomplete.data.elementfilter

import kotlin.math.max
import kotlin.math.min

/** Convenience class to make it easier to go step by step through a string  */
class StringWithCursor(private val string: String) {
    var cursorPos = 0
        private set

    operator fun get(index: Int): Char? =
        if (index < string.length) string[index] else null

    /** Advances the cursor if [str] is the next string sequence at the cursor.
     *  @return whether the next string was the [str] */
    fun nextIsAndAdvance(str: String): Boolean {
        if (!nextIs(str)) return false
        advanceBy(str.length)
        return true
    }

    /** Advances the cursor if [c] is the next character at the cursor.
     *  @return whether the next character was [c] */
    fun nextIsAndAdvance(c: Char): Boolean {
        if (!nextIs(c)) return false
        advance()
        return true
    }

    /** Advances the cursor if [regex] matches the next string sequence at the cursor.
     *  @return match result */
    fun nextMatchesAndAdvance(regex: Regex): MatchResult? {
        val result = nextMatches(regex) ?: return null
        advanceBy(result.value.length)
        return result
    }

    /** @return whether the cursor position + [offs] is at the end of the string */
    fun isAtEnd(offs: Int = 0): Boolean = cursorPos + offs >= string.length

    /** @return the position relative to the cursor position at which [str] is found in the string.
     *  If not found, the position past the end of the string is returned */
    fun findNext(str: String, offs: Int = 0): Int = toDelta(string.indexOf(str, cursorPos + offs))
    /** @return the position relative to the cursor position at which [c] is found in the string.
     *  If not found, the position past the end of the string is returned */
    fun findNext(c: Char, offs: Int = 0): Int = toDelta(string.indexOf(c, cursorPos + offs))
    /** @return the position relative to the cursor position at which [regex] is found in the string.
     *  If not found, the position past the end of the string is returned */
    fun findNext(regex: Regex, offs: Int = 0): Int =
        toDelta(regex.find(string, cursorPos + offs)?.range?.first ?: -1)
    /** @return the position relative to the cursor position at which the given [block] returns true
     *  If not found, the position past the end of the string is returned */
    fun findNext(offs: Int = 0, block: (Char) -> Boolean): Int {
        for (i in cursorPos + offs..<string.length) {
            if (block(string[i])) {
                return toDelta(i)
            }
        }
        return string.length - cursorPos
    }

    /** Advance cursor by one
     *
     * @return character that was at the previous cursor position
     * @throws IndexOutOfBoundsException if cursor is already at the end
     */
    fun advance(): Char {
        if (isAtEnd()) throw IndexOutOfBoundsException()
        val result = string[cursorPos]
        cursorPos = min(string.length, cursorPos + 1)
        return result
    }

    /** Advance cursor by [x]
     *
     * @return the string between the previous cursor and the cursor position now. If cursor+[x] is
     * beyond the end of the string, the method will just return the string sequence until the end
     * of the string
     *
     * @throws IndexOutOfBoundsException if x < 0
     */
    fun advanceBy(x: Int): String {
        if (x < 0) throw IndexOutOfBoundsException()
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

    /** Advance the cursor until the [block] does not return true and return the number of
     *  characters advanced */
    fun advanceWhile(block: (Char) -> Boolean): Int {
        var i = 0
        while (cursorPos < string.length && block(string[cursorPos])) {
            ++cursorPos
            ++i
        }
        return i
    }

    /** Retreat cursor by [x]
     *
     * @throws IndexOutOfBoundsException if x < 0
     */
    fun retreatBy(x: Int) {
        if (x < 0) throw IndexOutOfBoundsException()
        cursorPos = max(0, cursorPos - x)
    }

    /** Retreat the cursor until the [block] does not return true and return the number of
     *  characters advanced */
    fun retreatWhile(block: (Char) -> Boolean): Int {
        var i = 0
        while (cursorPos > 0 && block(string[cursorPos - 1])) {
            --cursorPos
            ++i
        }
        return i
    }

    /** @return whether the next character at the cursor is [c] */
    fun nextIs(c: Char): Boolean = c == get(cursorPos)
    /** @return whether the next string at the cursor is [str] */
    fun nextIs(str: String): Boolean = string.startsWith(str, cursorPos)
    /** @return the match of [regex] at the next string sequence at the cursor */
    fun nextMatches(regex: Regex): MatchResult? = regex.matchAt(string, cursorPos)

    private fun toDelta(index: Int): Int =
        if (index == -1) string.length - cursorPos else index - cursorPos

    // good for debugging
    override fun toString(): String =
        string.substring(0, cursorPos) + "►" + string.substring(cursorPos)
}
