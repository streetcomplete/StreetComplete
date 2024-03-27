package de.westnordost.streetcomplete.quests.destination

import android.content.Context
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Relation
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.groupByNodeIds
import de.westnordost.streetcomplete.osm.isForwardOneway
import de.westnordost.streetcomplete.osm.isReversedOneway
import de.westnordost.streetcomplete.quests.questPrefix
import de.westnordost.streetcomplete.quests.singleTypeElementSelectionDialog
import de.westnordost.streetcomplete.util.ktx.allExceptFirstAndLast
import de.westnordost.streetcomplete.util.math.finalBearingTo
import de.westnordost.streetcomplete.util.math.initialBearingTo
import de.westnordost.streetcomplete.util.math.isCompletelyInside
import de.westnordost.streetcomplete.util.math.normalizeDegrees
import kotlin.math.abs

class AddDestination : OsmElementQuestType<Pair<DestinationLanes?, DestinationLanes?>> {

    // need to filter elements with not-counting lanes
    // later lanes could be counted from available data if possible
    private val roadsFilter by lazy { """
        ways with
          highway ~ ${prefs.getString(questPrefix(prefs) + PREF_DESTINATION_ROADS, ROADS_FOR_DESTINATION.joinToString("|"))}
          and !destination and !~ destination:.*
          and junction !~ roundabout|circular
          and (oneway = yes or (!oneway and (!lanes or lanes = 2)))
          and cycleway != lane and !cycleway:lane and !cycleway:lanes and !bicycle:lanes and cycleway:left != lane and cycleway:right != lane and cycleway:both != lane and cycleway != opposite_lane
          and !motorcycle:lanes
    """.toElementFilterExpression()
    }

    // this filter must contain all ways matched by roadFilter
    // essentially these are the starting roads from which roads on the roadsFilter are reached
    // is there any reason not to use ALL_ROADS?
    private val branchingOffFromFilter by lazy { """
        ways with
          highway ~ ${prefs.getString(questPrefix(prefs) + PREF_DESTINATION_ROADS, ROADS_FOR_DESTINATION.joinToString("|"))}
    """.toElementFilterExpression() }

    override val changesetComment = "Add destination"
    override val wikiLink = "Key:destination"
    override val icon = R.drawable.ic_quest_destination // not nice, but ok for now
    override val defaultDisabledMessage = R.string.default_disabled_msg_ee

    override fun getTitle(tags: Map<String, String>) = R.string.quest_destination_title

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        // we need the bbox because we only want ways fully in bbox (less strict in overlay...)
        val bbox = mapData.boundingBox ?: return emptyList()
        // we need from-members of restrictions
        val restrictionsByFromMemberId = hashMapOf<Long, Relation>()
        // and we don't want to tag destination on to-members of destination_sign
        val destinationSignToMembersIds = hashSetOf<Long>()
        mapData.relations.forEach { rel ->
            when (rel.tags["type"]) {
                "destination_sign" -> rel.members.forEach { if (it.type == ElementType.WAY && it.role == "to") destinationSignToMembersIds.add(it.ref) }
                // technically a way could be member of more than 1 restriction with same role (in different directions), but ignore it for now
                "restriction" -> rel.members.forEach { if (it.type == ElementType.WAY && it.role == "from") restrictionsByFromMemberId[it.ref] = rel }
            }
        }

        // this needs to contain all roads matching roadFilter!
        val roads = mapData.ways.filter { branchingOffFromFilter.matches(it) }
        val roadsByNodeId = mapData.ways.filter { branchingOffFromFilter.matches(it) }.asSequence().groupByNodeIds()

        // maybe could be done without separating oneway and twoway here
        val (oneWayCandidates, twoWayCandidates) = roads.filter {
            roadsFilter.matches(it)
                && it.id !in destinationSignToMembersIds
                && mapData.getWayGeometry(it.id)?.getBounds()?.isCompletelyInside(bbox) == true // because otherwise filter below may fail
                && it.nodeIds.allExceptFirstAndLast().all { roadsByNodeId[it]!!.size == 1 } // ignore roads with branches not at the end (need to split, but that's for the overlay)
        }
            .partition { it.tags["oneway"] == "yes" } // other oneway tags are ignored by the filter
        if (oneWayCandidates.isEmpty() && twoWayCandidates.isEmpty()) return emptyList()

