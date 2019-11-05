package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.data.osm.ElementKey

data class CreateNote(
    var id: Long?,
    val text: String,
    val position: LatLon,
    val questTitle: String? = null,
    val elementKey: ElementKey? = null,
    val imagePaths: List<String>? = null
)
