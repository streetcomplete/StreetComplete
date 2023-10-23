package de.westnordost.streetcomplete.data.elementfilter

/** Builds a boolean expression. Basically a BooleanExpression with a cursor.  */
class BooleanExpressionBuilder<I : Matcher<T>, T> {
    private var node: Chain<I, T> = BracketHelper()
    private var bracketCount = 0

    fun build(): BooleanExpression<I, T>? {
        if (bracketCount > 0) {
            throw IllegalStateException("Closed one bracket too little")
        }

        while (node.parent != null) {
            node = node.parent!!
        }

        node.flatten()

        // flatten cannot remove itself, but we wanna do that
        when (node.children.size) {
            0 -> return null
            1 -> {
                val firstChild = node.children.first()
                node.removeChild(firstChild)
                return firstChild
            }
        }

        node.ensureNoBracketNodes()
        return node
    }

    fun addOpenBracket() {
        val group = BracketHelper<I, T>()
        node.addChild(group)
        node = group

        bracketCount++
    }

    fun addCloseBracket() {
        if (--bracketCount < 0) throw IllegalStateException("Closed one bracket too much")

        while (node !is BracketHelper) {
            node = node.parent!!
        }
        node = node.parent!!

        if (node is Not) {
            node = node.parent!!
        }
    }

    fun addValue(i: I) {
        node.addChild(Leaf(i))
    }

    fun addAnd() {
        if (node !is AllOf) {
            val last = node.children.last()
            val allOf = AllOf<I, T>()
            node.replaceChild(last, allOf)
            allOf.addChild(last)
            node = allOf
        }
    }

    fun addOr() {
        val allOf = node as? AllOf
        val group = node as? BracketHelper

        if (allOf != null) {
            val nodeParent = node.parent
            if (nodeParent is AnyOf) {
                node = nodeParent
            } else {
                nodeParent?.removeChild(allOf)
                val anyOf = AnyOf<I, T>()
                anyOf.addChild(allOf)
                nodeParent?.addChild(anyOf)
                node = anyOf
            }
        } else if (group != null) {
            val last = node.children.last()
            val anyOf = AnyOf<I, T>()
            node.replaceChild(last, anyOf)
            anyOf.addChild(last)
            node = anyOf
        }
    }

    fun addNot() {
        val not = Not<I, T>()
        node.addChild(not)
        node = not
    }
}

private fun <I : Matcher<T>, T> Chain<I, T>.ensureNoBracketNodes() {
    if (this is BracketHelper) throw IllegalStateException("BooleanExpression still contains a Bracket node!")

    val it = children.listIterator()
    while (it.hasNext()) {
        val child = it.next()
        if (child is Chain) child.ensureNoBracketNodes()
    }
}

private class BracketHelper<I : Matcher<T>, T> : Chain<I, T>() {
    override fun matches(obj: T) = throw IllegalStateException("Bracket cannot match")
}
