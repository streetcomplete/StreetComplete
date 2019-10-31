package de.westnordost.streetcomplete.data.osm.tql

import java.text.ParseException
import java.util.ArrayList
import java.util.Locale
import kotlin.math.min

/**
 * Compiles a string in filter syntax into a TagFilterExpression. A string in filter syntax is
 * something like this:
 *
 * <tt>"ways with (highway = residential or highway = tertiary) and !name"</tt> (finds all
 * residential and tertiary roads that have no name)
 */
class FiltersParser {
    fun parse(input: String): TagFilterExpression {
        // convert all white-spacey things to whitespaces so we do not have to deal with them later
        val cursor = StringWithCursor(input.replace("\\s".toRegex(), " "), Locale.US)

        return TagFilterExpression(cursor.parseElementsDeclaration(), cursor.parseTags())
    }
}

private const val WITH = "with"
private const val OR = "or"
private const val AND = "and"
private const val AROUND = "around"

private val RESERVED_WORDS = arrayOf(WITH, OR, AND, AROUND)
private val QUOTATION_MARKS = charArrayOf('"', '\'')
private val OPERATORS = arrayOf("=", "!=", "~", "!~")

private fun String.stripQuotes() = replace("^[\"']|[\"']$".toRegex(), "")

private fun StringWithCursor.parseElementsDeclaration(): List<ElementsTypeFilter> {
    val result = ArrayList<ElementsTypeFilter>()
    result.add(parseElementDeclaration())
    while (nextIsAndAdvance(',')) {
        val element = parseElementDeclaration()
        if (result.contains(element)) {
            throw ParseException("Mentioned the same element type $element twice", cursorPos)
        }
        result.add(element)
    }
    return result
}

private fun StringWithCursor.parseElementDeclaration(): ElementsTypeFilter {
    expectAnyNumberOfSpaces()
    for (t in ElementsTypeFilter.values()) {
        if (nextIsAndAdvanceIgnoreCase(t.tqlName)) {
            expectAnyNumberOfSpaces()
            return t
        }
    }
    throw ParseException(
        "Expected element types. Any of: nodes, ways or relations, separated by ','",
        cursorPos
    )
}

private fun StringWithCursor.parseTags(): BooleanExpression<TagFilter, Tags>? {
    // tags are optional...
    if (!nextIsAndAdvanceIgnoreCase(WITH)) {
        if (!isAtEnd()) {
            throw ParseException("Expected end of string or 'with' keyword", cursorPos)
        }
        return null
    }

    val builder = BooleanExpressionBuilder<TagFilter, Tags>()

    do {
        // if it has no bracket, there must be at least one whitespace
        if (!parseBrackets('(', builder)) {
            throw ParseException("Expected a whitespace or bracket before the tag", cursorPos)
        }

        builder.addValue(parseTag())

        // parseTag() might have "eaten up" a whitespace after the key in expectation of an
        // operator.
        var separated = previousIs(' ')
        separated = separated or parseBrackets(')', builder)

        if (isAtEnd()) break

        // same as with the opening bracket, only that if the string is over, its okay
        if (!separated) {
            throw ParseException("Expected a whitespace or bracket after the tag", cursorPos)
        }

        if (nextIsAndAdvanceIgnoreCase(OR)) {
            builder.addOr()
        } else if (nextIsAndAdvanceIgnoreCase(AND)) {
            builder.addAnd()
        } else
            throw ParseException("Expected end of string, 'and' or 'or'", cursorPos)

    } while (true)

    try {
        return builder.build()
    } catch (e: IllegalStateException) {
        throw ParseException(e.message, cursorPos)
    }
}

