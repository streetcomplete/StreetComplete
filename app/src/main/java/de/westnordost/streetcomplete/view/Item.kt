package de.westnordost.streetcomplete.view

interface GroupedItem {
    // only necessary for bundle-stuff
    val value:String?

    val drawableId: Int
    val titleId: Int get() = 0
    val descriptionId: Int get() = 0

	val isGroup: Boolean get() = !items.isNullOrEmpty()
	val items: List<GroupedItem>? get() = null
}

class Item(
    override val value: String?,
    override val drawableId: Int,
    override val titleId: Int = 0,
    override val descriptionId: Int = 0,
    override val items: List<GroupedItem>? = null
): GroupedItem {

    constructor(copy: GroupedItem, children: List<GroupedItem>) : this(
        copy.value,
        copy.drawableId,
        copy.titleId,
        copy.descriptionId,
        children
    )

}
