package de.westnordost.streetcomplete.quests.oneway

import android.os.Bundle
import android.text.TextUtils
import android.util.Log

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.Way
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.ElementGeometry
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.data.osm.tql.FiltersParser
import de.westnordost.streetcomplete.data.osm.tql.TagFilterExpression
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import de.westnordost.streetcomplete.util.Lazy

class AddOneway(
    private val overpassMapDataDao: OverpassMapDataDao,
    private val trafficFlowSegmentsDao: TrafficFlowSegmentsDao, private val db: WayTrafficFlowDao
) : OsmElementQuestType() {
    override val commitMessage: String
        get() = "Add whether this road is a one-way road," + " this road was marked as likely oneway by improveosm.org"
    override val icon: Int
        get() = R.drawable.ic_quest_oneway

    override fun download(bbox: BoundingBox, handler: MapDataWithGeometryHandler): Boolean {
        val trafficDirectionMap: Map<Long, List<TrafficFlowSegment>>
        try {
            trafficDirectionMap = trafficFlowSegmentsDao.get(bbox)
        } catch (e: Exception) {
            Log.e(TAG, "Unable to download traffic metadata", e)
            return false
        }

        if (trafficDirectionMap.isEmpty()) return true

        val overpassQuery =
            "way(id:" + TextUtils.join(",", trafficDirectionMap.keys) + "); out meta geom;"
        overpassMapDataDao.getAndHandleQuota(overpassQuery) { element, geometry ->
            if (geometry == null) return@overpassMapDataDao.getAndHandleQuota
            // filter the data as ImproveOSM data may be outdated or catching too much
            if (!FILTER.get()!!.matches(element)) return@overpassMapDataDao.getAndHandleQuota

            val way = element as Way
            val segments = trafficDirectionMap[way.id]
            if (segments == null) return@overpassMapDataDao.getAndHandleQuota
            val isForward = isForward(geometry!!, segments!!)
            // only create quest if direction can be clearly determined and is the same direction
            // for all segments belonging to one OSM way (because StreetComplete cannot split ways
            // up)
            if (isForward == null) return@overpassMapDataDao.getAndHandleQuota

            db.put(way.id, isForward!!)
            handler.handle(element, geometry)
        }

        return true
    }

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        val isOneway = answer.getBoolean(AddOnewayForm.ANSWER)
        if (!isOneway) {
            changes.add("oneway", "no")
        } else {
            val wayId = answer.getLong(AddOnewayForm.WAY_ID)
            changes.add("oneway", if (db.isForward(wayId)) "yes" else "-1")
        }
    }

    override fun isApplicableTo(element: Element): Boolean? {
        return FILTER.get()!!.matches(element) && db.isForward(element.id) != null
    }

    override fun cleanMetadata() {
        db.deleteUnreferenced()
    }

    override fun createForm(): AbstractQuestAnswerFragment {
        return AddOnewayForm()
    }

    override fun getTitle(tags: Map<String, String>): Int {
        return R.string.quest_oneway_title
    }

    fun hasMarkersAtEnds(): Boolean {
        return true
    }

    companion object {
        private val TAG = "AddOneway"

        private val FILTER = Lazy {
            FiltersParser().parse(
                " ways with highway ~ " +
                        "trunk|trunk_link|primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|" +
                        "unclassified|residential|living_street|pedestrian|track|road" +
                        " and !oneway and (access !~ private|no or (foot and foot !~ private|no)) and area != yes"
            )
        }

        /** @param geometry the OSM geometry
         * @param trafficFlowSegments list of segments which document a road user flow
         * @return true if all given segments point forward in relation to the direction of the OSM way
         * and false if they all point backward.<br></br>
         * If the segments point into different directions or their direction cannot be
         * determined. returns null.
         */
        private fun isForward(
            geometry: ElementGeometry,
            trafficFlowSegments: List<TrafficFlowSegment>
        ): Boolean? {
            var result: Boolean? = null
            val positions = geometry.polylines[0]
            for (segment in trafficFlowSegments) {
                val fromPositionIndex = findClosestPositionIndexOf(positions, segment.fromPosition)
                val toPositionIndex = findClosestPositionIndexOf(positions, segment.toPosition)

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
    }
}