        // oneway = yes -> only care about first node
        val oneWayCandidatesByFirstNodeId = oneWayCandidates.groupBy { it.nodeIds.first() }
        // not oneway -> need both end nodes
        val twoWayCandidatesByEndNodeIds = hashMapOf<Long, MutableList<Way>>()
        twoWayCandidates.forEach {
            twoWayCandidatesByEndNodeIds.getOrPut(it.nodeIds.first()) { mutableListOf() }.add(it)
            twoWayCandidatesByEndNodeIds.getOrPut(it.nodeIds.last()) { mutableListOf() }.add(it)
        }

        // remove nodes that are not start/end nodes for a candidate
        roadsByNodeId.keys.retainAll(oneWayCandidatesByFirstNodeId.keys + twoWayCandidatesByEndNodeIds.keys) // only keep nodes where a candidate starts or ends

        // find
        val eligibleWays = hashSetOf<Way>()

        for ((nodeId, ways) in roadsByNodeId) {
            if (ways.size < 2) continue // we need at least 2 ways, or it doesn't make sense

            val oneWay = oneWayCandidatesByFirstNodeId[nodeId] ?: emptyList()
            val twoWay = twoWayCandidatesByEndNodeIds[nodeId] ?: emptyList()
            val candidates = oneWay + twoWay

            // check if we can go from way to at any of the other ways
            for (way in ways) {
                val otherWays = ways - way

                // if none of the other ways is a candidate, there is nothing to do
                if (otherWays.none { it in candidates }) continue

                // if we can't travel to this node on this way, there is nothing to do
                if (!way.allowsFromAnyNeighboringNodeTo(nodeId)) continue

                // if relation exists, way is a from-member -> do nothing if it's only_ and the via node is nodeId
                val rel = restrictionsByFromMemberId[way.id]
                if (rel != null
                    && rel.tags["restriction"]?.startsWith("only_") == true
                    && rel.members.any { it.type == ElementType.NODE && it.ref == nodeId && it.role == "via" }
                ) continue

                // we want the bearing when going towards nodeId on way for the turn degrees check
                val wayBearings = way.getAllowedBearingGoingTo(nodeId, mapData)

                val otherAvailableWays = mutableListOf<Way>()
                for (otherWay in otherWays) {
                    // ignore way if we can't enter
                    if (!otherWay.allowsToAnyNeighboringNodeFrom(nodeId)) continue

                    // ignore way if we are not allowed by restriction (actually this ignores via ways...)
                    if (rel != null
                        && rel.tags["restriction"]?.startsWith("no_") == true
                        && rel.members.any { it.type == ElementType.WAY && it.ref == otherWay.id && it.role == "to" }
                    ) continue

                    // ignore if we would need to turn by more than 115°
                    // we want the bearing when leaving from nodeId on otherWay
                    val otherWayBearings = otherWay.getAllowedBearingStartingAt(nodeId, mapData)
                    if (wayBearings.any { b -> otherWayBearings.any { abs(normalizeDegrees(b - it, -180.0)) < 115 } })
                        otherAvailableWays.add(otherWay)
                }

                // take candidates from otherWays, but only if there are at least 2 otherWays or
                // a single one that goes through nodeId
                if (otherAvailableWays.size > 1 || otherAvailableWays.any { it.nodeIds.allExceptFirstAndLast().contains(nodeId) })
                    eligibleWays.addAll(otherAvailableWays.filter { it in candidates })
            }
        }
        return eligibleWays
    }

    override fun isApplicableTo(element: Element): Boolean? =
        if (roadsFilter.matches(element)) null
        else false

    override fun createForm() = AddDestinationForm()

    override fun applyAnswerTo(answer: Pair<DestinationLanes?, DestinationLanes?>, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        answer.first?.applyTo(tags, false)
        answer.second?.applyTo(tags, true)
    }

    override val hasQuestSettings = true

    override fun getQuestSettingsDialog(context: Context): AlertDialog {
        return singleTypeElementSelectionDialog(context,
            prefs,
            questPrefix(prefs) + PREF_DESTINATION_ROADS,
            ROADS_FOR_DESTINATION.joinToString("|"),
            R.string.quest_settings_eligible_highways)
    }
}

