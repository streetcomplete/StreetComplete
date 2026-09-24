package de.westnordost.streetcomplete.osm.building

import de.westnordost.streetcomplete.ui.common.item_select.Group

/**
 * A [BuildingTypeCategory] with a filtered list of children.
 *
 * Used because enum instances cannot be customized with different child lists.
 */
data class FilteredBuildingTypeCategory(
    val category: BuildingTypeCategory,
    override val children: List<BuildingType>
) : Group<BuildingType> {
    override val item = category.item
}
