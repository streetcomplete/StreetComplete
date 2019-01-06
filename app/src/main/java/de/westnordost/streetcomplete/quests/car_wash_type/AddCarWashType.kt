package de.westnordost.streetcomplete.quests.car_wash_type

import android.os.Bundle

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment

class AddCarWashType(o: OverpassMapDataDao) : SimpleOverpassQuestType(o) {

    override val tagFilters = "nodes, ways with amenity=car_wash and !automated and !self_service"
    override val commitMessage = "Add car wash type"
    override val icon = R.drawable.ic_quest_car_wash

    override fun getTitle(tags: Map<String, String>) = R.string.quest_carWashType_title

    override fun createForm() = AddCarWashTypeForm()

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        val values = answer.getStringArrayList(ImageListQuestAnswerFragment.OSM_VALUES)!!

        val isAutomated = values.contains(AddCarWashTypeForm.AUTOMATED)
        val isSelfService = values.contains(AddCarWashTypeForm.SELF_SERVICE)
        val isStaffService = values.contains(AddCarWashTypeForm.SERVICE)

        changes.add("automated", if (isAutomated) "yes" else "no")
        changes.add("self_service", if (isSelfService && !isStaffService) "yes" else "no")
    }
}
