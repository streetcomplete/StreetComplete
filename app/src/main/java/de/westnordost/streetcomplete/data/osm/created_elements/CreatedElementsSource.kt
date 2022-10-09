package de.westnordost.streetcomplete.data.osm.created_elements

import de.westnordost.streetcomplete.data.osm.mapdata.ElementType

interface CreatedElementsSource {
    /** Returns whether the given element has been created by this app */
    fun contains(elementType: ElementType, elementId: Long): Boolean
    /** Returns the current id of the given created element. Created elements get their proper ids
     *  assigned only after upload, so an element may have an old local id they can be referred
     *  to and a proper real id. This function returns the latter even if for [elementId], the old
     *  id is specified.
     *  Returns null if the given element is not a created element. */
    fun getId(elementType: ElementType, elementId: Long): Long?
}
