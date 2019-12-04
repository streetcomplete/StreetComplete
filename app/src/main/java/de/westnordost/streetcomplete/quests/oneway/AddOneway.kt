package de.westnordost.streetcomplete.quests.oneway

import android.util.Log

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.Way
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.ElementGeometry
import de.westnordost.streetcomplete.data.osm.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao
import de.westnordost.streetcomplete.data.osm.tql.FiltersParser
import de.westnordost.streetcomplete.quests.oneway.data.TrafficFlowSegment
import de.westnordost.streetcomplete.quests.oneway.data.TrafficFlowSegmentsDao
import de.westnordost.streetcomplete.quests.oneway.data.WayTrafficFlowDao

class AddOneway(
    private val overpassMapDataDao: OverpassMapDataAndGeometryDao,
    private val trafficFlowSegmentsDao: TrafficFlowSegmentsDao,
    private val db: WayTrafficFlowDao
) : OsmElementQuestType<OnewayAnswer> {

    private val tagFilters = """
        ways with highway ~ trunk|trunk_link|primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified|residential|living_street|pedestrian|track|road
         and !oneway and junction != roundabout and area != yes
         and (access !~ private|no or (foot and foot !~ private|no))
    """

    override val commitMessage =
        "Add whether this road is a one-way road, this road was marked as likely oneway by improveosm.org"
    override val icon = R.drawable.ic_quest_oneway
    override val hasMarkersAtEnds = true
    override val isSplitWayEnabled = true

    private val filter by lazy { FiltersParser().parse(tagFilters) }

    override fun getTitle(tags: Map<String, String>) = R.string.quest_oneway_title

    override fun isApplicableTo(element: Element) =
        filter.matches(element) && db.isForward(element.id) != null

    override fun download(bbox: BoundingBox, handler: (element: Element, geometry: ElementGeometry?) -> Unit): Boolean {
        val trafficDirectionMap: Map<Long, List<TrafficFlowSegment>>
        try {
            trafficDirectionMap = trafficFlowSegmentsDao.get(bbox)
        } catch (e: Exception) {
            Log.e("AddOneway", "Unable to download traffic metadata", e)
            return false
        }

        if (trafficDirectionMap.isEmpty()) return true

        val query = "way(id:${trafficDirectionMap.keys.joinToString(",")}); out meta geom;"
        overpassMapDataDao.query(query) { element, geometry ->
            fun checkValidAndHandle(element: Element, geometry: ElementGeometry?) {
                if (geometry == null) return
                if (geometry !is ElementPolylinesGeometry) return
                // filter the data as ImproveOSM data may be outdated or catching too much
                if (!filter.matches(element)) return

                val way = element as? Way ?: return
                val segments = trafficDirectionMap[way.id] ?: return
                /* exclude rings because the driving direction can then not be determined reliably
                   from the improveosm data and the quest should stay simple, i.e not require the
                   user to input it in those cases. Additionally, whether a ring-road is a oneway or
                   not is less valuable information (for routing) and many times such a ring will
                   actually be a roundabout. Oneway information on roundabouts is superfluous.
                   See #1320 */
                if (way.nodeIds.last() == way.nodeIds.first()) return
                /* only create quest if direction can be clearly determined and is the same
                   direction for all segments belonging to one OSM way (because StreetComplete
                   cannot split ways up) */
                val isForward = isForward(geometry.polylines.first(), segments) ?: return

                db.put(way.id, isForward)
                handler(element, geometry)
            }
            checkValidAndHandle(element, geometry)
        }

        return true
    }

    /** returns true if all given [trafficFlowSegments] point forward in relation to the
     *  direction of the OSM [way] and false if they all point backward.
     *
     *  If the segments point into different directions or their direction cannot be
     *  determined. returns null.
     */
    private fun isForward(way: List<LatLon>,trafficFlowSegments: List<TrafficFlowSegment>): Boolean? {
        var result: Boolean? = null
        for (segment in trafficFlowSegments) {
            val fromPositionIndex = findClosestPositionIndexOf(way, segment.fromPosition)
            val toPositionIndex = findClosestPositionIndexOf(way, segment.toPosition)

            if (fromPositionIndex == -1 || toPositionIndex == -1) return null
            if (fromPositionIndex == toPositionIndex) return null

            val forward = fromPositionIndex < toPositionIndex
            if (result == null) {
                result = forward
            } else if (result != forward) {
                return null
            }
        }
        return result
    }

    private fun findClosestPositionIndexOf(positions: List<LatLon>, latlon: LatLon): Int {
        var shortestDistance = 1.0
        var result = -1
        var index = 0
        for (pos in positions) {
            val distance = Math.hypot(
                pos.longitude - latlon.longitude,
                pos.latitude - latlon.latitude
            )
            if (distance < 0.00005 && distance < shortestDistance) {
                shortestDistance = distance
                result = index
            }
            index++
        }

        return result
    }

    override fun createForm() = AddOnewayForm()

    override fun applyAnswerTo(answer: OnewayAnswer, changes: StringMapChangesBuilder) {
        if (!answer.isOneway) {
            changes.add("oneway", "no")
        } else {
            changes.add("oneway", if (db.isForward(answer.wayId)!!) "yes" else "-1")
        }
    }

    override fun cleanMetadata() {
        db.deleteUnreferenced()
    }
}
