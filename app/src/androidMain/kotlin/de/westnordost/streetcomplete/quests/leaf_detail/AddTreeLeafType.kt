package de.westnordost.streetcomplete.quests.leaf_detail

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags

class AddTreeLeafType : OsmFilterQuestType<TreeLeafTypeAnswer>() {
    override val elementFilter = """
        nodes with
          natural = tree
          and !leaf_type
          and !~"(taxon|genus|species).*"
    """
    override val changesetComment = "Specify leaf types"
    override val wikiLink = "Key:leaf_type"
    override val icon = R.drawable.ic_quest_leaf
    override val isDeleteElementEnabled = true
    override val achievements = listOf(OUTDOORS)
    override val defaultDisabledMessage = R.string.default_disabled_msg_difficult_and_time_consuming

    override fun getTitle(tags: Map<String, String>) = R.string.quest_leafType_tree_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with natural = tree".toElementFilterExpression())

    override fun createForm() = AddTreeLeafTypeForm()

    override fun applyAnswerTo(answer: TreeLeafTypeAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is TreeLeafType -> tags["leaf_type"] = answer.osmValue
            NotTreeButStump -> tags["natural"] = "tree_stump"
        }
    }
}
