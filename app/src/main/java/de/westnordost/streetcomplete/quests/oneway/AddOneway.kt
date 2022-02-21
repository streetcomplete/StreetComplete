package de.westnordost.streetcomplete.quests.oneway

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.ALL_ROADS
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.estimateUsableRoadwayWidth
import de.westnordost.streetcomplete.quests.oneway.OnewayAnswer.BACKWARD
import de.westnordost.streetcomplete.quests.oneway.OnewayAnswer.FORWARD
import de.westnordost.streetcomplete.quests.oneway.OnewayAnswer.NO_ONEWAY

class AddOneway : OsmElementQuestType<OnewayAnswer> {

    /** find all roads */
    private val allRoadsFilter by lazy { """
        ways with highway ~ ${ALL_ROADS.joinToString("|")} and area != yes
    """.toElementFilterExpression() }

    /** find only those roads eligible for asking for oneway */
    private val elementFilter by lazy { """
        ways with highway ~ living_street|residential|service|tertiary|unclassified
         and lanes <= 1 and width
         and !oneway and area != yes and junction != roundabout
         and (access !~ private|no or (foot and foot !~ private|no))
    """.toElementFilterExpression() }

    override val changesetComment = "Add whether this road is a one-way road because it is quite slim"
    override val wikiLink = "Key:oneway"
    override val icon = R.drawable.ic_quest_oneway
    override val hasMarkersAtEnds = true
    override val isSplitWayEnabled = true

    override val questTypeAchievements = listOf(CAR)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_oneway2_title

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val allRoads = mapData.ways.filter { allRoadsFilter.matches(it) && it.nodeIds.size >= 2 }
        val connectionCountByNodeIds = mutableMapOf<Long, Int>()
        val onewayCandidates = mutableListOf<Way>()

        for (road in allRoads) {
            for (nodeId in road.nodeIds) {
                val prevCount = connectionCountByNodeIds[nodeId] ?: 0
                connectionCountByNodeIds[nodeId] = prevCount + 1
            }
            if (isOnewayRoadCandidate(road)) {
                onewayCandidates.add(road)
            }
        }

        return onewayCandidates.filter {
            /* ways that are simply at the border of the download bounding box are treated as if
               they are dead ends. This is fine though, because it only leads to this quest not
               showing up for those streets (which is better than the other way round)
            */
            // check if the way has connections to other roads at both ends
            (connectionCountByNodeIds[it.nodeIds.first()] ?: 0) > 1 &&
            (connectionCountByNodeIds[it.nodeIds.last()] ?: 0) > 1
        }
    }

    override fun isApplicableTo(element: Element): Boolean? {
        if (!isOnewayRoadCandidate(element)) return false
        /* return null because oneway candidate roads must also be connected on both ends with other
           roads for which we'd need to look at surrounding geometry */
        return null
    }

    private fun isOnewayRoadCandidate(road: Element): Boolean {
        if (!elementFilter.matches(road)) return false
        // check if the width of the road minus the space consumed by other stuff is quite narrow
        val usableWidth = estimateUsableRoadwayWidth(road.tags) ?: return false
        return usableWidth <= 4f
    }

    override fun createForm() = AddOnewayForm()

    override fun applyAnswerTo(answer: OnewayAnswer, tags: Tags, timestampEdited: Long) {
        tags["oneway"] = when (answer) {
            FORWARD -> "yes"
            BACKWARD -> "-1"
            NO_ONEWAY -> "no"
        }
    }
}
