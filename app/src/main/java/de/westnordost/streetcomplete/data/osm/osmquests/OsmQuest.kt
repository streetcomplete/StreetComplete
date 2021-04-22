package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.util.measuredLength
import de.westnordost.streetcomplete.util.pointOnPolylineFromEnd
import de.westnordost.streetcomplete.util.pointOnPolylineFromStart

/** Represents one task for the user to complete/correct the data based on one OSM element  */
data class OsmQuest(
    val osmElementQuestType: OsmElementQuestType<*>, // underlying OSM data
    override val elementType: Element.Type,
    override val elementId: Long,
    override val geometry: ElementGeometry
) : Quest, OsmQuestDaoEntry {

    override val key: OsmQuestKey get() = OsmQuestKey(elementType, elementId, questTypeName)

    override val questTypeName: String get() = osmElementQuestType::class.simpleName!!

    override val position: LatLon get() = geometry.center
    override val type: QuestType<*> get() = osmElementQuestType

    override val markerLocations: Collection<LatLon> get() {
        if (osmElementQuestType.hasMarkersAtEnds && geometry is ElementPolylinesGeometry) {
            val polyline = geometry.polylines[0]
            val length = polyline.measuredLength()
            if (length > 15 * 4) {
                return listOf(
                    polyline.pointOnPolylineFromStart(15.0)!!,
                    polyline.pointOnPolylineFromEnd(15.0)!!
                )
            }
        }
        return listOf(position)
    }
}
