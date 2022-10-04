package de.westnordost.streetcomplete.quests.sidewalk

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.osm.ANYTHING_UNPAVED
import de.westnordost.streetcomplete.osm.MAXSPEED_TYPE_KEYS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.estimateCycleTrackWidth
import de.westnordost.streetcomplete.osm.estimateParkingOffRoadWidth
import de.westnordost.streetcomplete.osm.estimateRoadwayWidth
import de.westnordost.streetcomplete.osm.guessRoadwayWidth
import de.westnordost.streetcomplete.osm.sidewalk.LeftAndRightSidewalk
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.INVALID
import de.westnordost.streetcomplete.osm.sidewalk.applyTo
import de.westnordost.streetcomplete.osm.sidewalk.createSidewalkSides
import de.westnordost.streetcomplete.util.math.isNearAndAligned

class AddSidewalk : OsmElementQuestType<LeftAndRightSidewalk> {
    private val maybeSeparatelyMappedSidewalksFilter by lazy { """
        ways with highway ~ path|footway|cycleway|construction
    """.toElementFilterExpression() }
    // highway=construction included, as situation often changes during and after construction

    override val changesetComment = "Specify whether roads have sidewalks"
    override val wikiLink = "Key:sidewalk"
    override val icon = R.drawable.ic_quest_sidewalk
    override val achievements = listOf(PEDESTRIAN)
    override val defaultDisabledMessage = R.string.default_disabled_msg_overlay

    override fun getTitle(tags: Map<String, String>) = R.string.quest_sidewalk_title

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val eligibleRoads = mapData.ways.filter { roadsFilter.matches(it) }
        val roadsWithMissingSidewalks = eligibleRoads.filter { untaggedRoadsFilter.matches(it) }.toMutableList()

        /* Unfortunately, the filter above is not enough. In OSM, sidewalks may be mapped as
         * separate ways as well and it is not guaranteed that in this case, sidewalk = separate
         * (or foot = use_sidepath) is always tagged on the main road then. So, all roads should
         * be excluded whose center is within of ~15 meters of a footway, to be on the safe side. */

        if (roadsWithMissingSidewalks.isNotEmpty()) {

            val maybeSeparatelyMappedSidewalkGeometries = mapData.ways
                .filter { maybeSeparatelyMappedSidewalksFilter.matches(it) }
                .mapNotNull { mapData.getWayGeometry(it.id) as? ElementPolylinesGeometry }
            if (maybeSeparatelyMappedSidewalkGeometries.isEmpty()) return roadsWithMissingSidewalks

            val minAngleToWays = 25.0

            if (maybeSeparatelyMappedSidewalkGeometries.isNotEmpty()) {
                // filter out roads with missing sidewalks that are near footways
                roadsWithMissingSidewalks.removeAll { road ->
                    val minDistToWays = getMinDistanceToWays(road.tags).toDouble()
                    val roadGeometry = mapData.getWayGeometry(road.id) as? ElementPolylinesGeometry
                    roadGeometry?.isNearAndAligned(
                        minDistToWays,
                        minAngleToWays,
                        maybeSeparatelyMappedSidewalkGeometries
                    ) ?: true
                }
            }
        }

        // Also include any roads with invalid (overloaded) or incomplete sidewalk tagging
        val roadsWithInvalidSidewalkTags = eligibleRoads.filter { way ->
            way.hasInvalidOrIncompleteSidewalkTags()
        }

        return roadsWithMissingSidewalks + roadsWithInvalidSidewalkTags
    }

    private fun getMinDistanceToWays(tags: Map<String, String>): Float =
        (
            (estimateRoadwayWidth(tags) ?: guessRoadwayWidth(tags)) +
            (estimateParkingOffRoadWidth(tags) ?: 0f) +
            (estimateCycleTrackWidth(tags) ?: 0f)
        ) / 2f +
        4f // + generous buffer for possible grass verge

    override fun isApplicableTo(element: Element): Boolean? {
        if (!roadsFilter.matches(element)) return false

        /* can't determine for yet untagged roads by the tags alone because we need info about
           surrounding geometry */
        if (untaggedRoadsFilter.matches(element)) return null

        /* but if already tagged and invalid or incomplete, we don't need to look at surrounding
           geometry to see if it is applicable or not */
        return element.hasInvalidOrIncompleteSidewalkTags()
    }

    override fun createForm() = AddSidewalkForm()

    override fun applyAnswerTo(answer: LeftAndRightSidewalk, tags: Tags, timestampEdited: Long) {
        answer.applyTo(tags)
    }

    companion object {
        // streets that may have sidewalk tagging
        private val roadsFilter by lazy { """
            ways with
              (
                (
                  highway ~ trunk|trunk_link|primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified|residential|service
                  and motorroad != yes
                  and foot != no
                )
                or
                (
                  highway ~ motorway|motorway_link|trunk|trunk_link|primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified|residential|service
                  and (foot ~ yes|designated or bicycle ~ yes|designated)
                )
              )
              and area != yes
              and access !~ private|no
        """.toElementFilterExpression() }

        // streets that do not have sidewalk tagging yet
        /* the filter additionally filters out ways that are unlikely to have sidewalks:
         *
         * + unpaved roads
         * + roads that are probably not developed enough to have sidewalk (i.e. country roads)
         * + roads with a very low speed limit
         * + Also, anything explicitly tagged as no pedestrians or explicitly tagged that the sidewalk
         *   is mapped as a separate way OR that is tagged with that the cycleway is separate. If the
         *   cycleway is separate, the sidewalk is too for sure
        * */
        private val untaggedRoadsFilter by lazy { """
            ways with
              highway ~ motorway|motorway_link|trunk|trunk_link|primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified|residential
              and !sidewalk and !sidewalk:both and !sidewalk:left and !sidewalk:right
              and (!maxspeed or maxspeed > 9)
              and surface !~ ${ANYTHING_UNPAVED.joinToString("|")}
              and (
                lit = yes
                or highway = residential
                or ~${(MAXSPEED_TYPE_KEYS + "maxspeed").joinToString("|")} ~ .*urban|.*zone.*
                or (foot ~ yes|designated and highway ~ motorway|motorway_link|trunk|trunk_link|primary|primary_link|secondary|secondary_link)
              )
              and foot != use_sidepath
              and bicycle != use_sidepath
              and bicycle:backward != use_sidepath
              and bicycle:forward != use_sidepath
              and cycleway != separate
              and cycleway:left != separate
              and cycleway:right != separate
              and cycleway:both != separate
        """.toElementFilterExpression() }
    }

    private fun Element.hasInvalidOrIncompleteSidewalkTags(): Boolean {
        val sides = createSidewalkSides(tags) ?: return false
        if (sides.any { it == INVALID || it == null }) return true
        return false
    }
}

private fun LeftAndRightSidewalk.any(block: (sidewalk: Sidewalk?) -> Boolean): Boolean =
    block(left) || block(right)
