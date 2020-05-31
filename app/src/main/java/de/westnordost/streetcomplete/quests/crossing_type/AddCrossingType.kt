package de.westnordost.streetcomplete.quests.crossing_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquest.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi

class AddCrossingType(o: OverpassMapDataAndGeometryApi) : SimpleOverpassQuestType<String>(o) {

    override val tagFilters = "nodes with highway = crossing and (!crossing or crossing = island) and foot!=no"
    override val commitMessage = "Add crossing type"
    override val wikiLink = "Key:crossing"
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
