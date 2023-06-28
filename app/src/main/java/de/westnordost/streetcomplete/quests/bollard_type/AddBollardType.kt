package de.westnordost.streetcomplete.quests.bollard_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.LIFESAVER
import de.westnordost.streetcomplete.osm.Tags

class AddBollardType : OsmElementQuestType<BollardTypeAnswer> {

    private val bollardNodeFilter by lazy { """
        nodes with
          barrier = bollard
          and !bollard
    """.toElementFilterExpression() }

    private val waysFilter by lazy { """
        ways with
          highway and highway != construction
          and area != yes
    """.toElementFilterExpression() }

    override val changesetComment = "Specify bollard types"
    override val wikiLink = "Key:bollard"
    override val icon = R.drawable.ic_quest_no_cars
    override val isDeleteElementEnabled = true
    override val achievements = listOf(CAR, LIFESAVER)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bollard_type_title

    // exclude free-floating nodes
    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val wayNodeIds = mapData.ways
            .filter { waysFilter.matches(it) }
            .flatMapTo(HashSet()) { it.nodeIds }

        return mapData.nodes
            .filter { bollardNodeFilter.matches(it) && it.id in wayNodeIds }
    }

    override fun isApplicableTo(element: Element): Boolean? =
        if (!bollardNodeFilter.matches(element)) false else null

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with barrier = bollard")

    override fun createForm() = AddBollardTypeForm()

    override fun applyAnswerTo(answer: BollardTypeAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is BollardType -> tags["bollard"] = answer.osmValue
            BarrierTypeIsNotBollard -> tags["barrier"] = "yes"
        }
    }
}
