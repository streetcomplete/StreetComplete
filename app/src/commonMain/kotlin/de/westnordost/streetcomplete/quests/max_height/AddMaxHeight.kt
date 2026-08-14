package de.westnordost.streetcomplete.quests.max_height

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.util.ktx.containsAny
import de.westnordost.streetcomplete.util.math.intersects

class AddMaxHeight : OsmElementQuestType<MaxHeightAnswer> {

    private val nodeFilter by lazy { """
        nodes with
        (
          barrier = height_restrictor
          or amenity = parking_entrance and parking ~ underground|multi-storey
        )
        and $noMaxHeight
    """.toElementFilterExpression() }

    private val roadsWithoutMaxHeightFilter by lazy { """
        ways with
        (
          highway ~ motorway|motorway_link|trunk|trunk_link|primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified|residential|living_street|track|road|busway
          or (highway = service and access !~ private|no and vehicle !~ private|no)
        )
        and $noMaxHeight
        and (access !~ private|no or (foot and foot !~ private|no))
    """.toElementFilterExpression() }

    private val railwayCrossingsFilter by lazy { """
        nodes with
          railway = level_crossing
          and $noMaxHeight
    """.toElementFilterExpression() }

    private val electrifiedRailwaysFilter by lazy { """
        ways with
          railway and railway != tram
          and electrified = contact_line
    """.toElementFilterExpression() }
    // not trams because people tell me it is extremely unlikely that it is signed - at least
    // directly at the crossing, anyway. Also, since a tram crosses with a road so often, it is
    // kind of spammy, especially if the answer is virtually always(?) "not signed"

    private val allRoadsFilter by lazy { """
        ways with
          highway ~ ${ALL_ROADS.joinToString("|")}
          and (access !~ private|no or (foot and foot !~ private|no))
    """.toElementFilterExpression() }

    private val allPathsFilter by lazy { """
        ways with
          highway ~ ${ALL_PATHS.joinToString("|")}
          and (access !~ private|no or (foot and foot !~ private|no))
    """.toElementFilterExpression() }

    private val noMaxHeight = """
        !maxheight
        and !maxheight:signed
        and !maxheight:physical
        and (!maxheight:forward or !maxheight:backward)
        and !maxheight:lanes
    """

    override val changesetComment = "Specify maximum heights"
    override val wikiLink = "Key:maxheight"
    override val icon = Res.drawable.quest_max_height
    override val title = Res.string.quest_maxheight_sign_title
    override val achievements = listOf(CAR)

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        // amenity = parking_entrance nodes etc. only if they are a vertex in a road
        val roadsNodeIds = mapData.ways
            .filter { allRoadsFilter.matches(it) }
            .flatMapTo(HashSet()) { it.nodeIds }

        val pathNodeIds = mapData.ways
            .filter { allPathsFilter.matches(it) }
            .flatMapTo(HashSet()) { it.nodeIds }

        val nodesWithoutHeight = mapData.nodes
            .filter { it.id in roadsNodeIds && nodeFilter.matches(it) }

        // railway crossings with railways that have an electrified contact line
        val electrifiedRailwayNodeIds = mapData.ways
            .filter { electrifiedRailwaysFilter.matches(it) }
            .flatMapTo(HashSet()) { it.nodeIds }

        val railwayCrossingNodesWithoutHeight = mapData.nodes
            .filter {
                (it.id in roadsNodeIds || it.id in pathNodeIds)
                && it.id in electrifiedRailwayNodeIds
                && railwayCrossingsFilter.matches(it)
            }

        // tunnels without height
        val roadsWithoutHeight = mapData.ways.filter { roadsWithoutMaxHeightFilter.matches(it) }

        val tunnelsWithoutHeight = roadsWithoutHeight.filter { tunnelFilter.matches(it) }

        // ways below bridges without height
        val bridges = mapData.ways.filter { bridgeFilter.matches(it) }

        val waysBelowBridgesWithoutHeight = roadsWithoutHeight.filter { way ->
            val layer = way.tags["layer"]?.toIntOrNull() ?: 0
            val geometry = mapData.getWayGeometry(way.id) as? ElementPolylinesGeometry

            // applicable if with any bridge...
            geometry != null && bridges.any { bridge ->
                val bridgeGeometry = mapData.getWayGeometry(bridge.id)
                val bridgeLayer = bridge.tags["layer"]?.toIntOrNull() ?: 0

                // , that is in a layer above this way
                bridgeLayer > layer
                    // and with which it does not share any node (=connects) (#2555)
                    && !bridge.nodeIds.toSet().containsAny(way.nodeIds)
                    // , it intersects
                    && bridgeGeometry != null && bridgeGeometry.intersects(geometry)
            }
        }

        return nodesWithoutHeight +
            railwayCrossingNodesWithoutHeight +
            tunnelsWithoutHeight +
            waysBelowBridgesWithoutHeight
    }

    override fun isApplicableTo(element: Element): Boolean? {
        if (roadsWithoutMaxHeightFilter.matches(element)) {
            if (tunnelFilter.matches(element)) return true
            // if it is a way but not a tunnel, we cannot determine whether it is applicable (=
            // below a bridge) just by looking at the tags
            return null
        }
        // for nodes that may be applicable we cannot finally determine it because that node must be
        // a vertex of a road
        if (nodeFilter.matches(element)) return null
        // same for railway crossing
        if (railwayCrossingsFilter.matches(element)) return null
        return false
    }

    @Composable
    override fun Form(on: (QuestAction<MaxHeightAnswer>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        AddMaxHeightForm(on, element, countryInfo)
    }

    override fun applyAnswerTo(answer: MaxHeightAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is MaxHeight -> {
                tags["maxheight"] = answer.value.toOsmValue()
            }
            is NoMaxHeightSign -> {
                tags["maxheight:signed"] = "no"
            }
        }
    }

    override fun getHighlightedElements(
        element: Element,
        mapData: MapDataWithGeometry
    ): Sequence<Element> = (element as? Way)?.getIntersectingBridges(mapData).orEmpty()
}
