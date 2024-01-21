package de.westnordost.streetcomplete.data.elementfilter

import de.westnordost.streetcomplete.data.elementfilter.filters.CombineFilters
import de.westnordost.streetcomplete.data.elementfilter.filters.DateFilter
import de.westnordost.streetcomplete.data.elementfilter.filters.ElementFilter
import de.westnordost.streetcomplete.data.elementfilter.filters.ElementNewerThan
import de.westnordost.streetcomplete.data.elementfilter.filters.ElementOlderThan
import de.westnordost.streetcomplete.data.elementfilter.filters.FixedDate
import de.westnordost.streetcomplete.data.elementfilter.filters.HasDateTagGreaterOrEqualThan
import de.westnordost.streetcomplete.data.elementfilter.filters.HasDateTagGreaterThan
import de.westnordost.streetcomplete.data.elementfilter.filters.HasDateTagLessOrEqualThan
import de.westnordost.streetcomplete.data.elementfilter.filters.HasDateTagLessThan
import de.westnordost.streetcomplete.data.elementfilter.filters.HasKey
import de.westnordost.streetcomplete.data.elementfilter.filters.HasKeyLike
import de.westnordost.streetcomplete.data.elementfilter.filters.HasTag
import de.westnordost.streetcomplete.data.elementfilter.filters.HasTagGreaterOrEqualThan
import de.westnordost.streetcomplete.data.elementfilter.filters.HasTagGreaterThan
import de.westnordost.streetcomplete.data.elementfilter.filters.HasTagLessOrEqualThan
import de.westnordost.streetcomplete.data.elementfilter.filters.HasTagLessThan
import de.westnordost.streetcomplete.data.elementfilter.filters.HasTagLike
import de.westnordost.streetcomplete.data.elementfilter.filters.HasTagValueLike
import de.westnordost.streetcomplete.data.elementfilter.filters.NotHasKey
import de.westnordost.streetcomplete.data.elementfilter.filters.NotHasKeyLike
import de.westnordost.streetcomplete.data.elementfilter.filters.NotHasTag
import de.westnordost.streetcomplete.data.elementfilter.filters.NotHasTagLike
import de.westnordost.streetcomplete.data.elementfilter.filters.NotHasTagValueLike
import de.westnordost.streetcomplete.data.elementfilter.filters.RelativeDate
import de.westnordost.streetcomplete.data.elementfilter.filters.TagNewerThan
import de.westnordost.streetcomplete.data.elementfilter.filters.TagOlderThan
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.osm.toCheckDate
import kotlin.math.min

/**
 * Compiles a string in filter syntax into a ElementFilterExpression. A string in filter syntax is
 * something like this:
 *
 * <tt>"ways with (highway = residential or highway = tertiary) and !name"</tt> (finds all
 * residential and tertiary roads that have no name)
 */
