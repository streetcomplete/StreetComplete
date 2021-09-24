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
            when {
                // when very short, always have a single marker in the middle
                length < MARKER_FROM_END_DISTANCE * 4 -> {
                    return listOf(position)
                }
                // when `hasMarkersAtEnds` or very long, put markers at ends and space markers
                // evenly between such that no markers are more than `MAXIMUM_MARKER_DISTANCE` apart
                osmElementQuestType.hasMarkersAtEnds || (length > MAXIMUM_MARKER_DISTANCE) -> {
                    val count = 2 + (length / MAXIMUM_MARKER_DISTANCE).toInt()
                    val between = (length - (2 * MARKER_FROM_END_DISTANCE)) / (count - 1)
                    // space markers `between` apart, starting with `MARKER_FROM_END_DISTANCE` (the
                    // final marker will end up at `MARKER_FROM_END_DISTANCE` from the other end)
                    return (0 until count).map {
                        polyline.pointOnPolylineFromStart(
                            MARKER_FROM_END_DISTANCE + (it * between)
                        )!!
                    }
                }
            }
        }
        // all other cases will fall through to a single marker in the middle
        return listOf(position)
    }
}

const val MAXIMUM_MARKER_DISTANCE = 500
const val MARKER_FROM_END_DISTANCE = 15
