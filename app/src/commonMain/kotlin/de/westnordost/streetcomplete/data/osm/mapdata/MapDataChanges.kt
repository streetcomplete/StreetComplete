package de.westnordost.streetcomplete.data.osm.mapdata

/** Data class that contains the request to create, modify elements and delete the given elements */
data class MapDataChanges(
    val creations: Collection<Element> = emptyList(),
    val modifications: Collection<Element> = emptyList(),
    val deletions: Collection<Element> = emptyList()
)
