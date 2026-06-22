package de.westnordost.streetcomplete.data.osm.created_elements

import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey

interface CreatedElementsController : CreatedElementsSource {
    fun putAll(entries: Collection<ElementKey>)
    fun deleteAll(entries: Collection<ElementKey>)
    fun clear()
}
