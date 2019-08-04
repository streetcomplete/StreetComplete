package de.westnordost.streetcomplete.data.osm.tql

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
        when(node.children.size) {
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
        if (--bracketCount < 0 ) throw IllegalStateException("Closed one bracket too much")

        node = node.parent!!
    }

    fun addValue(i: I) {
        node.addChild(Leaf(i))
    }

    fun addAnd() {
        val or = node as? AnyOf
        val group = node as? BracketHelper

        if (or != null) {
            val last = or.children.last()
            val and = AllOf<I, T>()
            or.replaceChild(last, and)
            and.addChild(last)
            node = and
        }
        else if (group != null) {
            node = replaceChain(group, AllOf())
        }
    }

    fun addOr() {
        val and = node as? AllOf
        val group = node as? BracketHelper

        if (and != null) {
            val nodeParent = node.parent
            if (nodeParent is AnyOf) {
                node = nodeParent
            } else  {
                nodeParent?.removeChild(and)
                val or = AnyOf<I, T>()
                or.addChild(and)
                nodeParent?.addChild(or)
                node = or
            }
        }
        else if (group != null) {
            node = replaceChain(group, AnyOf())
        }
    }
}

private fun <I : Matcher<T>, T> replaceChain(replace: Chain<I, T>, with: Chain<I, T>): Chain<I, T> {
    replace.parent?.replaceChild(replace, with)
    for (child in replace.children) {
        with.addChild(child)
    }
    return with
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
    override fun matches(obj: T?) = throw IllegalStateException("Bracket cannot match")
}
