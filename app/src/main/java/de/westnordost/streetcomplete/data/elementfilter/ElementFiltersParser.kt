package de.westnordost.streetcomplete.data.elementfilter

import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.elementfilter.filters.*
import de.westnordost.streetcomplete.data.meta.toCheckDate
import java.lang.NumberFormatException
import java.text.ParseException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min

/**
 * Compiles a string in filter syntax into a ElementFilterExpression. A string in filter syntax is
 * something like this:
 *
 * <tt>"ways with (highway = residential or highway = tertiary) and !name"</tt> (finds all
 * residential and tertiary roads that have no name)
 */

fun String.toElementFilterExpression(): ElementFilterExpression {
    // convert all white-spacey things to whitespaces so we do not have to deal with them later
    val cursor = StringWithCursor(replace("\\s".toRegex(), " "))

    return ElementFilterExpression(cursor.parseElementsDeclaration(), cursor.parseTags())
}

private const val WITH = "with"
private const val OR = "or"
private const val AND = "and"

private const val YEARS = "years"
private const val MONTHS = "months"
private const val WEEKS = "weeks"
private const val DAYS = "days"

private const val EQUALS = "="
private const val NOT_EQUALS = "!="
private const val LIKE = "~"
private const val NOT_LIKE = "!~"
private const val GREATER_THAN = ">"
private const val LESS_THAN = "<"
private const val GREATER_OR_EQUAL_THAN = ">="
private const val LESS_OR_EQUAL_THAN = "<="
private const val OLDER = "older"
private const val NEWER = "newer"
private const val TODAY = "today"
private const val PLUS = "+"
private const val MINUS = "-"

private val RESERVED_WORDS = arrayOf(WITH, OR, AND)
private val QUOTATION_MARKS = charArrayOf('"', '\'')
private val KEY_VALUE_OPERATORS = arrayOf( EQUALS, NOT_EQUALS, LIKE, NOT_LIKE )
private val COMPARISON_OPERATORS = arrayOf(
    GREATER_THAN, GREATER_OR_EQUAL_THAN,
    LESS_THAN, LESS_OR_EQUAL_THAN
)
// must be in that order because if ">=" would be after ">", parser would match ">" also when encountering ">="
private val OPERATORS = arrayOf(
    GREATER_OR_EQUAL_THAN,
    LESS_OR_EQUAL_THAN,
    GREATER_THAN,
    LESS_THAN,
    NOT_EQUALS,
    EQUALS,
    NOT_LIKE,
    LIKE,
    OLDER,
    NEWER
)

private val NUMBER_WORD_REGEX = Regex("(?:([0-9]+(?:\\.[0-9]*)?)|(\\.[0-9]+))(?:$| |\\))")

private fun String.stripQuotes() = replace("^[\"']|[\"']$".toRegex(), "")

private fun StringWithCursor.parseElementsDeclaration(): EnumSet<ElementsTypeFilter> {
    val result = ArrayList<ElementsTypeFilter>()
    result.add(parseElementDeclaration())
    while (nextIsAndAdvance(',')) {
        val element = parseElementDeclaration()
        if (result.contains(element)) {
            throw ParseException("Mentioned the same element type $element twice", cursorPos)
        }
        result.add(element)
    }
    // a little odd interface of EnumSet here
    return when(result.size) {
        1 -> EnumSet.of(result[0])
        2 -> EnumSet.of(result[0], result[1])
        3 -> EnumSet.of(result[0], result[1], result[2])
        else -> throw IllegalStateException()
    }

}

private fun StringWithCursor.parseElementDeclaration(): ElementsTypeFilter {
    expectAnyNumberOfSpaces()
    for (t in ElementsTypeFilter.values()) {
        val name = when(t) {
            ElementsTypeFilter.NODES -> "nodes"
            ElementsTypeFilter.WAYS -> "ways"
            ElementsTypeFilter.RELATIONS -> "relations"
        }
        if (nextIsAndAdvance(name)) {
            expectAnyNumberOfSpaces()
            return t
        }
    }
    throw ParseException(
        "Expected element types. Any of: nodes, ways or relations, separated by ','",
        cursorPos
    )
}

