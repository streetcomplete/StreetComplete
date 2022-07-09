package de.westnordost.streetcomplete.data.quest

import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon

/** Represents one task for the user to complete/correct  */
interface Quest {
    val key: QuestKey
    val position: LatLon
    val markerLocations: Collection<LatLon>
    val geometry: ElementGeometry

    val type: QuestType
}
