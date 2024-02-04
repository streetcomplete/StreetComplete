package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.util.math.measuredLength
import de.westnordost.streetcomplete.util.math.pointsOnPolylineFromStart

/** Represents one task for the user to complete/correct the data based on one OSM element  */
data class OsmQuest(
    override val type: OsmElementQuestType<*>,
    override val elementType: ElementType,
    override val elementId: Long,
    override val geometry: ElementGeometry
) : Quest, OsmQuestDaoEntry {

    override val key: OsmQuestKey by lazy { OsmQuestKey(elementType, elementId, questTypeName) }

    override val questTypeName: String get() = type.name

    override val position: LatLon get() = geometry.center

    override val markerLocations: Collection<LatLon> by lazy {
        if (geometry is ElementPolylinesGeometry) {
            val polyline = geometry.polylines[0]
            val length = polyline.measuredLength()
            // a polyline will have multiple markers if it is over a certain length
            val minLengthForMultiMarkers =
                if (type.hasMarkersAtEnds) {
                    4 * MARKER_FROM_END_DISTANCE
                } else {
                    MAXIMUM_MARKER_DISTANCE + 2 * MARKER_FROM_END_DISTANCE
                }
            if (length > minLengthForMultiMarkers) {
                val count = 2 + (length / MAXIMUM_MARKER_DISTANCE).toInt()
                val between = (length - (2 * MARKER_FROM_END_DISTANCE)) / (count - 1)
                // space markers `between` apart, starting with `MARKER_FROM_END_DISTANCE` (the
                // final marker will end up at `MARKER_FROM_END_DISTANCE` from the other end)
                return@lazy polyline.pointsOnPolylineFromStart(
                    (0 until count).map { MARKER_FROM_END_DISTANCE + (it * between) }
                )
            }
        }
        // fall through to a single marker in the middle
        listOf(position)
    }
}

const val MAXIMUM_MARKER_DISTANCE = 400
const val MARKER_FROM_END_DISTANCE = 15
