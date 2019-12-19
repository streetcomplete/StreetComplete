package de.westnordost.streetcomplete.quests.bike_parking_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao

class AddBikeParkingType(o: OverpassMapDataAndGeometryDao) : SimpleOverpassQuestType<String>(o) {

    override val tagFilters = "nodes, ways with amenity = bicycle_parking and access !~ private|no and !bicycle_parking"
    override val commitMessage = "Add bicycle parking type"
    override val icon = R.drawable.ic_quest_bicycle_parking

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bicycle_parking_type_title

    override fun createForm() = AddBikeParkingTypeForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.add("bicycle_parking", answer)
    }
}
