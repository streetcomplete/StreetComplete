package de.westnordost.streetcomplete.quests.max_height

import de.westnordost.osmapi.map.MapDataWithGeometry
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType

class AddMaxHeight : OsmElementQuestType<MaxHeightAnswer> {

    private val nodeFilter by lazy { """
        nodes with
        (
          barrier = height_restrictor
          or amenity = parking_entrance and parking ~ underground|multi-storey
        )
        and !maxheight and !maxheight:physical
    """.toElementFilterExpression()}

    private val wayFilter by lazy { """
        ways with
        (
          highway ~ motorway|motorway_link|trunk|trunk_link|primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified|residential|living_street|track|road
          or (highway = service and access !~ private|no and vehicle !~ private|no)
        )
        and (covered = yes or tunnel ~ yes|building_passage|avalanche_protector)
        and !maxheight and !maxheight:physical
    """.toElementFilterExpression()}

    override val commitMessage = "Add maximum heights"
    override val wikiLink = "Key:maxheight"
    override val icon = R.drawable.ic_quest_max_height

    override fun getTitle(tags: Map<String, String>): Int {
        val isParkingEntrance = tags["amenity"] == "parking_entrance"
        val isHeightRestrictor =  tags["barrier"] == "height_restrictor"
        val isTunnel = tags["tunnel"] == "yes"

        return when {
            isParkingEntrance  -> R.string.quest_maxheight_parking_entrance_title
            isHeightRestrictor -> R.string.quest_maxheight_height_restrictor_title
            isTunnel           -> R.string.quest_maxheight_tunnel_title
            else               -> R.string.quest_maxheight_title
        }
    }

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.nodes.filter { nodeFilter.matches(it) } + mapData.ways.filter { wayFilter.matches(it) }

    override fun isApplicableTo(element: Element) =
        nodeFilter.matches(element) || wayFilter.matches(element)

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
