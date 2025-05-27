package de.westnordost.streetcomplete.data.osm.mapdata

/** An update of a single element */
sealed interface ElementUpdate {
    data class Update(val newId: Long, val newVersion: Int) : ElementUpdate
    data object Delete : ElementUpdate
}
