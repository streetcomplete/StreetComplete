package de.westnordost.streetcomplete.ui.common.quest

data class TreeNode<T>(
    val value: T? = null,
    val question: String? = null,
    val yes: TreeNode<T>? = null,
    val no: TreeNode<T>? = null,
) {
    constructor(value: T) : this(value = value, question = null, yes = null, no = null)

    val isLeaf: Boolean get() = value != null
}