private fun StringWithCursor.parseBrackets(bracket: Char, expr: BooleanExpressionBuilder<*,*>): Boolean {
    var characterCount = expectAnyNumberOfSpaces()
    var previousCharacterCount: Int
    do {
        previousCharacterCount = characterCount
        if (nextIsAndAdvance(bracket)) {
            try {
                if (bracket == '(')      expr.addOpenBracket()
                else if (bracket == ')') expr.addCloseBracket()
            } catch (e: IllegalStateException) {
                throw ParseException(e.message, cursorPos)
            }

            characterCount++
        }
        characterCount += expectAnyNumberOfSpaces()
    } while (characterCount > previousCharacterCount)

    return characterCount > 0
}

private fun StringWithCursor.parseTag(): TagFilter {
    if (nextIsAndAdvance('!')) {
        expectAnyNumberOfSpaces()
        return NotHasKey(parseKey())
    }

    if (nextIsAndAdvance('~')) {
        expectAnyNumberOfSpaces()
        val key = parseKey()
        expectAnyNumberOfSpaces()
        val operator = parseOperator()
        if ("~" != operator) {
            throw ParseException(
                "Unexpected operator '$operator': The key prefix operator '~' must be used together with the binary operator '~'",
                cursorPos
            )
        }
        expectAnyNumberOfSpaces()
        return HasTagLike(key, parseValue())
    }

    val key = parseKey()
    expectAnyNumberOfSpaces()
    val operator = parseOperator() ?: return HasKey(key)

    expectAnyNumberOfSpaces()
    val value = parseValue()

    when (operator) {
        "=" -> return HasTag(key, value)
        "!=" -> return NotHasTag(key, value)
        "~" -> return HasTagValueLike(key, value)
        "!~" -> return NotHasTagValueLike(key, value)
    }
    throw ParseException("Unknown operator '$operator'", cursorPos)
}

private fun StringWithCursor.parseKey(): String {
    val reserved = nextIsReservedWord()
    if(reserved != null) {
        throw ParseException(
            "A key cannot be named like the reserved word '$reserved', surround it with quotation marks",
            cursorPos
        )
    }

    val length = findKeyLength()
    if (length == 0) {
        throw ParseException("Missing key (dangling prefix operator)", cursorPos)
    }
    return advanceBy(length).stripQuotes()
}

private fun StringWithCursor.parseOperator(): String? {
    return OPERATORS.firstOrNull { nextIsAndAdvance(it) }
}

private fun StringWithCursor.parseValue(): String {
    val length = findValueLength()
    if (length == 0) {
        throw ParseException("Missing value (dangling operator)", cursorPos)
    }
    return advanceBy(length).stripQuotes()
}

private fun StringWithCursor.expectAnyNumberOfSpaces(): Int {
    var count = 0
    while (nextIsAndAdvance(' ')) count++
    return count
}

private fun StringWithCursor.expectOneOrMoreSpaces(): Int {
    if (!nextIsAndAdvance(' '))
        throw ParseException("Expected a whitespace", cursorPos)
    return expectAnyNumberOfSpaces() + 1
}

private fun StringWithCursor.nextIsReservedWord(): String? {
    return RESERVED_WORDS.firstOrNull {
        nextIsIgnoreCase(it) && (isAtEnd(it.length) || findNext(' ', it.length) == it.length)
    }
}

private fun StringWithCursor.findKeyLength(): Int {
    var length = findQuotationLength()
    if (length != null) return length

    length = min(findNext(' '), findNext(')'))
    for (o in OPERATORS) {
        val opLen = findNext(o)
        if (opLen < length!!) length = opLen
    }
    return length!!
}

private fun StringWithCursor.findValueLength(): Int {
    return findQuotationLength() ?: min(findNext(' '), findNext(')'))
}

private fun StringWithCursor.findQuotationLength(): Int? {
    for (quot in QUOTATION_MARKS) {
        if (nextIs(quot)) {
            val length = findNext(quot, 1)
            if (isAtEnd(length))
                throw ParseException("Did not close quotation marks", cursorPos - 1)
            // +1 because we want to include the closing quotation mark
            return length + 1
        }
    }
    return null
}
