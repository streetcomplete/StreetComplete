package de.westnordost.streetcomplete.data.osm.geometry

import de.westnordost.streetcomplete.data.osm.mapdata.ElementType

data class ElementGeometryEntry(
    val elementType: ElementType,
    val elementId: Long,
    val geometry: ElementGeometry
)
