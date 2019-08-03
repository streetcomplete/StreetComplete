package de.westnordost.streetcomplete.data.osm.tql

import de.westnordost.streetcomplete.data.osm.tql.BooleanExpression.Type.*
import java.util.*

/** A boolean expression of values that are connected by ANDs and ORs  */
class BooleanExpression<I : BooleanExpressionValue<T>,T>(asRoot: Boolean = false) {

    // once set, these are final
    private var type: Type? = null
		private set(value) {
			if (field != null) throw IllegalStateException()
			field = value
		}
    var value: I? = null
        private set

    var parent: BooleanExpression<I,T>? = null
        private set
	val children get() = _children.toList()
    private var _children = LinkedList<BooleanExpression<I,T>>()

    val firstChild: BooleanExpression<I,T>?
        get() = if (!_children.isEmpty()) _children.first else null

	enum class Type { AND, OR, ROOT, LEAF }

    val isOr: Boolean get() = type == OR
    val isAnd: Boolean get() = type == AND
    val isValue: Boolean get() = type == LEAF
    val isRoot: Boolean get() = type == ROOT

	init {
		if (asRoot) type = ROOT
	}

    /** --------------------- Methods for extending the boolean expression ----------------------  */

    fun addAnd(): BooleanExpression<I,T> {
        if (!isAnd) {
            val newChild = createIntermediateChild()
            newChild.type = AND
            return newChild
        }
        return this
    }

    fun addOr(): BooleanExpression<I,T> {
        var node: BooleanExpression<I,T> = this

        if (isAnd) {
            if (parent!!.isRoot) {
                node = createIntermediateParent()
                node.type = OR
            } else {
                node = parent!!
            }
        }

        if (!node.isOr) {
            val newChild = node.createIntermediateChild()
            newChild.type = OR
            return newChild
        }
        return node
    }

    fun addValue(t: I) {
        val child = createChild()
        child.type = LEAF
        child.value = t
    }

    fun addOpenBracket(): BooleanExpression<I,T> {
        return createChild()
    }

    private fun createChild(): BooleanExpression<I,T> {
        val child = BooleanExpression<I,T>()
        addChild(child)
        return child
    }

    private fun createIntermediateParent(): BooleanExpression<I,T> {
        val newParent = BooleanExpression<I,T>()
	    val oldParent = parent!!
	    oldParent.removeChild(this)
        newParent.addChild(this)
	    oldParent.addChild(newParent)
        return newParent
    }

    private fun createIntermediateChild(): BooleanExpression<I,T> {
        val lastChild = removeLastChild()
        val newNode = createChild()
        if (lastChild != null) newNode.addChild(lastChild)
        return newNode
    }

    private fun removeLastChild(): BooleanExpression<I,T>? {
        return if (!_children.isEmpty()) _children.removeLast() else null
    }

    private fun addChild(child: BooleanExpression<I,T>) {
        child.parent = this
        _children.add(child)
    }

    private fun removeChild(child: BooleanExpression<I,T>) {
        _children.remove(child)
        child.parent = null
    }

    /** --------------------- Methods for accessing the boolean expression ---------------------  */

    fun matches(obj: T?): Boolean = when (type) {
        LEAF -> value!!.matches(obj)
        OR   -> _children.any { it.matches(obj) }
        AND  -> _children.all { it.matches(obj) }
        ROOT -> _children.first.matches(obj)
        else -> false
    }

    /** Removes unnecessary depth in the expression tree  */
    fun flatten() {
        removeEmptyNodes()
        mergeNodesWithSameOperator()
    }

    /** remove nodes from superfluous brackets  */
    private fun removeEmptyNodes() {
        val it = _children.listIterator()
        while (it.hasNext()) {
            val child = it.next()
            if (child.type == null && child._children.size == 1) {
                replaceChildAt(it, child._children.first)
                it.previous() // = the just replaced node will be checked again
            } else {
                child.removeEmptyNodes()
            }
        }
    }

    /** merge children recursively which do have the same operator set (and, or)  */
    private fun mergeNodesWithSameOperator() {
        if (isValue) return

        val it = _children.listIterator()
        while (it.hasNext()) {
            val child = it.next()
            child.mergeNodesWithSameOperator()

            // merge two successive nodes of same type
            if (child.type == type) {
                replaceChildAt(it, child._children)
            }
        }
    }

    private fun replaceChildAt(
        at: MutableListIterator<BooleanExpression<I,T>>,
        with: BooleanExpression<I,T>
    ) {
        replaceChildAt(at, listOf(with))
    }

    private fun replaceChildAt(
        at: MutableListIterator<BooleanExpression<I,T>>,
        with: Collection<BooleanExpression<I,T>>
    ) {
        at.remove()
        for (withEle in with) {
            at.add(withEle)
            withEle.parent = this
        }
    }

    /** Expand the expression so that all ANDs have only leaves  */
    fun expand() {
        moveDownAnds()
        mergeNodesWithSameOperator()
    }

    private fun moveDownAnds() {
        if (isValue) return

        if (isAnd) {
            val rest = removeFirstOr()
            if (rest != null) {
                // the first OR moves into our place, we are now an orphan
                parent!!.replaceChild(this, rest)
                addCopiesOfMyselfInBetweenChildrenOf(rest)
                rest.mergeNodesWithSameOperator()
                rest.moveDownAnds()
                return
            }
        }
        for (child in LinkedList(_children)) child.moveDownAnds()
    }

    private fun replaceChild(replace: BooleanExpression<I,T>, with: BooleanExpression<I,T>) {
        val it = _children.listIterator()
        while (it.hasNext()) {
            val child = it.next()
            if (child === replace) {
                replaceChildAt(it, with)
                return
            }
        }
    }

    fun copy(): BooleanExpression<I,T> {
        val result = BooleanExpression<I,T>()
        result.type = type
        result.value = value // <- this is a a reference! value should be immutable
        result.parent = null // parent is set on parent.addChild, see for loop
        result._children = LinkedList()
        for (child in _children) {
            result.addChild(child.copy())
        }

        return result
    }

    /** Adds deep copies of this as children to other, each taking one original child as its own  */
    private fun addCopiesOfMyselfInBetweenChildrenOf(other: BooleanExpression<I,T>) {
        val it = other._children.listIterator()
        while (it.hasNext()) {
            val child = it.next()
            val clone = copy()
            clone.replacePlaceholder(child)
            other.replaceChildAt(it, clone)
        }
    }

    private fun replacePlaceholder(with: BooleanExpression<I,T>) {
        val it = _children.listIterator()
        while (it.hasNext()) {
            val child = it.next()
            if (child.type == null) {
                replaceChildAt(it, with)
                return
            }
        }
    }

    /** Find first OR child and remove it from my children  */
    private fun removeFirstOr(): BooleanExpression<I,T>? {
        val it = _children.listIterator()
        while (it.hasNext()) {
            val child = it.next()
            if (child.isOr) {
                it.remove()
                val placeholder = BooleanExpression<I,T>()
                it.add(placeholder)
                return child
            }
        }
        return null
    }

    override fun toString(): String {
        if (type == LEAF) return value!!.toString()

        val builder = StringBuilder()
        if (isOr && !parent!!.isRoot) builder.append('(')

        var first = true
        for (child in _children) {
            if (first)
                first = false
            else {
                builder.append(' ')
                builder.append(type!!.toString().toLowerCase(Locale.US))
                builder.append(' ')
            }

            builder.append(child.toString())
        }
        if (isOr && !parent!!.isRoot) builder.append(')')

        return builder.toString()
    }
}
