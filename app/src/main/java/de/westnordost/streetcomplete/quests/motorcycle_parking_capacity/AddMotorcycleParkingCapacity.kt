package de.westnordost.streetcomplete.quests.motorcycle_parking_capacity

import android.os.Bundle

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.TextInputQuestAnswerFragment

class AddMotorcycleParkingCapacity(o: OverpassMapDataDao) : SimpleOverpassQuestType(o) {

    override val tagFilters = "nodes, ways with amenity=motorcycle_parking and !capacity and access !~ private|no"
    override val commitMessage = "Add motorcycle parking capacities"
    override val icon = R.drawable.ic_quest_motorcycle_parking_capacity

    override fun getTitle(tags: Map<String, String>) = R.string.quest_motorcycleParkingCapacity_title

    override fun createForm() = AddMotorcycleParkingCapacityForm()

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        changes.add("capacity","" + answer.getString(TextInputQuestAnswerFragment.INPUT).toInt())
    }
}
