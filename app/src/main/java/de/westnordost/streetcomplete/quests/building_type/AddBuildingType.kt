package de.westnordost.streetcomplete.quests.building_type

import android.os.Bundle

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao

class AddBuildingType (o: OverpassMapDataDao) : SimpleOverpassQuestType(o) {

    // in the case of man_made, historic, military and power, these tags already contain
    // information about the purpose of the building, so no need to force asking it
    override val tagFilters = """
        ways, relations with building=yes
        and !man_made and !historic and !military and !power and location!=underground"
    """
    override val commitMessage = "Add building types"
    override val icon = R.drawable.ic_quest_building

    override fun getTitle(tags: Map<String, String>) = R.string.quest_buildingType_title2

    override fun createForm() = AddBuildingTypeForm()

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        val building = answer.getString(AddBuildingTypeForm.BUILDING)
        val man_made = answer.getString(AddBuildingTypeForm.MAN_MADE)
        if (man_made != null) {
            changes.delete("building")
            changes.add("man_made", man_made)
        } else {
            changes.modify("building", building!!)
        }
    }
}
