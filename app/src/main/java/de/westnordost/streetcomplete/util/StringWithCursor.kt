package de.westnordost.streetcomplete.util

import kotlin.math.max
import kotlin.math.min

/** Convenience class to make it easier to go step by step through a string  */
class StringWithCursor(val string: String) {
    var cursor = 0

    operator fun get(index: Int): Char? =
        if (index < string.length) string[index] else null

    /** Advances the cursor if [str] is the next string sequence at the cursor.
     *  @return whether the next string was the [str] */
    fun nextIsAndAdvance(str: String, ignoreCase: Boolean = false): Boolean {
        if (!nextIs(str, ignoreCase)) return false
        advanceBy(str.length)
        return true
    }

    /** Advances the cursor if [c] is the next character at the cursor.
     *  @return whether the next character was [c] */
    fun nextIsAndAdvance(c: Char, ignoreCase: Boolean = false): Boolean {
        if (!nextIs(c, ignoreCase)) return false
        advance()
        return true
    }

    inline fun nextIsAndAdvance(block: (Char) -> Boolean): Char? {
        if (!nextIs(block)) return null
        return advance()
    }

    /** Advances the cursor if [regex] matches the next string sequence at the cursor.
     *  @return match result */
    fun nextMatchesAndAdvance(regex: Regex): MatchResult? {
        val result = nextMatches(regex) ?: return null
        advanceBy(result.value.length)
        return result
    }

    /** @return whether the cursor position + [offs] is at the end of the string */
    fun isAtEnd(offs: Int = 0): Boolean = cursor + offs >= string.length

    /** @return the position relative to the cursor position at which [str] is found in the string.
     *  If not found, the position past the end of the string is returned */
    fun findNext(str: String, offs: Int = 0, ignoreCase: Boolean = false): Int =
        toDelta(string.indexOf(str, cursor + offs, ignoreCase))
    /** @return the position relative to the cursor position at which [c] is found in the string.
     *  If not found, the position past the end of the string is returned */
    fun findNext(c: Char, offs: Int = 0, ignoreCase: Boolean = false): Int =
        toDelta(string.indexOf(c, cursor + offs, ignoreCase))
    /** @return the position relative to the cursor position at which [regex] is found in the string.
     *  If not found, the position past the end of the string is returned */
    fun findNext(regex: Regex, offs: Int = 0): Int =
        toDelta(regex.find(string, cursor + offs)?.range?.first ?: -1)
    /** @return the position relative to the cursor position at which the given [block] returns true
     *  If not found, the position past the end of the string is returned */
    inline fun findNext(offs: Int = 0, block: (Char) -> Boolean): Int {
        for (i in cursor + offs..<string.length) {
            if (block(string[i])) {
                return i - cursor
            }
        }
        return string.length - cursor
    }

    /** Advance cursor by one
     *
     * @return character that was at the previous cursor position
     * @throws IndexOutOfBoundsException if cursor is already at the end
     */
    fun advance(): Char {
        if (isAtEnd()) throw IndexOutOfBoundsException()
        val result = string[cursor]
        cursor = min(string.length, cursor + 1)
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
        val end = cursor + x
        val result: String
        if (string.length < end) {
            result = string.substring(cursor)
            cursor = string.length
        } else {
            result = string.substring(cursor, end)
            cursor = end
        }
        return result
    }

    /** Retreat cursor by [x]
     *
     * @throws IndexOutOfBoundsException if x < 0
     */
    fun retreatBy(x: Int) {
        if (x < 0) throw IndexOutOfBoundsException()
        cursor = max(0, cursor - x)
    }

    /** @return whether the next character at the cursor is [c] */
    fun nextIs(c: Char, ignoreCase: Boolean = false): Boolean =
        get(cursor)?.equals(c, ignoreCase) == true

    /** @return whether the next string at the cursor is [str] */
    fun nextIs(str: String, ignoreCase: Boolean = false): Boolean =
        string.startsWith(str, cursor, ignoreCase)

    /** @return whether the [block] returns true for the next character */
    inline fun nextIs(block: (Char) -> Boolean): Boolean =
        get(cursor)?.let(block) == true

    /** @return the match of [regex] at the next string sequence at the cursor */
    fun nextMatches(regex: Regex): MatchResult? = regex.matchAt(string, cursor)

    /** Advance the cursor until the [block] does not return true and return the number of
     *  characters advanced */
    inline fun advanceWhile(block: (Char) -> Boolean): Int {
        var i = 0
        while (cursor < string.length && block(string[cursor])) {
            ++cursor
            ++i
        }
        return i
    }

    /** Retreat the cursor until the [block] does not return true and return the number of
     *  characters advanced */
    inline fun retreatWhile(block: (Char) -> Boolean): Int {
        var i = 0
        while (cursor > 0 && block(string[cursor - 1])) {
            --cursor
            ++i
        }
        return i
    }

    /** @return the next string that contains only characters where [block] returns true of the
     *  given [maxLength].
     *  Returns null if the word is either longer than that or there is no word at this position. */
    inline fun getNextWord(maxLength: Int? = null, block: (Char) -> Boolean): String? {
        var i = 0
        while (!isAtEnd(i) && block(string[cursor + i])) {
            ++i
            if (maxLength != null && i > maxLength) return null
        }
        return if (i == 0) null else string.substring(cursor, cursor + i)
    }

    inline fun getNextWordAndAdvance(maxLength: Int? = null, block: (Char) -> Boolean): String? {
        val result = getNextWord(maxLength, block) ?: return null
        cursor += result.length
        return result
    }

    private fun toDelta(index: Int): Int =
        if (index == -1) string.length - cursor else index - cursor

    // good for debugging
    override fun toString(): String =
        string.substring(0, cursor) + "►" + string.substring(cursor)
}
