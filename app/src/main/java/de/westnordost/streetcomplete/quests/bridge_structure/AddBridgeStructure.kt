package de.westnordost.streetcomplete.quests.bridge_structure

import android.os.Bundle

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao

class AddBridgeStructure(o: OverpassMapDataDao) : SimpleOverpassQuestType(o) {

    override val tagFilters = "ways with man_made=bridge and !bridge:structure"
    override val icon = R.drawable.ic_quest_bridge
    override val commitMessage = "Add bridge structures"

    override fun createForm() = AddBridgeStructureForm()

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        val values = answer.getStringArrayList(AddBridgeStructureForm.OSM_VALUES)
        if (values != null && values.size == 1) {
            changes.add("bridge:structure", values[0])
        }
    }

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bridge_structure_title
}
