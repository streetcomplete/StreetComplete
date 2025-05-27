package de.westnordost.streetcomplete.data.elementfilter

import de.westnordost.streetcomplete.util.StringWithCursor

object TestBooleanExpressionParser {
    fun parse(input: String): BooleanExpression<Matcher<String>, String>? {
        val builder = BooleanExpressionBuilder<Matcher<String>, String>()
        val reader = StringWithCursor(input)
        while (!reader.isAtEnd()) {
            when {
                reader.nextIsAndAdvance('*') -> builder.addAnd()
                reader.nextIsAndAdvance('+') -> builder.addOr()
                reader.nextIsAndAdvance('(') -> builder.addOpenBracket()
                reader.nextIsAndAdvance(')') -> builder.addCloseBracket()
                else -> builder.addValue(TestBooleanExpressionValue(reader.advance().toString()))
            }
        }
        return builder.build()
    }
}
