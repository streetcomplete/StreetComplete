package de.westnordost.streetcomplete.data.osm.tql

import java.util.EmptyStackException
import java.util.Stack

/** Builds a boolean expression. Basically a BooleanExpression with a cursor.  */
class BooleanExpressionBuilder<I : BooleanExpressionValue<T>, T> {
    private var node: BooleanExpression<I,T> = BooleanExpression(true)
    private val bracket: Stack<BooleanExpression<I,T>> = Stack()

    fun build(): BooleanExpression<I,T> {
        if (!bracket.empty()) {
            throw IllegalStateException("Closed one bracket too little")
        }

        while (node.parent != null) {
            node = node.parent!!
        }

        node.flatten()

        return node
    }

    fun addOpenBracket() {
        bracket.push(node)
        node = node.addOpenBracket()
    }

    fun addCloseBracket() {
        try {
            node = bracket.pop()
        } catch (e: EmptyStackException) {
            throw IllegalStateException("Closed one bracket too much")
        }
    }

    fun addValue(i: I) { node.addValue(i) }
    fun addAnd() { node = node.addAnd() }
    fun addOr() { node = node.addOr() }
}
