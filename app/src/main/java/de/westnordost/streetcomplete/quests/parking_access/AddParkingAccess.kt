package de.westnordost.streetcomplete.quests.parking_access

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder

class AddParkingAccess : OsmFilterQuestType<String>() {

    override val elementFilter = "nodes, ways, relations with amenity=parking and (!access or access=unknown)"
    override val commitMessage = "Add type of parking access"
    override val wikiLink = "Tag:amenity=parking"
    override val icon = R.drawable.ic_quest_parking_access

    override fun getTitle(tags: Map<String, String>) = R.string.quest_parking_access_title

    override fun createForm() = AddParkingAccessForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.addOrModify("access", answer)
    }
}
