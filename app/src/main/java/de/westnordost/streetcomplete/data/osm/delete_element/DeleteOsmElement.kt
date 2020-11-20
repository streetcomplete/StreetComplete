package de.westnordost.streetcomplete.data.osm.delete_element

import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.upload.UploadableInChangeset

/** Contains all necessary information to delete an OSM element as a result of a quest answer */
data class DeleteOsmElement(
    val questId: Long,
    val questType: OsmElementQuestType<*>,
    override val elementId: Long,
    override val elementType: Element.Type,
    override val source: String,
    override val position: LatLon
) : UploadableInChangeset  {
    override val osmElementQuestType get() = questType
}
