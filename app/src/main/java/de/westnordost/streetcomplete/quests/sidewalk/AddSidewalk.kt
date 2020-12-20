package de.westnordost.streetcomplete.quests.sidewalk

import de.westnordost.osmapi.map.MapDataWithGeometry
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.ANYTHING_UNPAVED
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.util.isNearAndAligned

class AddSidewalk : OsmElementQuestType<SidewalkAnswer> {

    /* the filter additionally filters out ways that are unlikely to have sidewalks:
     * unpaved roads, roads with very low speed limits and roads that are probably not developed
     * enough to have pavement (that are not lit).
     * Also, anything explicitly tagged as no pedestrians or explicitly tagged that the sidewalk
     * is mapped as a separate way
    * */
    private val filter by lazy { """
        ways with
          highway ~ primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified|residential
          and area != yes
          and motorroad != yes
          and !sidewalk and !sidewalk:left and !sidewalk:right and !sidewalk:both
          and (!maxspeed or maxspeed > 8 or maxspeed !~ "5 mph|walk")
          and surface !~ ${ANYTHING_UNPAVED.joinToString("|")}
          and lit = yes
          and foot != no and access !~ private|no
          and foot != use_sidepath
    """.toElementFilterExpression() }

    private val maybeSeparatelyMappedSidewalksFilter by lazy { """
        ways with highway ~ path|footway|cycleway
    """.toElementFilterExpression() }

    override val commitMessage = "Add whether there are sidewalks"
    override val wikiLink = "Key:sidewalk"
    override val icon = R.drawable.ic_quest_sidewalk
    override val isSplitWayEnabled = true

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
            val minDistToWays = estimatedWidth(road.tags) / 2.0 + 6
            val roadGeometry = mapData.getWayGeometry(road.id) as? ElementPolylinesGeometry
            if (roadGeometry != null) {
                !roadGeometry.isNearAndAligned(minDistToWays, minAngleToWays, maybeSeparatelyMappedSidewalkGeometries)
            } else {
                false
            }
        }
    }

    private fun estimatedWidth(tags: Map<String, String>): Float {
        val width = tags["width"]?.toFloatOrNull()
        if (width != null) return width
        val lanes = tags["lanes"]?.toIntOrNull()
        if (lanes != null) return lanes * 3f
        return 12f
    }

    override fun isApplicableTo(element: Element): Boolean? = null

    override fun createForm() = AddSidewalkForm()

    override fun applyAnswerTo(answer: SidewalkAnswer, changes: StringMapChangesBuilder) {
        changes.add("sidewalk", getSidewalkValue(answer))
    }

    private fun getSidewalkValue(answer: SidewalkAnswer) =
        when (answer) {
            is SeparatelyMapped -> "separate"
            is SidewalkSides -> when {
                answer.left && answer.right -> "both"
                answer.left -> "left"
                answer.right -> "right"
                else -> "none"
            }
        }
}
