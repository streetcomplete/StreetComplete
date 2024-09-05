package de.westnordost.streetcomplete.quests.tree

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags

class AddTreeGenus : OsmFilterQuestType<TreeAnswer>() {

    override val elementFilter = """
        nodes with
          natural = tree
          and !genus and !species and !taxon
          and !~"genus:.*" and !~"species:.*" and !~"taxon:.*"
    """
    override val changesetComment = "Add tree genus/species"
    override val defaultDisabledMessage = R.string.quest_tree_disabled_msg
    override val wikiLink = "Key:genus"
    override val icon = R.drawable.ic_quest_tree

    override fun getTitle(tags: Map<String, String>) = R.string.quest_tree_genus_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with natural = tree")

    override fun createForm() = AddTreeGenusForm()

    override val isDeleteElementEnabled = true

    override fun applyAnswerTo(answer: TreeAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is NotTreeButStump -> tags["natural"] = "tree_stump"
            is Tree -> {
                if (answer.isSpecies)
                    tags["species"] = answer.name
                else
                    tags["genus"] = answer.name
            }
        }
    }
}
