package de.westnordost.streetcomplete.util.tree

data class Node<T>(val value: T?, val children: List<Node<T>> = emptyList()) {

    /** Return all values in this tree in a flat sequence */
    fun yieldAll(): Sequence<T> = sequence {
        if (value != null) yield(value)
        for (child in children) {
            yieldAll(child.yieldAll())
        }
    }

    /** Return a breadcrumb sequence to find the given [v] or null if the given value is not
     *  contained in this tree. */
    fun yieldParentValues(v: T): Sequence<T>? {
        if (v == value) return emptySequence()
        for (child in children) {
            val parents = child.yieldParentValues(v)
            if (parents != null) {
                return sequence {
                    if (value != null) yield(value)
                    yieldAll(parents)
                }
            }
        }
        return null
    }

    /** Return all values that are children (indirect or direct) of the given [v] or null if the
     *  given value is not contained in this tree */
    fun yieldChildValues(v: T): Sequence<T>? {
        val node = findFirst(v) ?: return null
        return sequence {
            for (child in node.children) {
                yieldAll(child.yieldAll())
            }
        }
    }

    /** Find node with the given [v] */
    fun findFirst(v: T): Node<T>? {
        if (v == value) return this
        for (child in children) {
            val n = child.findFirst(v)
            if (n != null) return n
        }
        return null
    }
}

class NodeBuilder<T>(val value: T?) {
    private val children: MutableList<Node<T>> = mutableListOf()

    fun nd(value: T?, block: NodeBuilder<T>.() -> Unit = {}) {
        val childBuilder = NodeBuilder(value)
        childBuilder.apply(block)
        children.add(childBuilder.build())
    }

    fun build(): Node<T> = Node(value, children)
}

fun <T> buildTree(block: NodeBuilder<T>.() -> Unit): Node<T> {
    val builder = NodeBuilder<T>(null)
    builder.apply(block)
    return builder.build()
}