private fun StringWithCursor.parseTags(): BooleanExpression<ElementFilter, Element>? {
    // tags are optional...
    if (!nextIsAndAdvance(WITH)) {
        if (!isAtEnd()) {
            throw ParseException("Expected end of string or 'with' keyword", cursorPos)
        }
        return null
    }

    val builder = BooleanExpressionBuilder<ElementFilter, Element>()

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

        if (nextIsAndAdvance(OR)) {
            builder.addOr()
        } else if (nextIsAndAdvance(AND)) {
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

private fun StringWithCursor.parseTag(): ElementFilter {
    if (nextIsAndAdvance('!')) {
        expectAnyNumberOfSpaces()
        if (nextIsAndAdvance('~')) {
            return NotHasKeyLike(parseKey())
        } else {
            return NotHasKey(parseKey())
        }
    }

    if (nextIsAndAdvance('~')) {
        expectAnyNumberOfSpaces()
        val key = parseKey()
        expectAnyNumberOfSpaces()
        val operator = parseOperator()
        if (operator == null) {
            return HasKeyLike(key)
        } else if ("~" == operator) {
            expectAnyNumberOfSpaces()
            return HasTagLike(key, parseQuotableWord())
        }
        throw ParseException("Unexpected operator '$operator': The key prefix operator '~' must be used together with the binary operator '~'", cursorPos)
    }

    if (nextIsAndAdvance(OLDER)) {
        expectOneOrMoreSpaces()
        return ElementOlderThan(parseDate())
    }
    if (nextIsAndAdvance(NEWER)) {
        expectOneOrMoreSpaces()
        return ElementNewerThan(parseDate())
    }

    val key = parseKey()
    expectAnyNumberOfSpaces()
    val operator = parseOperator() ?: return HasKey(key)

    if (operator == OLDER) {
        expectOneOrMoreSpaces()
        return CombineFilters(HasKey(key), TagOlderThan(key, parseDate()))
    }
    if (operator == NEWER) {
        expectOneOrMoreSpaces()
        return CombineFilters(HasKey(key), TagNewerThan(key, parseDate()))
    }

    if (KEY_VALUE_OPERATORS.contains(operator)) {
        expectAnyNumberOfSpaces()
        val value = parseQuotableWord()

        when (operator) {
            EQUALS       -> return HasTag(key, value)
            NOT_EQUALS   -> return NotHasTag(key, value)
            LIKE         -> return HasTagValueLike(key, value)
            NOT_LIKE     -> return NotHasTagValueLike(key, value)
        }
    }

    if (COMPARISON_OPERATORS.contains(operator)) {
        expectAnyNumberOfSpaces()
        if (nextMatches(NUMBER_WORD_REGEX) != null) {
            val value = parseNumber()
            when(operator) {
                GREATER_THAN          -> return HasTagGreaterThan(key, value)
                GREATER_OR_EQUAL_THAN -> return HasTagGreaterOrEqualThan(key, value)
                LESS_THAN             -> return HasTagLessThan(key, value)
                LESS_OR_EQUAL_THAN    -> return HasTagLessOrEqualThan(key, value)
            }
        } else {
            val value = parseDate()
            when(operator) {
                GREATER_THAN          -> return HasDateTagGreaterThan(key, value)
                GREATER_OR_EQUAL_THAN -> return HasDateTagGreaterOrEqualThan(key, value)
                LESS_THAN             -> return HasDateTagLessThan(key, value)
                LESS_OR_EQUAL_THAN    -> return HasDateTagLessOrEqualThan(key, value)
            }
        }
        throw ParseException("must either be a number or a (relative) date", cursorPos)
    }
    throw ParseException("Unknown operator '$operator'", cursorPos)
}

private fun StringWithCursor.parseKey(): String {
    val reserved = nextIsReservedWord()
    if(reserved != null) {
        throw ParseException("A key cannot be named like the reserved word '$reserved', surround it with quotation marks", cursorPos)
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

private fun StringWithCursor.parseQuotableWord(): String {
    val length = findQuotableWordLength()
    if (length == 0) {
        throw ParseException("Missing value (dangling operator)", cursorPos)
    }
    return advanceBy(length).stripQuotes()
}

private fun StringWithCursor.parseWord(): String {
    val length = findWordLength()
    if (length == 0) {
        throw ParseException("Missing value (dangling operator)", cursorPos)
    }
    return advanceBy(length)
}

private fun StringWithCursor.parseNumber(): Float {
    val word = parseWord()
    try {
        return word.toFloat()
    } catch (e: NumberFormatException) {
        throw ParseException("Expected a number", cursorPos)
    }
}

private fun StringWithCursor.parseDate(): DateFilter {
    val length = findWordLength()
    if (length == 0) {
        throw ParseException("Missing date", cursorPos)
    }
    val word = advanceBy(length)
    if (word == TODAY) {
        var deltaDays = 0f
        if (nextIsAndAdvance(' ')) {
            expectAnyNumberOfSpaces()
            deltaDays = parseDeltaDurationInDays()
        }
        return RelativeDate(deltaDays)
    }

    val date = word.toCheckDate()
    if (date != null) {
        return FixedDate(date)
    }

    throw ParseException("Expected either a date (YYYY-MM-DD) or 'today'", cursorPos)
}

private fun StringWithCursor.parseDeltaDurationInDays(): Float {
    return when {
        nextIsAndAdvance(PLUS) -> {
            expectAnyNumberOfSpaces()
            +parseDurationInDays()
        }
        nextIsAndAdvance(MINUS) -> {
            expectAnyNumberOfSpaces()
            -parseDurationInDays()
        }
        else -> throw ParseException("Expected + or -", cursorPos)
    }
}

private fun StringWithCursor.parseDurationInDays(): Float {
    val duration = parseNumber()
    expectOneOrMoreSpaces()
    return when {
        nextIsAndAdvance(YEARS) -> 365.25f * duration
        nextIsAndAdvance(MONTHS) -> 30.5f * duration
        nextIsAndAdvance(WEEKS) -> 7 * duration
        nextIsAndAdvance(DAYS) -> duration
        else -> throw ParseException("Expected years, months, weeks or days", cursorPos)
    }
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

    length = findWordLength()
    for (o in OPERATORS) {
        val opLen = findNext(o)
        if (opLen < length!!) length = opLen
    }
    return length!!
}

private fun StringWithCursor.findWordLength(): Int =
    min(findNext(' '), findNext(')'))

private fun StringWithCursor.findQuotableWordLength(): Int =
    findQuotationLength() ?: findWordLength()

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
