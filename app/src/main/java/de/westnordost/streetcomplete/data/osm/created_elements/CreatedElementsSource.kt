package de.westnordost.streetcomplete.data.osm.created_elements

import de.westnordost.streetcomplete.data.osm.mapdata.ElementType

interface CreatedElementsSource {
    /** Returns whether the given element has been created by this app */
    fun contains(elementType: ElementType, elementId: Long): Boolean
}
