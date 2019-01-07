package de.westnordost.streetcomplete.quests.bike_parking_capacity

import android.os.Bundle

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.TextInputQuestAnswerFragment

class AddBikeParkingCapacity(o: OverpassMapDataDao) : SimpleOverpassQuestType(o) {

    override val tagFilters = "nodes, ways with amenity=bicycle_parking and !capacity and access !~ private|no"
    override val commitMessage = "Add bicycle parking capacities"
    override val icon = R.drawable.ic_quest_bicycle_parking_capacity

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bikeParkingCapacity_title

    override fun createForm() = AddBikeParkingCapacityForm()

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        changes.add("capacity", "" + answer.getString(TextInputQuestAnswerFragment.INPUT).toInt())
    }
}
