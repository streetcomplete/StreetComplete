package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.LatLon

interface OsmQuestDaoEntry {
    var id: Long?
    val questTypeName: String
    val elementType: Element.Type
    val elementId: Long
    val position: LatLon
}

