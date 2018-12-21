package de.westnordost.streetcomplete.quests.parking_access

import android.os.Bundle

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao

class AddParkingAccess(o: OverpassMapDataDao) : SimpleOverpassQuestType(o) {

    override val tagFilters = "nodes, ways, relations with amenity=parking and (!access or access=unknown)"
    override val commitMessage = "Add type of parking access"
    override val icon = R.drawable.ic_quest_parking_access

    override fun getTitle(tags: Map<String, String>) = R.string.quest_parking_access_title

    override fun createForm() = AddParkingAccessForm()

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        changes.addOrModify("access", answer.getString(AddParkingAccessForm.ACCESS)!!)
    }
}
