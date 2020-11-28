package de.westnordost.streetcomplete.quests.max_height

import de.westnordost.osmapi.map.MapDataWithGeometry
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.ALL_PATHS
import de.westnordost.streetcomplete.data.meta.ALL_ROADS
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.util.intersects

class AddMaxHeight : OsmElementQuestType<MaxHeightAnswer> {

    private val nodeFilter by lazy { """
        nodes with
        (
          barrier = height_restrictor
          or amenity = parking_entrance and parking ~ underground|multi-storey
        )
        and !maxheight and !maxheight:signed and !maxheight:physical
    """.toElementFilterExpression() }

    private val wayFilter by lazy { """
        ways with
        (
          highway ~ motorway|motorway_link|trunk|trunk_link|primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified|residential|living_street|track|road
          or (highway = service and access !~ private|no and vehicle !~ private|no)
        )
        and !maxheight and !maxheight:signed and !maxheight:physical
    """.toElementFilterExpression() }

    private val tunnelFilter by lazy { """
        ways with highway and (covered = yes or tunnel ~ yes|building_passage|avalanche_protector)
    """.toElementFilterExpression() }

    private val bridgeFilter by lazy { """
        ways with (
            highway ~ ${(ALL_ROADS + ALL_PATHS).joinToString("|")}
            or railway ~ rail|light_rail|subway|narrow_gauge|tram|disused|preserved|funicular
          ) and (
            bridge and bridge != no
            or man_made = pipeline and location = overhead
          )
          and layer
    """.toElementFilterExpression() }

    override val commitMessage = "Add maximum heights"
    override val wikiLink = "Key:maxheight"
    override val icon = R.drawable.ic_quest_max_height
    override val isSplitWayEnabled = true
    override val isRecreateAfterSplitEnabled = false

    override fun getTitle(tags: Map<String, String>): Int {
        val isParkingEntrance = tags["amenity"] == "parking_entrance"
        val isHeightRestrictor =  tags["barrier"] == "height_restrictor"
        val isTunnel = tags["tunnel"] == "yes"
        val isBelowBridge =
            !isParkingEntrance && !isHeightRestrictor &&
            tags["tunnel"] == null && tags["covered"] == null &&
            tags["man_made"] != "pipeline"

        return when {
            isParkingEntrance  -> R.string.quest_maxheight_parking_entrance_title
            isHeightRestrictor -> R.string.quest_maxheight_height_restrictor_title
            isTunnel           -> R.string.quest_maxheight_tunnel_title
            isBelowBridge      -> R.string.quest_maxheight_below_bridge_title
            else               -> R.string.quest_maxheight_title
        }
    }

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val bridges = mapData.ways.filter { bridgeFilter.matches(it) }

        val waysWithoutHeight = mapData.ways.filter { wayFilter.matches(it) }

        val nodesWithoutHeight = mapData.nodes.filter { nodeFilter.matches(it) }
        val tunnelsWithoutHeight = waysWithoutHeight.filter { tunnelFilter.matches(it) }

        val waysBelowBridgesWithoutHeight = waysWithoutHeight.filter { way ->
            val layer = way.tags?.get("layer")?.toIntOrNull() ?: 0
            val geometry = mapData.getWayGeometry(way.id) as? ElementPolylinesGeometry

            geometry != null && bridges.any { bridge ->
                val bridgeGeometry = mapData.getWayGeometry(bridge.id) as? ElementPolylinesGeometry
                val bridgeLayer = bridge.tags?.get("layer")?.toIntOrNull() ?: 0

                bridgeGeometry != null && bridgeLayer > layer && bridgeGeometry.intersects(geometry)
            }
        }

        return nodesWithoutHeight + tunnelsWithoutHeight + waysBelowBridgesWithoutHeight
    }


    override fun isApplicableTo(element: Element): Boolean? {
        if (nodeFilter.matches(element)) return true
        if (wayFilter.matches(element)) {
            if (tunnelFilter.matches(element)) return true
            // if it is a way but not a tunnel, we cannot determine whether it is applicable (=
            // below a bridge) just by looking at the tags
            return null
        }
        return false
    }

    override fun createForm() = AddMaxHeightForm()

    override fun applyAnswerTo(answer: MaxHeightAnswer, changes: StringMapChangesBuilder) {
        when(answer) {
            is MaxHeight -> {
                changes.add("maxheight", answer.value.toString())
            }
            is NoMaxHeightSign -> {
                changes.add("maxheight", if (answer.isTallEnough) "default" else "below_default")
            }
        }
    }
}
