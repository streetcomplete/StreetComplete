package de.westnordost.streetcomplete.quests.parking_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao

class AddParkingType(o: OverpassMapDataAndGeometryDao) : SimpleOverpassQuestType<String>(o) {

    override val tagFilters = "nodes, ways, relations with amenity = parking and !parking"
    override val commitMessage = "Add parking type"
    override val icon = R.drawable.ic_quest_parking

    override fun getTitle(tags: Map<String, String>) = R.string.quest_parkingType_title

    override fun createForm() = AddParkingTypeForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.add("parking", answer)
    }
}
