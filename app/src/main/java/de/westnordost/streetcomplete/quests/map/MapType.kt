package de.westnordost.streetcomplete.quests.map

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.map.MapType.*
import de.westnordost.streetcomplete.view.image_select.GroupableDisplayItem
import de.westnordost.streetcomplete.view.image_select.Item

enum class MapType(val osmValue: String) {
    TOPO("topo"),
    STREET("street"),
    SCHEME("scheme"),
    TOPOSCOPE("toposcope")
}

fun Array<MapType>.toItems() = map { it.asItem() }

fun MapType.asItem(): GroupableDisplayItem<MapType> {
    return Item(this, imageResId, titleResId, descriptionResId)
}

private val MapType.imageResId: Int get() = when (this) {
    TOPO -> R.drawable.map_type_topo
    STREET -> R.drawable.map_type_street
    SCHEME -> R.drawable.map_type_scheme
    TOPOSCOPE -> R.drawable.map_type_toposcope
}

private val MapType.titleResId: Int get() = when (this) {
    TOPO -> R.string.quest_mapType_topo_title
    STREET -> R.string.quest_mapType_street_title
    SCHEME -> R.string.quest_mapType_scheme_title
    TOPOSCOPE -> R.string.quest_mapType_toposcope_title
}

private val MapType.descriptionResId: Int get() = when (this) {
    TOPO -> R.string.quest_mapType_topo_description
    STREET -> R.string.quest_mapType_street_description
    SCHEME -> R.string.quest_mapType_scheme_description
    TOPOSCOPE -> R.string.quest_mapType_toposcope_description
}
