package de.westnordost.streetcomplete.quests.oneway_suspects

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.oneway_suspects.data.TrafficFlowSegment
import de.westnordost.streetcomplete.quests.oneway_suspects.data.TrafficFlowSegmentsApi
import de.westnordost.streetcomplete.quests.oneway_suspects.data.WayTrafficFlowDao
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlin.math.hypot

class AddSuspectedOneway(
    private val trafficFlowSegmentsApi: TrafficFlowSegmentsApi,
    private val db: WayTrafficFlowDao
) : OsmElementQuestType<SuspectedOnewayAnswer> {

    private val filter by lazy { """
        ways with
          highway ~ trunk|trunk_link|primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified|residential|living_street|pedestrian|track|road
          and !oneway
          and junction != roundabout
          and area != yes
          and (
            access !~ private|no
            or (foot and foot !~ private|no)
          )
    """.toElementFilterExpression() }

    override val changesetComment =
        "Add whether roads are one-way roads as they were marked as likely oneway by improveosm.org"
    override val wikiLink = "Key:oneway"
    override val icon = R.drawable.ic_quest_oneway
    override val hasMarkersAtEnds = true
    override val achievements = listOf(CAR)

    override val hint = R.string.quest_arrow_tutorial

    override fun getTitle(tags: Map<String, String>) = R.string.quest_oneway_title

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val bbox = mapData.boundingBox ?: return emptyList()

        val trafficDirectionMap = try {
            runBlocking(Dispatchers.IO) { trafficFlowSegmentsApi.get(bbox) }
        } catch (e: Exception) {
            Log.e("AddSuspectedOneway", "Unable to download traffic metadata", e)
            return emptyList()
        }

        val suspectedOnewayWayIds = trafficDirectionMap.keys

        val onewayCandidates = mapData.ways.filter {
            // so, only the ways suspected by improveOSM to be oneways
            it.id in suspectedOnewayWayIds
            // but also filter the data as ImproveOSM data may be outdated or catching too much
            && filter.matches(it)
            /* also exclude rings because the driving direction can then not be determined reliably
               from the improveosm data and the quest should stay simple, i.e not require the
               user to input it in those cases. Additionally, whether a ring-road is a oneway or
               not is less valuable information (for routing) and many times such a ring will
               actually be a roundabout. Oneway information on roundabouts is superfluous.
               See #1320 */
            && it.nodeIds.first() != it.nodeIds.last()
        }

        // rehash traffic direction data into simple "way id -> forward/backward" data and persist
        val onewayDirectionMap = onewayCandidates.associate { way ->
            val segments = trafficDirectionMap[way.id]
            val geometry = mapData.getWayGeometry(way.id) as? ElementPolylinesGeometry
            val isForward =
                if (segments != null && geometry != null) isForward(geometry.polylines.first(), segments) else null

            way.id to isForward
        }

        for ((wayId, isForward) in onewayDirectionMap) {
            if (isForward != null) db.put(wayId, isForward)
        }

        /* only create quest if direction could be clearly determined (isForward != null) and is the
           same direction for all segments belonging to one OSM way */
        return onewayCandidates.filter { onewayDirectionMap[it.id] != null }
    }

    override fun isApplicableTo(element: Element): Boolean =
        filter.matches(element) && db.isForward(element.id) != null

    /** returns true if all given [trafficFlowSegments] point forward in relation to the
     *  direction of the OSM [way] and false if they all point backward.
     *
     *  If the segments point into different directions or their direction cannot be
     *  determined. returns null.
     */
    private fun isForward(way: List<LatLon>, trafficFlowSegments: List<TrafficFlowSegment>): Boolean? {
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
        for ((index, pos) in positions.withIndex()) {
            val distance = hypot(
                pos.longitude - latlon.longitude,
                pos.latitude - latlon.latitude
            )
            if (distance < 0.00005 && distance < shortestDistance) {
                shortestDistance = distance
                result = index
            }
        }

        return result
    }

    override fun createForm() = AddSuspectedOnewayForm()

    override fun applyAnswerTo(answer: SuspectedOnewayAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        if (!answer.isOneway) {
            tags["oneway"] = "no"
        } else {
            tags["oneway"] = if (db.isForward(answer.wayId)!!) "yes" else "-1"
        }
    }

    override fun deleteMetadataOlderThan(timestamp: Long) {
        db.deleteUnreferenced()
    }
}
