package de.westnordost.streetcomplete.quests.clothing_bin_operator

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CITIZEN

class AddClothingBinOperator : OsmElementQuestType<String> {

    /* not the complete filter, see below: we want to filter out additionally all elements that
       contain any recycling:* = yes that is not shoes or clothes but this can not be expressed
       in the elements filter syntax */
    private val filter by lazy { """
        nodes with amenity = recycling and recycling_type = container
         and recycling:clothes = yes
         and !operator and !name and !brand
    """.toElementFilterExpression() }

    override val changesetComment = "Add clothing bin operator"
    override val wikiLink = "Tag:amenity=recycling"
    override val icon = R.drawable.ic_quest_recycling_clothes
    override val isDeleteElementEnabled = true

    override val questTypeAchievements = listOf(CITIZEN)

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.nodes.filter { filter.matches(it) && it.tags.hasNoOtherRecyclingTags() }

    override fun isApplicableTo(element: Element): Boolean =
        filter.matches(element) && element.tags.hasNoOtherRecyclingTags()

    private fun Map<String, String>.hasNoOtherRecyclingTags(): Boolean =
        entries.none { (key, value) ->
            key.startsWith("recycling:")
            && key != "recycling:shoes"
            && key != "recycling:clothes"
            && value == "yes"
        }

    override fun getTitle(tags: Map<String, String>) = R.string.quest_clothes_container_operator_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with amenity = recycling")

    override fun createForm() = AddClothingBinOperatorForm()

    override fun applyAnswerTo(answer: String, tags: Tags, timestampEdited: Long) {
        tags["operator"] = answer
    }
}
