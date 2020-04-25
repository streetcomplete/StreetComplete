package de.westnordost.streetcomplete.data.osm.upload

import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType

interface UploadableInChangeset {
    val source: String
    val osmElementQuestType: OsmElementQuestType<*>
    val elementType: Element.Type
    val elementId: Long
    val position: LatLon
}
