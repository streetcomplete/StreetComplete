package de.westnordost.streetcomplete.quests.building_entrance

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Relation
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.osm.Tags

class AddEntrance : OsmElementQuestType<EntranceAnswer> {

    private val withoutEntranceFilter by lazy { """
        nodes with
          !entrance and !barrier and noexit != yes and !railway
    """.toElementFilterExpression() }

    private val buildingFilter by lazy { """
        ways, relations with
          building and building !~ yes|no|service|shed|house|detached|terrace|semi|semidetached_house|roof|carport|construction
          and location != underground
          and (layer !~ -[0-9]+ or location)
    """.toElementFilterExpression() }

    private val incomingWaysFilter by lazy { """
        ways with
          highway ~ path|footway|steps|cycleway and area != yes and access !~ private|no
    """.toElementFilterExpression() }

    private val excludedWaysFilter by lazy { """
        ways with (tunnel and tunnel != no) or (covered and covered != no) or location ~ roof|rooftop
    """.toElementFilterExpression() }

    override val changesetComment = "Specify type of entrances"
    override val wikiLink = "Key:entrance"
    override val icon = R.drawable.ic_quest_door
    override val achievements = listOf(PEDESTRIAN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_building_entrance_title

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val buildingsWayNodeIds = mutableSetOf<Long>()
        mapData
            .filter { buildingFilter.matches(it) }
            .flatMapTo(buildingsWayNodeIds) {
                when (it) {
                    is Way -> it.nodeIds
                    is Relation -> it.getMultipolygonNodeIds(mapData)
                    else -> emptyList()
                }
            }

        val incomingWayNodeIds = mutableSetOf<Long>()
        mapData.ways
            .filter { incomingWaysFilter.matches(it) }
            .flatMapTo(incomingWayNodeIds) { it.nodeIds }

        val excludedWayNodeIds = mutableSetOf<Long>()
        mapData.ways
            .filter { excludedWaysFilter.matches(it) }
            .flatMapTo(excludedWayNodeIds) { it.nodeIds }

        return mapData.nodes
            .filter { it.id in buildingsWayNodeIds && it.id in incomingWayNodeIds
                && it.id !in excludedWayNodeIds
                && withoutEntranceFilter.matches(it)
            }
    }

    override fun isApplicableTo(element: Element): Boolean? =
        if (!withoutEntranceFilter.matches(element)) false else null

    override fun createForm() = AddEntranceForm()

    override fun applyAnswerTo(answer: EntranceAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            DeadEnd -> tags["noexit"] = "yes"
            is EntranceExistsAnswer -> tags["entrance"] = answer.osmValue
        }
    }
}

private fun Relation.getMultipolygonNodeIds(mapData: MapDataWithGeometry): List<Long> {
    if (tags["type"] != "multipolygon") return emptyList()
    val nodeIds = mutableListOf<Long>()
    for (member in members) {
        if (member.type != ElementType.WAY) continue
        val wayNodeIds = mapData.getWay(member.ref)?.nodeIds
        if (wayNodeIds != null) {
            nodeIds.addAll(wayNodeIds)
        }
    }
    return nodeIds
}