private const val PREF_DESTINATION_ROADS = "qs_AddDestination_road_selection"
private val ROADS_FOR_DESTINATION = listOf("motorway", "motorway_link", "trunk", "trunk_link",
    "primary", "primary_link", "secondary", "secondary_link", "tertiary", "tertiary_link")

// returns bearings going from [nodeId] to a neighboring node
// but only towards nodes allowed by oneway
private fun Way.getAllowedBearingStartingAt(nodeId: Long, mapData: MapDataWithGeometry): List<Double> {
    if (!allowsToAnyNeighboringNodeFrom(nodeId)) return emptyList()
    val startPos = mapData.getNode(nodeId)!!.position
    val nodeIndex = nodeIds.indexOf(nodeId)
    // ways in mapData are complete, so we must have all way nodes
    return when (nodeIndex) {
        0 -> {
            val p = mapData.getNode(nodeIds[1])?.position
            require(p != null) { "node ${nodeIds[1]} not in mapData, but it's in way $this" }
            listOf(startPos.initialBearingTo(p))
        }
        nodeIds.lastIndex -> listOf(startPos.initialBearingTo(mapData.getNode(nodeIds[nodeIndex - 1])!!.position))
        else -> listOfNotNull(
            if (!isReversedOneway(tags)) // i to i+1 not allowed if reverse
                startPos.initialBearingTo(mapData.getNode(nodeIds[nodeIndex + 1])!!.position)
            else null,
            if (!isForwardOneway(tags)) // i to i-1 not allowed if forward
                startPos.initialBearingTo(mapData.getNode(nodeIds[nodeIndex - 1])!!.position)
            else null,
        )
    }
}

// returns bearings going from a neighboring node to [nodeId]
// but only for directions allowed by oneway
private fun Way.getAllowedBearingGoingTo(nodeId: Long, mapData: MapDataWithGeometry): List<Double> {
    if (!allowsFromAnyNeighboringNodeTo(nodeId)) return emptyList()
    val endPos = mapData.getNode(nodeId)!!.position
    val nodeIndex = nodeIds.indexOf(nodeId)
    // ways in mapData are complete, so we must have all way nodes
    return when (nodeIndex) {
        0 -> {
            val n = mapData.getNode(nodeIds[1])
            require(n != null) { "node ${nodeIds[1]} not in mapData, but it's in way $this" }
            listOf(n.position.finalBearingTo(endPos))
        }
        nodeIds.lastIndex -> listOf(mapData.getNode(nodeIds[nodeIndex - 1])!!.position.finalBearingTo(endPos))
        else -> listOfNotNull(
            if (!isForwardOneway(tags)) // i+1 to i not allowed if forward
                mapData.getNode(nodeIds[nodeIndex + 1])!!.position.finalBearingTo(endPos)
            else null,
            if (!isReversedOneway(tags)) // i-1 to i not allowed if reverse
                mapData.getNode(nodeIds[nodeIndex - 1])!!.position.finalBearingTo(endPos)
            else null,
        )
    }
}

private fun Way.allowsFromAnyNeighboringNodeTo(nodeId: Long): Boolean {
    val onewayForward = isForwardOneway(tags)
    val onewayBackward = isReversedOneway(tags)
    if (!onewayForward && !onewayBackward) return true
    return when (nodeIds.indexOf(nodeId)) {
        0 -> onewayBackward
        nodeIds.lastIndex -> onewayForward
        else -> true
    }
}

private fun Way.allowsToAnyNeighboringNodeFrom(nodeId: Long): Boolean {
    val onewayForward = isForwardOneway(tags)
    val onewayBackward = isReversedOneway(tags)
    if (!onewayForward && !onewayBackward) return true
    return when (nodeIds.indexOf(nodeId)) {
        0 -> onewayForward
        nodeIds.lastIndex -> onewayBackward
        else -> true
    }
}
