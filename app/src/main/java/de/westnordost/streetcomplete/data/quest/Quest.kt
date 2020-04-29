package de.westnordost.streetcomplete.data.quest

import java.util.Date
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.osmapi.map.data.LatLon

/** Represents one task for the user to complete/correct  */
interface Quest {
    var id: Long?

    val center: LatLon
    val markerLocations: Collection<LatLon>
    val geometry: ElementGeometry

    val type: QuestType<*>

    var status: QuestStatus

    val lastUpdate: Date
}
