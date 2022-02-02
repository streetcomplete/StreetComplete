package de.westnordost.streetcomplete.quests.sidewalk

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.ANYTHING_UNPAVED
import de.westnordost.streetcomplete.data.meta.MAXSPEED_TYPE_KEYS
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.osm.estimateCycleTrackWidth
import de.westnordost.streetcomplete.osm.estimateParkingOffRoadWidth
import de.westnordost.streetcomplete.osm.estimateRoadwayWidth
import de.westnordost.streetcomplete.osm.guessRoadwayWidth
import de.westnordost.streetcomplete.util.isNearAndAligned

class AddSidewalk : OsmElementQuestType<SidewalkSides> {

    /* the filter additionally filters out ways that are unlikely to have sidewalks:
     *
     * + unpaved roads
     * + roads that are probably not developed enough to have sidewalk (i.e. country roads)
     * + roads with a very low speed limit
     * + Also, anything explicitly tagged as no pedestrians or explicitly tagged that the sidewalk
     *   is mapped as a separate way OR that is tagged with that the cycleway is separate. If the
     *   cycleway is separate, the sidewalk is too for sure
    * */
    private val filter by lazy { """
        ways with
          highway ~ trunk|trunk_link|primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified|residential
          and area != yes
          and motorroad != yes
          and !sidewalk and !sidewalk:left and !sidewalk:right and !sidewalk:both
          and (
            !maxspeed
            or maxspeed > 8
            or (maxspeed ~ ".*mph" and maxspeed !~ "[1-5] mph")
          )
          and surface !~ ${ANYTHING_UNPAVED.joinToString("|")}
          and (
            lit = yes
            or highway = residential
            or ~${(MAXSPEED_TYPE_KEYS + "maxspeed").joinToString("|")} ~ .*urban|.*zone.*
          )
          and foot != no
          and access !~ private|no
          and foot != use_sidepath
          and bicycle != use_sidepath
          and bicycle:backward != use_sidepath
          and bicycle:forward != use_sidepath
          and cycleway != separate
          and cycleway:left != separate
          and cycleway:right != separate
          and cycleway:both != separate
    """.toElementFilterExpression() }

    private val maybeSeparatelyMappedSidewalksFilter by lazy { """
        ways with highway ~ path|footway|cycleway|construction
    """.toElementFilterExpression() }
    // highway=construction included, as situation often changes during and after construction

    override val changesetComment = "Add whether there are sidewalks"
    override val wikiLink = "Key:sidewalk"
    override val icon = R.drawable.ic_quest_sidewalk
    override val isSplitWayEnabled = true
    override val questTypeAchievements = listOf(PEDESTRIAN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_sidewalk_title

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val roadsWithMissingSidewalks = mapData.ways.filter { filter.matches(it) }
        if (roadsWithMissingSidewalks.isEmpty()) return emptyList()

        /* Unfortunately, the filter above is not enough. In OSM, sidewalks may be mapped as
         * separate ways as well and it is not guaranteed that in this case, sidewalk = separate
         * (or foot = use_sidepath) is always tagged on the main road then. So, all roads should
         * be excluded whose center is within of ~15 meters of a footway, to be on the safe side. */

        val maybeSeparatelyMappedSidewalkGeometries = mapData.ways
            .filter { maybeSeparatelyMappedSidewalksFilter.matches(it) }
            .mapNotNull { mapData.getWayGeometry(it.id) as? ElementPolylinesGeometry }
        if (maybeSeparatelyMappedSidewalkGeometries.isEmpty()) return roadsWithMissingSidewalks

        val minAngleToWays = 25.0

        // filter out roads with missing sidewalks that are near footways
        return roadsWithMissingSidewalks.filter { road ->
            val minDistToWays = getMinDistanceToWays(road.tags).toDouble()
            val roadGeometry = mapData.getWayGeometry(road.id) as? ElementPolylinesGeometry
            if (roadGeometry != null) {
                !roadGeometry.isNearAndAligned(minDistToWays, minAngleToWays, maybeSeparatelyMappedSidewalkGeometries)
            } else {
                false
            }
        }
    }

    private fun getMinDistanceToWays(tags: Map<String, String>): Float =
        (
            (estimateRoadwayWidth(tags) ?: guessRoadwayWidth(tags)) +
            (estimateParkingOffRoadWidth(tags) ?: 0f) +
            (estimateCycleTrackWidth(tags) ?: 0f)
        ) / 2f +
        4f // + generous buffer for possible grass verge

    override fun isApplicableTo(element: Element): Boolean? =
        if (!filter.matches(element)) false else null

    override fun createForm() = AddSidewalkForm()

    override fun applyAnswerTo(answer: SidewalkSides, tags: Tags, timestampEdited: Long) {
        answer.applyTo(tags)
    }
}
