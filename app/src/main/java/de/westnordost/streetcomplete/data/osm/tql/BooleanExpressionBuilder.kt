package de.westnordost.streetcomplete.data.osm.tql

import java.util.EmptyStackException
import java.util.Stack

/** Builds a boolean expression. Basically a BooleanExpression with a cursor.  */
class BooleanExpressionBuilder<T : BooleanExpressionValue> {
    private var node: BooleanExpression<T> = BooleanExpression(true)
    private val bracket: Stack<BooleanExpression<T>> = Stack()

    fun build(): BooleanExpression<T> {
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

    fun addValue(t: T) { node.addValue(t) }
    fun addAnd() { node = node.addAnd() }
    fun addOr() { node = node.addOr() }
}
