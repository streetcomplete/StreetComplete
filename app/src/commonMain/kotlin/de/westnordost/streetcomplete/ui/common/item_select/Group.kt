package de.westnordost.streetcomplete.ui.common.item_select

interface Group<I> {
    val item: I?
    val children: List<I>
}

/**
 * Simple [Group] implementation used when creating filtered copies of groups.
 */
private data class FilteredGroup<I>(
    override val item: I?,
    override val children: List<I>,
) : Group<I>

/**
 * Returns filtered copies of the groups with all excluded children removed.
 */
operator fun <I, G : Group<I>> List<G>.minus(
    excluded: Set<I>,
): List<Group<I>> {

    return map { group ->
        FilteredGroup(
            item = group.item,
            children = group.children.filterNot { it in excluded },
        )
    }
}
