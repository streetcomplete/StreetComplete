package de.westnordost.streetcomplete.quests.clothing_bin_operator

import de.westnordost.osmapi.map.MapDataWithGeometry
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType

class AddClothingBinOperator : OsmElementQuestType<String> {

    /* not the complete filter, see below: we want to filter out additionally all elements that
       contain any recycling:* = yes that is not shoes or clothes but this can not be expressed
       in the elements filter syntax */
    private val filter by lazy { """
        nodes with amenity = recycling and recycling_type = container 
         and recycling:clothes = yes 
         and !operator
    """.toElementFilterExpression() }

    override val commitMessage = "Add clothing bin operator"
    override val wikiLink = "Tag:amenity=recycling"
    override val icon = R.drawable.ic_quest_recycling_clothes

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.nodes.filter { filter.matches(it) && it.tags.hasNoOtherRecyclingTags() }

    override fun isApplicableTo(element: Element): Boolean =
        filter.matches(element) && element.tags.hasNoOtherRecyclingTags()

    private fun Map<String, String>.hasNoOtherRecyclingTags(): Boolean {
        return entries.find {
            it.key.startsWith("recycling:")
            it.key != "recycling:shoes" &&
            it.key != "recycling:clothes" &&
            it.value == "yes"
        } == null
    }

    override fun getTitle(tags: Map<String, String>) = R.string.quest_clothes_container_operator_title

    override fun createForm() = AddClothingBinOperatorForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.add("operator", answer)
    }
}


