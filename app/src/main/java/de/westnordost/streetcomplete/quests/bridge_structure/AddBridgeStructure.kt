package de.westnordost.streetcomplete.quests.bridge_structure

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BUILDING
import de.westnordost.streetcomplete.osm.Tags

class AddBridgeStructure : OsmFilterQuestType<BridgeStructure>() {

    override val elementFilter = """
        ways with
          man_made = bridge
          and !bridge:structure
          and !bridge:movable
          and (!indoor or indoor = no)
    """
    override val changesetComment = "Specify bridge structures"
    override val wikiLink = "Key:bridge:structure"
    override val icon = R.drawable.ic_quest_bridge
    override val achievements = listOf(BUILDING)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bridge_structure_title

    override fun createForm() = AddBridgeStructureForm()

    override fun applyAnswerTo(answer: BridgeStructure, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["bridge:structure"] = answer.osmValue
    }
}
