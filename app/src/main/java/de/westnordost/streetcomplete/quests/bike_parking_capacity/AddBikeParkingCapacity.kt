package de.westnordost.streetcomplete.quests.bike_parking_capacity

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao

class AddBikeParkingCapacity(o: OverpassMapDataAndGeometryDao) : SimpleOverpassQuestType<Int>(o) {

    override val tagFilters = "nodes, ways with amenity = bicycle_parking and !capacity and access !~ private|no"
    override val commitMessage = "Add bicycle parking capacities"
    override val icon = R.drawable.ic_quest_bicycle_parking_capacity

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bikeParkingCapacity_title

    override fun createForm() = AddBikeParkingCapacityForm()

    override fun applyAnswerTo(answer: Int, changes: StringMapChangesBuilder) {
        changes.add("capacity", answer.toString())
    }
}
