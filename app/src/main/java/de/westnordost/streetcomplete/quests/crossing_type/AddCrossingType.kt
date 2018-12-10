package de.westnordost.streetcomplete.quests.crossing_type

import android.os.Bundle

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao

class AddCrossingType(o: OverpassMapDataDao) : SimpleOverpassQuestType(o) {

    override val tagFilters = "nodes with highway=crossing and !crossing"
    override val commitMessage = "Add crossing type"
    override val icon = R.drawable.ic_quest_pedestrian_crossing

	override fun getTitle(tags: Map<String, String>) = R.string.quest_crossing_type_title

	override fun createForm() = AddCrossingTypeForm()

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        val values = answer.getStringArrayList(AddCrossingTypeForm.OSM_VALUES)
        if (values != null && values.size == 1) {
            changes.add("crossing", values[0])
        }
    }
}