fun String.toElementFilterExpression(): ElementFilterExpression {
    val cursor = StringWithCursor(this)
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
private const val NOT = "!"
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
private val KEY_VALUE_OPERATORS = setOf(EQUALS, NOT_EQUALS, LIKE, NOT_LIKE)
private val COMPARISON_OPERATORS = setOf(
    GREATER_THAN, GREATER_OR_EQUAL_THAN,
    LESS_THAN, LESS_OR_EQUAL_THAN
)
// must be in that order because if ">=" would be after ">", parser would match ">" also when encountering ">="
private val OPERATORS = linkedSetOf(
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

private val NUMBER_WITH_OPTIONAL_UNIT_REGEX = Regex("[0-9]+'[0-9]+\"|(?:[0-9]*\\.[0-9]+|[0-9]+)[a-z/'\"]*")
private val ESCAPED_QUOTE_REGEX = Regex("\\\\(['\"])")
private val WHITESPACE_REGEX = Regex("\\s")
private val WHITESPACES_REGEX = Regex("\\s*")
private val NOT_WITH_WHITESPACE_AND_OPENING_BRACE = Regex("!\\s*\\(")

private fun StringWithCursor.parseElementsDeclaration(): Set<ElementsTypeFilter> {
    val result = LinkedHashSet<ElementsTypeFilter>()
    do {
        val element = parseElementDeclaration()
        if (result.contains(element)) {
            throw ParseException("Mentioned the same element type $element twice", cursorPos)
        }
        result.add(element)
    } while (nextIsAndAdvance(','))
    return result
}

private fun StringWithCursor.parseElementDeclaration(): ElementsTypeFilter {
    expectAnyNumberOfSpaces()
    for (t in ElementsTypeFilter.entries) {
        val name = when (t) {
            ElementsTypeFilter.NODES -> "nodes"
            ElementsTypeFilter.WAYS -> "ways"
            ElementsTypeFilter.RELATIONS -> "relations"
        }
        if (nextIsAndAdvance(name)) {
            expectAnyNumberOfSpaces()
            return t
        }
    }
    throw ParseException("Expected element types. Any of: nodes, ways or relations, separated by ','", cursorPos)
}

private fun StringWithCursor.parseTags(): BooleanExpression<ElementFilter, Element>? {
    // tags are optional...
    if (!nextIsAndAdvance(WITH)) {
        if (!isAtEnd()) {
            throw ParseException("Expected end of string or '$WITH' keyword", cursorPos)
        }
        return null
    }

    val builder = BooleanExpressionBuilder<ElementFilter, Element>()

    do {
        // if it has no bracket, there must be at least one whitespace
        if (!parseBracketsAndSpaces('(', builder)) {
            throw ParseException("Expected a whitespace or bracket before the tag", cursorPos)
        }

        if (nextMatches(NOT_WITH_WHITESPACE_AND_OPENING_BRACE) != null) {
            advanceBy(NOT.length)
            builder.addNot()
            // continue is required, as !( could be nested
            continue
        }

        builder.addValue(parseTag())

        val separated = parseBracketsAndSpaces(')', builder)

        if (isAtEnd()) break

        // same as with the opening bracket, only that if the string is over, it's okay
        if (!separated) {
            throw ParseException("Expected a whitespace or bracket after the tag", cursorPos)
        }

        if (nextIsAndAdvance(OR)) {
            builder.addOr()
        } else if (nextIsAndAdvance(AND)) {
            builder.addAnd()
        } else {
            throw ParseException("Expected end of string, '$AND' or '$OR'", cursorPos)
        }
    } while (true)

    try {
        return builder.build()
    } catch (e: IllegalStateException) {
        throw ParseException(e.message, cursorPos)
    }
}

private fun StringWithCursor.parseBracketsAndSpaces(bracket: Char, expr: BooleanExpressionBuilder<*, *>): Boolean {
    val initialCursorPos = cursorPos
    do {
        val loopStartCursorPos = cursorPos
        expectAnyNumberOfSpaces()
        if (nextIsAndAdvance(bracket)) {
            try {
                if (bracket == '(') {
                    expr.addOpenBracket()
                } else if (bracket == ')') {
                    expr.addCloseBracket()
                }
            } catch (e: IllegalStateException) {
                throw ParseException(e.message, cursorPos)
            }
        }
    } while (loopStartCursorPos < cursorPos)
    expectAnyNumberOfSpaces()
    return initialCursorPos < cursorPos
}

private fun StringWithCursor.parseTag(): ElementFilter {
    if (nextIsAndAdvance(NOT)) {
        if (nextIsAndAdvance(LIKE)) {
            expectAnyNumberOfSpaces()
            return NotHasKeyLike(parseKey())
        } else {
            expectAnyNumberOfSpaces()
            return NotHasKey(parseKey())
        }
    }

    if (nextIsAndAdvance(LIKE)) {
        expectAnyNumberOfSpaces()
        val key = parseKey()
        val operator = parseOperatorWithSurroundingSpaces()
        if (operator == null) {
            return HasKeyLike(key)
        } else if (LIKE == operator) {
            return HasTagLike(key, parseQuotableWord())
        } else if (NOT_LIKE == operator) {
            return NotHasTagLike(key, parseQuotableWord())
        }
        throw ParseException("Unexpected operator '$operator': The key prefix operator '$LIKE' must be used together with the binary operator '$LIKE' or '$NOT_LIKE'", cursorPos)
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
    val operator = parseOperatorWithSurroundingSpaces() ?: return HasKey(key)

    if (operator == OLDER) {
        return CombineFilters(HasKey(key), TagOlderThan(key, parseDate()))
    }
    if (operator == NEWER) {
        return CombineFilters(HasKey(key), TagNewerThan(key, parseDate()))
    }

    if (operator in KEY_VALUE_OPERATORS) {
        val value = parseQuotableWord()
        when (operator) {
            EQUALS       -> return HasTag(key, value)
            NOT_EQUALS   -> return NotHasTag(key, value)
            LIKE         -> return HasTagValueLike(key, value)
            NOT_LIKE     -> return NotHasTagValueLike(key, value)
        }
    }

    if (operator in COMPARISON_OPERATORS) {
        // we need to decide beforehand what to parse here: a number with optional unit or a date
        val numberWithUnit = nextMatches(NUMBER_WITH_OPTIONAL_UNIT_REGEX)?.value
        if (numberWithUnit != null && findWordLength() == numberWithUnit.length) {
            advanceBy(numberWithUnit.length)
            val value = numberWithUnit.withOptionalUnitToDoubleOrNull()?.toFloat()
                ?: throw ParseException("must be a number or a number with a known unit", cursorPos)
            when (operator) {
                GREATER_THAN          -> return HasTagGreaterThan(key, value)
                GREATER_OR_EQUAL_THAN -> return HasTagGreaterOrEqualThan(key, value)
                LESS_THAN             -> return HasTagLessThan(key, value)
                LESS_OR_EQUAL_THAN    -> return HasTagLessOrEqualThan(key, value)
            }
        } else {
            val value = parseDate()
            when (operator) {
                GREATER_THAN          -> return HasDateTagGreaterThan(key, value)
                GREATER_OR_EQUAL_THAN -> return HasDateTagGreaterOrEqualThan(key, value)
                LESS_THAN             -> return HasDateTagLessThan(key, value)
                LESS_OR_EQUAL_THAN    -> return HasDateTagLessOrEqualThan(key, value)
            }
        }
        throw ParseException("must either be a number (with optional unit) or a (relative) date", cursorPos)
    }
    throw ParseException("Unknown operator '$operator'", cursorPos)
}

private fun StringWithCursor.parseKey(): String {
    val reserved = nextIsReservedWord()
    if (reserved != null) {
        throw ParseException("A key cannot be named like the reserved word '$reserved', surround it with quotation marks", cursorPos)
    }

    val length = findKeyLength()
    if (length == 0) {
        throw ParseException("Missing key (dangling prefix operator)", cursorPos)
    }
    return advanceBy(length).stripAndUnescapeQuotes()
}

private fun StringWithCursor.parseOperatorWithSurroundingSpaces(): String? {
    val spaces = expectAnyNumberOfSpaces()
    val result = OPERATORS.firstOrNull { nextIsAndAdvance(it) }
    if (result == null) {
        retreatBy(spaces)
        return null
    }
    expectAnyNumberOfSpaces()
    return result
}

private fun StringWithCursor.parseQuotableWord(): String {
    val length = findQuotableWordLength()
    if (length == 0) {
        throw ParseException("Missing value (dangling operator)", cursorPos)
    }
    return advanceBy(length).stripAndUnescapeQuotes()
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
        if (nextMatchesAndAdvance(WHITESPACE_REGEX) != null) {
            expectAnyNumberOfSpaces()
            deltaDays = parseDeltaDurationInDays()
        }
        return RelativeDate(deltaDays)
    }

    val date = word.toCheckDate()
    if (date != null) {
        return FixedDate(date)
    }

    throw ParseException("Expected either a date (YYYY-MM-DD) or '$TODAY'", cursorPos)
}

private fun StringWithCursor.parseDeltaDurationInDays(): Float {
    when {
        nextIsAndAdvance(PLUS) -> {
            expectAnyNumberOfSpaces()
            return +parseDurationInDays()
        }
        nextIsAndAdvance(MINUS) -> {
            expectAnyNumberOfSpaces()
            return -parseDurationInDays()
        }
        else -> throw ParseException("Expected $PLUS or $MINUS", cursorPos)
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
        else -> throw ParseException("Expected $YEARS, $MONTHS, $WEEKS or $DAYS", cursorPos)
    }
}

private fun StringWithCursor.expectAnyNumberOfSpaces(): Int =
    nextMatchesAndAdvance(WHITESPACES_REGEX)?.value?.length ?: 0

private fun StringWithCursor.expectOneOrMoreSpaces(): Int {
    if (nextMatchesAndAdvance(WHITESPACE_REGEX) == null) {
        throw ParseException("Expected a whitespace", cursorPos)
    }
    return expectAnyNumberOfSpaces() + 1
}

private fun StringWithCursor.nextIsReservedWord(): String? {
    val wordLength = findWordLength()
    return RESERVED_WORDS.firstOrNull { nextIs(it) && wordLength == it.length }
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
    min(findNext(WHITESPACE_REGEX), findNext(')'))

private fun StringWithCursor.findQuotableWordLength(): Int =
    findQuotationLength() ?: findWordLength()

private fun StringWithCursor.findQuotationLength(): Int? {
    for (quot in QUOTATION_MARKS) {
        if (nextIs(quot)) {
            var length = 0
            while (true) {
                length = findNext(quot, 1 + length)
                if (isAtEnd(length)) {
                    throw ParseException("Did not close quotation marks", cursorPos - 1)
                }
                // ignore escaped
                if (get(cursorPos + length - 1) == '\\') continue
                // +1 because we want to include the closing quotation mark
                return length + 1
            }
        }
    }
    return null
}

private fun String.stripAndUnescapeQuotes(): String {
    val trimmed = if (startsWith('\'') || startsWith('"')) substring(1, length - 1) else this
    val unescaped = trimmed.replace(ESCAPED_QUOTE_REGEX) { it.groupValues[1] }
    return unescaped
}

class ParseException(message: String?, val errorOffset: Int) :
    RuntimeException("At position $errorOffset: $message")
