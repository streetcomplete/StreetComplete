package de.westnordost.streetcomplete.quests.bridge_structure

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.BUILDING

class AddBridgeStructure : OsmFilterQuestType<BridgeStructure>() {

    override val elementFilter = "ways with man_made = bridge and !bridge:structure and !bridge:movable"
    override val commitMessage = "Add bridge structures"
    override val wikiLink = "Key:bridge:structure"
    override val icon = R.drawable.ic_quest_bridge

    override val questTypeAchievements = listOf(BUILDING)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bridge_structure_title

    override fun createForm() = AddBridgeStructureForm()

    override fun applyAnswerTo(answer: BridgeStructure, changes: StringMapChangesBuilder) {
        changes.add("bridge:structure", answer.osmValue)
    }
}
