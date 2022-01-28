package de.westnordost.streetcomplete.quests.bollard_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CAR
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.LIFESAVER

class AddBollardType : OsmElementQuestType<BollardType> {

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

    override val changesetComment = "Add bollard type"
    override val wikiLink = "Key:bollard"
    override val icon = R.drawable.ic_quest_no_cars
    override val isDeleteElementEnabled = true

    override val questTypeAchievements = listOf(CAR, LIFESAVER)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bollard_type_title

    // exclude free-floating nodes
    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val wayNodeIds = mutableSetOf<Long>()
        mapData.ways
            .filter { waysFilter.matches(it) }
            .flatMapTo(wayNodeIds) { it.nodeIds }

        return mapData.nodes
            .filter { bollardNodeFilter.matches(it) && it.id in wayNodeIds }
    }

    override fun isApplicableTo(element: Element): Boolean? =
        if (!bollardNodeFilter.matches(element)) false else null

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with barrier = bollard")

    override fun createForm() = AddBollardTypeForm()

    override fun applyAnswerTo(answer: BollardType, tags: Tags, timestampEdited: Long) {
        tags["bollard"] = answer.osmValue
    }
}
