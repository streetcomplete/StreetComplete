package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.quest.OsmQuestKey

interface OsmQuestDaoEntry {
    val questTypeName: String
    val elementType: ElementType
    val elementId: Long
    val position: LatLon
}

val OsmQuestDaoEntry.key get() =
    OsmQuestKey(elementType, elementId, questTypeName)
