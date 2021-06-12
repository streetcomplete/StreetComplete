package de.westnordost.streetcomplete.data.osm.created_elements

import de.westnordost.streetcomplete.data.osm.mapdata.ElementType

interface CreatedElementsSource {
    fun contains(elementType: ElementType, elementId: Long): Boolean
}
