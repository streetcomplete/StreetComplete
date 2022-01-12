package de.westnordost.streetcomplete.quests.crossing

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.*
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.quests.findNodesAtCrossingsOf
import de.westnordost.streetcomplete.quests.kerb_height.AddKerbHeightForm
import de.westnordost.streetcomplete.quests.kerb_height.KerbHeight
import de.westnordost.streetcomplete.util.isRightOf

class AddCrossing : OsmElementQuestType<KerbHeight> {

    private val roadsFilter by lazy { """
        ways with
          highway ~ trunk|trunk_link|primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified|residential
          and area != yes
          and (access !~ private|no or (foot and foot !~ private|no))
    """.toElementFilterExpression() }

    private val footwaysFilter by lazy { """
        ways with
          (highway ~ footway|steps or highway ~ path|cycleway and foot ~ designated|yes)
          and footway !~ sidewalk|crossing
          and area != yes
          and access !~ private|no
    """.toElementFilterExpression() }

    /* It is neither asked for sidewalks nor crossings (=separately mapped sidewalk infrastructure)
    *  because a "no" answer would require to also delete/adapt the crossing ways, rather than just
    *  tagging crossing=no on the vertex.
    *  See https://github.com/streetcomplete/StreetComplete/pull/2999#discussion_r681516203 */

    override val commitMessage = "Add whether there is a crossing"
    override val wikiLink = "Tag:highway=crossing"
    override val icon = R.drawable.ic_quest_pedestrian

    override val questTypeAchievements = listOf(PEDESTRIAN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_crossing_title

    override fun isApplicableTo(element: Element): Boolean? =
        if(element !is Node || element.tags.isNotEmpty()) false else null

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val barrierWays = mapData.ways.asSequence()
            .filter { roadsFilter.matches(it) }

        val movingWays = mapData.ways.asSequence()
            .filter { footwaysFilter.matches(it) }

        var crossings = findNodesAtCrossingsOf(barrierWays, movingWays, mapData)

        /* require all roads at a shared node to either have no sidewalk tagging or all of them to
         * have sidewalk tagging: If the sidewalk tagging changes at that point, it may be an
         * indicator that this is the transition point between separate sidewalk mapping and
         * sidewalk mapping on road-way. F.e.:
         * https://www.openstreetmap.org/node/1839120490 */
        val anySidewalk = setOf("both","left","right")

        crossings = crossings.filter {
            it.barrierWays.all { way ->
                (way.tags["sidewalk"] in anySidewalk)
            }
            ||
            it.barrierWays.all { way ->
                way.tags["sidewalk"] !in anySidewalk
            }
        }
        return crossings.map { it.node }.filter { it.tags.isEmpty() }
    }

    override fun createForm() = AddKerbHeightForm()

    override fun applyAnswerTo(answer: KerbHeight, changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate("kerb", answer.osmValue)
        /* So, we don't assume there is a crossing here for kerb=no and kerb=raised.

           As most actual crossings will have at least lowered kerbs, this is a good indicator.

           When there is no kerb at all, it is likely that this is a situation where the footway
           or road drawn in OSM are just virtual, to connect the geometry. In other words, it may be
           just e.g. an asphalted area, which does not really classify as a crossing.
         */

        if (answer.osmValue in listOf("lowered", "flush")) {
            changes.add("highway", "crossing")
        }
    }
}

/** get the node id(s) neighbouring to the given node id */
private fun Way.getNodeIdsNeighbouringNodeId(nodeId: Long): List<Long> {
    val idx = nodeIds.indexOf(nodeId)
    if (idx == -1) return emptyList()
    val prevNodeId = if (idx > 0) nodeIds[idx - 1] else null
    val nextNodeId = if (idx < nodeIds.size - 1) nodeIds[idx + 1] else null
    return listOfNotNull(prevNodeId, nextNodeId)
}

private fun MutableMap<Long, MutableList<Way>>.removeEndNodes() {
    entries.removeAll { (nodeId, ways) ->
        ways.size == 1 && (nodeId == ways[0].nodeIds.first() || nodeId == ways[0].nodeIds.last())
    }
}

/** groups the sequence of ways to a map of node id -> list of ways */
private fun Sequence<Way>.groupByNodeIds(): MutableMap<Long, MutableList<Way>> {
    val result = mutableMapOf<Long, MutableList<Way>>()
    forEach { way ->
        way.nodeIds.forEach { nodeId ->
            result.getOrPut(nodeId, { mutableListOf() } ).add(way)
        }
    }
    return result
}

/** Returns whether any of the lines spanned by any of the points in this list through the
 *  vertex point cross any of the lines spanned by any of the points through the vertex point
 *  from the other list */
private fun List<LatLon>.anyCrossesAnyOf(other: List<LatLon>, vertex: LatLon): Boolean =
    (1 until size).any { i -> other.anyAreOnDifferentSidesOf(this[0], vertex, this[i]) }

/** Returns whether any of the points in this list are on different sides of the line spanned
 *  by p0 and p1 and the line spanned by p1 and p2 */
private fun List<LatLon>.anyAreOnDifferentSidesOf(p0: LatLon, p1: LatLon, p2: LatLon): Boolean =
    map { it.isRightOf(p0, p1, p2) }.toSet().size > 1
