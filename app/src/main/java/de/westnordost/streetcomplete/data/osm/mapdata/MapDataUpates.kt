package de.westnordost.streetcomplete.data.osm.mapdata

data class MapDataUpdates(
    val updated: Collection<Element> = emptyList(),
    val deleted: Collection<ElementKey> = emptyList(),
    val idUpdates: Collection<ElementIdUpdate> = emptyList()
)

data class ElementIdUpdate(
    val elementType: ElementType,
    val oldElementId: Long,
    val newElementId: Long
)
