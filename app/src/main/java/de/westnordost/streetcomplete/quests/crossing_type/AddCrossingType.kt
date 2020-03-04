package de.westnordost.streetcomplete.quests.crossing_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao

class AddCrossingType(o: OverpassMapDataAndGeometryDao) : SimpleOverpassQuestType<String>(o) {

    override val tagFilters = "nodes with highway = crossing and (!crossing or crossing = island)"
    override val commitMessage = "Add crossing type"
    override val icon = R.drawable.ic_quest_pedestrian_crossing

    override fun getTitle(tags: Map<String, String>) = R.string.quest_crossing_type_title

    override fun createForm() = AddCrossingTypeForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        if(changes.getPreviousValue("crossing") == "island") {
            changes.modify("crossing", answer)
            changes.addOrModify("crossing:island", "yes")
        } else {
            changes.add("crossing", answer)
        }
    }
}
