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
import de.westnordost.streetcomplete.util.StringWithCursor

/**
 * Compiles a string in filter syntax into a ElementFilterExpression. A string in filter syntax is
 * something like this:
 *
 * <tt>"ways with (highway = residential or highway = tertiary) and !name"</tt> (finds all
 * residential and tertiary roads that have no name)
 */
fun String.toElementFilterExpression(): ElementFilterExpression {
    val cursor = StringWithCursor(this)
    return ElementFilterExpression(cursor.parseElementsDeclaration(), cursor.parseElementFilters())
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

private val RESERVED_WORDS = setOf(WITH, OR, AND, OLDER, NEWER, TODAY)
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

private const val OPERATOR_CHARS = "!=~><(),"

private val ELEMENT_TYPE_FILTERS_BY_NAME =
    ElementsTypeFilter.entries.associateBy { it.name.lowercase() }

private fun StringWithCursor.parseElementsDeclaration(): Set<ElementsTypeFilter> {
    val result = LinkedHashSet<ElementsTypeFilter>()
    do {
        expectAnyNumberOfSpaces()
        val element = parseElementDeclaration()
        expectAnyNumberOfSpaces()
        if (result.contains(element)) {
            throw ParseException("Mentioned the same element type $element twice", cursor)
        }
        result.add(element)
    } while (nextIsAndAdvance(','))
    return result
}

private fun StringWithCursor.parseElementDeclaration(): ElementsTypeFilter {
    val word = parseWord()
    return ELEMENT_TYPE_FILTERS_BY_NAME[word]
        ?: throw ParseException("Expected element types. Any of: nodes, ways or relations, separated by ','", cursor - word.length)
}

private fun StringWithCursor.parseElementFilters(): BooleanExpression<ElementFilter, Element>? {
    // tags are optional...
    if (!nextIsAndAdvance(WITH)) {
        if (!isAtEnd()) {
            throw ParseException("Expected end of string or '$WITH' keyword", cursor)
        }
        return null
    }

    val builder = BooleanExpressionBuilder<ElementFilter, Element>()

    do {
        // if it has no bracket, there must be at least one whitespace
        if (!parseBracketsAndSpaces('(', builder)) {
            throw ParseException("Expected a whitespace or bracket before the tag", cursor)
        }

        if (nextIsNegation()) {
            advanceBy(NOT.length)
            builder.addNot()
            // continue is required, as !( could be nested
            continue
        }

        builder.addValue(parseElementFilter())

        val separated = parseBracketsAndSpaces(')', builder)

        if (isAtEnd()) break

        // same as with the opening bracket, only that if the string is over, it's okay
        if (!separated) {
            throw ParseException("Expected a whitespace or bracket after the tag", cursor)
        }

        if (nextIsAndAdvance(OR)) {
            builder.addOr()
        } else if (nextIsAndAdvance(AND)) {
            builder.addAnd()
        } else {
            throw ParseException("Expected end of string, '$AND' or '$OR'", cursor)
        }
    } while (true)

    try {
        return builder.build()
    } catch (e: IllegalStateException) {
        throw ParseException(e.message, cursor)
    }
}

private fun StringWithCursor.nextIsNegation(): Boolean {
    val initialPos = cursor
    if (nextIsAndAdvance(NOT)) {
        expectAnyNumberOfSpaces()
        if (nextIsAndAdvance('(')) {
            retreatBy(cursor - initialPos)
            return true
        }
    }
    retreatBy(cursor - initialPos)
    return false
}

private fun StringWithCursor.parseBracketsAndSpaces(bracket: Char, expr: BooleanExpressionBuilder<*, *>): Boolean {
    val initialCursorPos = cursor
    do {
        val loopStartCursorPos = cursor
        expectAnyNumberOfSpaces()
        if (nextIsAndAdvance(bracket)) {
            try {
                if (bracket == '(') {
                    expr.addOpenBracket()
                } else if (bracket == ')') {
                    expr.addCloseBracket()
                }
            } catch (e: IllegalStateException) {
                throw ParseException(e.message, cursor)
            }
        }
    } while (loopStartCursorPos < cursor)
    expectAnyNumberOfSpaces()
    return initialCursorPos < cursor
}

private fun StringWithCursor.parseElementFilter(): ElementFilter {
    if (nextIsAndAdvance(NOT)) {
        if (nextIsAndAdvance(LIKE)) {
            expectAnyNumberOfSpaces()
            return NotHasKeyLike(parseTag())
        } else {
            expectAnyNumberOfSpaces()
            return NotHasKey(parseTag())
        }
    }

    if (nextIsAndAdvance(LIKE)) {
        expectAnyNumberOfSpaces()
        val key = parseTag()
        val operator = parseOperatorWithSurroundingSpaces()
        if (operator == null) {
            return HasKeyLike(key)
        } else if (LIKE == operator) {
            return HasTagLike(key, parseTag())
        } else if (NOT_LIKE == operator) {
            return NotHasTagLike(key, parseTag())
        }
        throw ParseException("Unexpected operator '$operator': The key prefix operator '$LIKE' must be used together with the binary operator '$LIKE' or '$NOT_LIKE'", cursor)
    }

    if (nextIsAndAdvance(OLDER)) {
        expectOneOrMoreSpaces()
        return ElementOlderThan(parseDateFilter())
    }
    if (nextIsAndAdvance(NEWER)) {
        expectOneOrMoreSpaces()
        return ElementNewerThan(parseDateFilter())
    }

    val key = parseTag()
    val operator = parseOperatorWithSurroundingSpaces() ?: return HasKey(key)

    if (operator == OLDER) {
        return CombineFilters(HasKey(key), TagOlderThan(key, parseDateFilter()))
    }
    if (operator == NEWER) {
        return CombineFilters(HasKey(key), TagNewerThan(key, parseDateFilter()))
    }

    if (operator in KEY_VALUE_OPERATORS) {
        val value = parseTag()
        when (operator) {
            EQUALS       -> return HasTag(key, value)
            NOT_EQUALS   -> return NotHasTag(key, value)
            LIKE         -> return HasTagValueLike(key, value)
            NOT_LIKE     -> return NotHasTagValueLike(key, value)
        }
    }

    if (operator in COMPARISON_OPERATORS) {
        // we need to decide beforehand what to parse here: a number with optional unit or a date
        val word = parseWord()
        val value = word.withOptionalUnitToDoubleOrNull()?.toFloat()
        if (value != null) {
            when (operator) {
                GREATER_THAN          -> return HasTagGreaterThan(key, value)
                GREATER_OR_EQUAL_THAN -> return HasTagGreaterOrEqualThan(key, value)
                LESS_THAN             -> return HasTagLessThan(key, value)
                LESS_OR_EQUAL_THAN    -> return HasTagLessOrEqualThan(key, value)
            }
        } else {
            retreatBy(word.length)
            val date = parseDateFilter()
            when (operator) {
                GREATER_THAN          -> return HasDateTagGreaterThan(key, date)
                GREATER_OR_EQUAL_THAN -> return HasDateTagGreaterOrEqualThan(key, date)
                LESS_THAN             -> return HasDateTagLessThan(key, date)
                LESS_OR_EQUAL_THAN    -> return HasDateTagLessOrEqualThan(key, date)
            }
        }
        throw ParseException("must either be a number (with optional unit) or a (relative) date", cursor)
    }
    throw ParseException("Unknown operator '$operator'", cursor)
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

private fun StringWithCursor.parseTag(): String {
    val quotedWord = parseQuotedWord('"') ?: parseQuotedWord('\'')
    if (quotedWord != null) {
        return quotedWord
    }
    val word = parseWord()
    if (word in RESERVED_WORDS) {
        throw ParseException("A key or value cannot be named like the reserved word '$word', surround it with quotation marks", cursor)
    }
    return word
}

private fun StringWithCursor.parseQuotedWord(quot: Char): String? {
    if (!nextIs(quot)) return null

    var length = 0
    while (true) {
        length = findNext(quot, 1 + length)
        if (isAtEnd(length)) {
            throw ParseException("Did not close quotation marks", cursor - 1)
        }
        // ignore escaped
        if (get(cursor + length - 1) != '\\') break
    }
    length += 1 // +1 because we want to include the closing quotation mark

    val word = advanceBy(length)
    return word
        .substring(1, word.length - 1) // remove quotes
        .replace("\\$quot", "$quot") // unescape quotes within string
}

private fun StringWithCursor.parseWord(): String {
    // words are separated by whitespaces or operators
    val length = findNext { it.isWhitespace() || it in OPERATOR_CHARS }
    if (length == 0) {
        throw ParseException("Missing value (dangling operator)", cursor)
    }
    return advanceBy(length)
}

private fun StringWithCursor.parseNumber(): Float {
    val word = parseWord()
    try {
        return word.toFloat()
    } catch (e: NumberFormatException) {
        throw ParseException("Expected a number", cursor)
    }
}

private fun StringWithCursor.parseDateFilter(): DateFilter {
    val word = parseWord()
    if (word == TODAY) {
        expectAnyNumberOfSpaces()
        val deltaDays = parseDeltaDurationInDays() ?: 0f
        return RelativeDate(deltaDays)
    }

    val date = word.toCheckDate()
    if (date != null) {
        return FixedDate(date)
    }

    throw ParseException("Expected either a date (YYYY-MM-DD) or '$TODAY'", cursor)
}

private fun StringWithCursor.parseDeltaDurationInDays(): Float? {
    val op = when {
        nextIsAndAdvance(PLUS) -> +1
        nextIsAndAdvance(MINUS) -> -1
        else -> return null
    }
    expectAnyNumberOfSpaces()
    return op * parseDurationInDays()
}

private fun StringWithCursor.parseDurationInDays(): Float {
    val duration = parseNumber()
    expectOneOrMoreSpaces()
    return when {
        nextIsAndAdvance(YEARS) -> 365.25f * duration
        nextIsAndAdvance(MONTHS) -> 30.5f * duration
        nextIsAndAdvance(WEEKS) -> 7 * duration
        nextIsAndAdvance(DAYS) -> duration
        else -> throw ParseException("Expected $YEARS, $MONTHS, $WEEKS or $DAYS", cursor)
    }
}

private fun StringWithCursor.expectAnyNumberOfSpaces(): Int =
    advanceWhile { it.isWhitespace() }

private fun StringWithCursor.expectOneOrMoreSpaces(): Int {
    val spaces = advanceWhile { it.isWhitespace() }
    if (spaces == 0) {
        throw ParseException("Expected a whitespace", cursor)
    }
    return spaces
}

class ParseException(message: String?, val errorOffset: Int) :
    RuntimeException("At position $errorOffset: $message")
