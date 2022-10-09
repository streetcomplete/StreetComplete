package de.westnordost.streetcomplete.data.osm.created_elements

import de.westnordost.streetcomplete.data.osm.mapdata.ElementType

data class CreatedElementKey(
    val elementType: ElementType,
    val elementId: Long,
    val newElementId: Long?
)
