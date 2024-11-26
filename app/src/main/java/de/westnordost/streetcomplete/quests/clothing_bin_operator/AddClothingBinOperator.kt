package de.westnordost.streetcomplete.quests.clothing_bin_operator

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags

class AddClothingBinOperator : OsmElementQuestType<ClothingBinOperatorAnswer> {

    /* not the complete filter, see below: we want to filter out additionally all elements that
       contain any recycling:* = yes that is not shoes or clothes but this can not be expressed
       in the elements filter syntax */
    private val filter by lazy { """
        nodes with amenity = recycling and recycling_type = container
         and recycling:clothes = yes
         and !operator and !name and !brand
         and operator:signed != no
         and brand:signed != no
    """.toElementFilterExpression() }

    override val changesetComment = "Specify clothing bin operators"
    override val wikiLink = "Tag:amenity=recycling"
    override val icon = R.drawable.ic_quest_recycling_clothes
    override val isDeleteElementEnabled = true
    override val achievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_clothes_container_operator_title

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

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with amenity = recycling")

    override fun createForm() = AddClothingBinOperatorForm()

    override fun applyAnswerTo(answer: ClothingBinOperatorAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is ClothingBinOperator -> {
                tags["operator"] = answer.name
            }
            is NoClothingBinOperatorSigned -> {
                tags["operator:signed"] = "no"
            }
        }
    }
}
