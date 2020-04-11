package de.westnordost.streetcomplete.quests.ferry

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment



class AddFerryAccessMotorVehicle(o: OverpassMapDataAndGeometryDao) : SimpleOverpassQuestType<Boolean>(o) {

    override val tagFilters = "ways, relations with route = ferry and !motor_vehicle"
    override val commitMessage = "Specify ferry access for motor vehicles"
    override val icon = R.drawable.ic_quest_ferry
    override val hasMarkersAtEnds = true

    override fun getTitle(tags: Map<String, String>): Int {
        val hasName = tags.containsKey("name")
        return if (hasName)
            R.string.quest_ferry_motor_vehicle_name_title
        else
            R.string.quest_ferry_motor_vehicle_title
    }

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        changes.add("motor_vehicle", if (answer) "yes" else "no")
    }
}
