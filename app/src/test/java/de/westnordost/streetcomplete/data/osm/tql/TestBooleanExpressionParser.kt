package de.westnordost.streetcomplete.data.osm.tql

import java.util.Locale

object TestBooleanExpressionParser {
    fun parse(input: String): BooleanExpression<Matcher<String>, String>? {
        val builder = BooleanExpressionBuilder<Matcher<String>, String>()
        val reader = StringWithCursor(input, Locale.US)
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
