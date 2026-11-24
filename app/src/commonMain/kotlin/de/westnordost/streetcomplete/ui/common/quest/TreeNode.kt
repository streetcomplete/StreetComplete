package de.westnordost.streetcomplete.ui.common.quest

/**
 * A node in a binary decision tree used by [TreeQuestForm].
 *
 * Represents either a decision node with a [question] and [yes]/[no] subtrees,
 * or a leaf node holding a final [value].
 */
data class TreeNode<T>(
    val value: T? = null,
    val question: String? = null,
    val yes: TreeNode<T>? = null,
    val no: TreeNode<T>? = null,
) {
    /** Creates a leaf node with the final [value]. */
    constructor(value: T) : this(value = value, question = null, yes = null, no = null)

    val isLeaf: Boolean get() = value != null
}
