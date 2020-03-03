package de.westnordost.streetcomplete.quests.car_wash_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao
import de.westnordost.streetcomplete.quests.car_wash_type.CarWashType.*

class AddCarWashType(o: OverpassMapDataAndGeometryDao) : SimpleOverpassQuestType<List<CarWashType>>(o) {

    override val tagFilters = "nodes, ways with amenity = car_wash and !automated and !self_service"
    override val commitMessage = "Add car wash type"
    override val icon = R.drawable.ic_quest_car_wash

    override fun getTitle(tags: Map<String, String>) = R.string.quest_carWashType_title

    override fun createForm() = AddCarWashTypeForm()

    override fun applyAnswerTo(answer: List<CarWashType>, changes: StringMapChangesBuilder) {
        val isAutomated = answer.contains(AUTOMATED)
        changes.add("automated", if (isAutomated) "yes" else "no")

        val hasSelfService = answer.contains(SELF_SERVICE)
        val selfService = when {
            hasSelfService && answer.size == 1 -> "only"
            hasSelfService -> "yes"
            else -> "no"
        }
        changes.add("self_service", selfService)
    }
}
