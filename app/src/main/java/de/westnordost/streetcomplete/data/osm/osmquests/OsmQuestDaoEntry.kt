package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.data.quest.OsmQuestKey

interface OsmQuestDaoEntry {
    val questTypeName: String
    val elementType: Element.Type
    val elementId: Long
    val position: LatLon
}

val OsmQuestDaoEntry.key get() =
    OsmQuestKey(elementType, elementId, questTypeName)
