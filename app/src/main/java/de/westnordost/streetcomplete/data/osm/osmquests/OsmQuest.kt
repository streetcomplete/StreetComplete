package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.util.measuredLength
import de.westnordost.streetcomplete.util.pointOnPolylineFromStart
import kotlin.math.floor

/** Represents one task for the user to complete/correct the data based on one OSM element  */
data class OsmQuest(
    val osmElementQuestType: OsmElementQuestType<*>, // underlying OSM data
    override val elementType: ElementType,
    override val elementId: Long,
    override val geometry: ElementGeometry
) : Quest, OsmQuestDaoEntry {

    override val key: OsmQuestKey get() = OsmQuestKey(elementType, elementId, questTypeName)

    override val questTypeName: String get() = osmElementQuestType::class.simpleName!!

    override val position: LatLon get() = geometry.center
    override val type: QuestType<*> get() = osmElementQuestType

    override val markerLocations: Collection<LatLon> get() {
        if (geometry is ElementPolylinesGeometry) {
            val polyline = geometry.polylines[0]
            val length = polyline.measuredLength()
            if ((osmElementQuestType.hasMarkersAtEnds && length > MARKER_FROM_END_DISTANCE * 4)
                || (length > MAXIMUM_MARKER_DISTANCE + 2 * MARKER_FROM_END_DISTANCE)) {
                val markerCount = 2 + (length / MAXIMUM_MARKER_DISTANCE).toInt()
                val markerDistance = (length - (2 * MARKER_FROM_END_DISTANCE)) / (markerCount - 1)
                return (0 until markerCount).map{
                    polyline.pointOnPolylineFromStart(
                        MARKER_FROM_END_DISTANCE + (it * markerDistance)
                    )!!
                }
            }
        }
        return listOf(position)
    }
}

const val MAXIMUM_MARKER_DISTANCE = 500
const val MARKER_FROM_END_DISTANCE = 15
