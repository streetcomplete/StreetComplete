package de.westnordost.streetcomplete.quests.bike_parking_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder

class AddBikeParkingType : OsmFilterQuestType<String>() {

    override val elementFilter = "nodes, ways with amenity = bicycle_parking and access !~ private|no and !bicycle_parking"
    override val commitMessage = "Add bicycle parking type"
    override val wikiLink = "Key:bicycle_parking"
    override val icon = R.drawable.ic_quest_bicycle_parking

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bicycle_parking_type_title

    override fun createForm() = AddBikeParkingTypeForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.add("bicycle_parking", answer)
    }
}
