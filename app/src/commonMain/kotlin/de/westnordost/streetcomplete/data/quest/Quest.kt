package de.westnordost.streetcomplete.data.quest

import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon

/** Represents one task for the user to complete/correct  */
interface Quest {
    /** Key with which to uniquely identify a quest */
    val key: QuestKey
    /** Position where the quest is at */
    val position: LatLon
    /** Position(s) where the pins for the quest should be put. E.g. a quest on some long way could
     *  have multiple markers along the way instead just on the center. */
    val markerLocations: Collection<LatLon>
    /** Geometry of the element this quest refers to. It is highlighted when selecting the quest. */
    val geometry: ElementGeometry
    /** The type of the quest */
    val type: QuestType
}
