package de.westnordost.streetcomplete.quests.max_height

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.util.ktx.containsAny
import de.westnordost.streetcomplete.util.math.intersects

class AddMaxHeight : OsmElementQuestType<MaxHeightAnswer> {

    private val nodeFilter by lazy { """
        nodes with
        (
          barrier = height_restrictor
          or amenity = parking_entrance and parking ~ underground|multi-storey
        )
        and !maxheight and !maxheight:signed and !maxheight:physical
    """.toElementFilterExpression() }

    private val roadsWithoutMaxHeightFilter by lazy { """
        ways with
        (
          highway ~ motorway|motorway_link|trunk|trunk_link|primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified|residential|living_street|track|road
          or (highway = service and access !~ private|no and vehicle !~ private|no)
        )
        and !maxheight and !maxheight:signed and !maxheight:physical
    """.toElementFilterExpression() }

    private val railwayCrossingsFilter by lazy { """
        nodes with
          railway = level_crossing
          and !maxheight and !maxheight:signed and !maxheight:physical
    """.toElementFilterExpression() }

    private val electrifiedRailwaysFilter by lazy { """
        ways with
          railway
          and electrified = contact_line
    """.toElementFilterExpression() }

    private val allRoadsFilter by lazy { """
        ways with highway ~ ${ALL_ROADS.joinToString("|")}
    """.toElementFilterExpression() }

    private val tunnelFilter by lazy { """
        ways with highway and (covered = yes or tunnel ~ yes|building_passage|avalanche_protector)
    """.toElementFilterExpression() }

    private val bridgeFilter by lazy { """
        ways with (
            highway ~ ${(ALL_ROADS + ALL_PATHS).joinToString("|")}
            or railway ~ rail|light_rail|subway|narrow_gauge|tram|disused|preserved|funicular|monorail
          ) and (
            bridge and bridge != no
            or man_made = pipeline and location = overhead
          )
          and layer
    """.toElementFilterExpression() }

    override val changesetComment = "Specify maximum heights"
    override val wikiLink = "Key:maxheight"
    override val icon = R.drawable.ic_quest_max_height
    override val achievements = listOf(CAR)

    override fun getTitle(tags: Map<String, String>): Int {
        val isBelowBridge = tags["amenity"] != "parking_entrance"
            && tags["barrier"] != "height_restrictor"
            && tags["tunnel"] == null
            && tags["covered"] == null
            && tags["man_made"] != "pipeline"
            && tags["railway"] != "level_crossing"
        // only the "below the bridge" situation may need some context
        return when {
            isBelowBridge -> R.string.quest_maxheight_below_bridge_title
            else          -> R.string.quest_maxheight_title
        }
    }

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        // amenity = parking_entrance nodes etc. only if they are a vertex in a road
        val roadsNodeIds = mapData.ways
            .filter { allRoadsFilter.matches(it) }
            .flatMapTo(HashSet()) { it.nodeIds }

        val nodesWithoutHeight = mapData.nodes
            .filter { it.id in roadsNodeIds && nodeFilter.matches(it) }

        // railway crossings with railways that have an electrified contact line
        val electrifiedRailwayNodeIds = mapData.ways
            .filter { electrifiedRailwaysFilter.matches(it) }
            .flatMapTo(HashSet()) { it.nodeIds }

        val railwayCrossingNodesWithoutHeight = mapData.nodes
            .filter { it.id in electrifiedRailwayNodeIds && railwayCrossingsFilter.matches(it) }

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
                val bridgeGeometry = mapData.getWayGeometry(bridge.id) as? ElementPolylinesGeometry
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
        // railway crossing
        if (railwayCrossingsFilter.matches(element)) return null
        return false
    }

    override fun createForm() = AddMaxHeightForm()

    override fun applyAnswerTo(answer: MaxHeightAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is MaxHeight -> {
                tags["maxheight"] = answer.value.toOsmValue()
            }
            is NoMaxHeightSign -> {
                tags["maxheight"] = if (answer.isTallEnough) "default" else "below_default"
            }
        }
    }
}
