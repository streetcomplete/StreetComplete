package de.westnordost.streetcomplete.quests.bridge_structure

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao

class AddBridgeStructure(o: OverpassMapDataAndGeometryDao) : SimpleOverpassQuestType<String>(o) {

    override val tagFilters = "ways with man_made = bridge and !bridge:structure and !bridge:movable"
    override val icon = R.drawable.ic_quest_bridge
    override val commitMessage = "Add bridge structures"

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bridge_structure_title

    override fun createForm() = AddBridgeStructureForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.add("bridge:structure", answer)
    }
}
