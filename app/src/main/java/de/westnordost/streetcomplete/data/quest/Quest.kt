package de.westnordost.streetcomplete.data.quest

import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.screens.main.map.components.Pin

/** Represents one task for the user to complete/correct  */
abstract class Quest {
    abstract val key: QuestKey
    abstract val position: LatLon
    abstract val markerLocations: Collection<LatLon>
    abstract val geometry: ElementGeometry

    abstract val type: QuestType

    /** caching pins in the quest allows for faster setting of pins */
    var pins: List<Pin>? = null
}
