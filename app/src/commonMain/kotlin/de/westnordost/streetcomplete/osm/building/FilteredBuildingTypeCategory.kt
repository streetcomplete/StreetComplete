package de.westnordost.streetcomplete.osm.building

import de.westnordost.streetcomplete.ui.common.item_select.Group

/**
 * A [BuildingTypeCategory] with a filtered list of children.
 *
 * Used because enum instances cannot be customized with different child lists.
 */
data class FilteredBuildingTypeCategory(
    private val category: BuildingTypeCategory,
    override val children: List<BuildingType>
) : Group<BuildingType> {
    override val item = category.item

    val icon get() = category.icon
    val title get() = category.title
    val description get() = category.description
}
