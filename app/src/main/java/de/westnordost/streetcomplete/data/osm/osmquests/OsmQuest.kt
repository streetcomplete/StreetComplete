package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.util.measuredLength
import de.westnordost.streetcomplete.util.pointOnPolylineFromEnd
import de.westnordost.streetcomplete.util.pointOnPolylineFromStart
import kotlin.math.roundToInt

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
            if (osmElementQuestType.hasMarkersAtEnds && length > 15 * 4) {
                return kotlin.collections.listOf(
                    polyline.pointOnPolylineFromStart(15.0)!!,
                    polyline.pointOnPolylineFromEnd(15.0)!!
                )
            } else if (length >= 1.5 * MAXIMUM_MARKER_DISTANCE) {
                val markerCount = (length / MAXIMUM_MARKER_DISTANCE).roundToInt()
                val markerDistance = length / markerCount
                return (1..markerCount).map{
                    polyline.pointOnPolylineFromStart(
                        (it - 0.5) * markerDistance
                    )!!
                }
            }
        }
        return listOf(position)
    }
}

const val MAXIMUM_MARKER_DISTANCE = 500
