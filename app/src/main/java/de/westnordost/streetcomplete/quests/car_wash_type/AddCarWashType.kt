package de.westnordost.streetcomplete.quests.car_wash_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao

class AddCarWashType(o: OverpassMapDataDao) : SimpleOverpassQuestType<CarWashType>(o) {

    override val tagFilters = "nodes, ways with amenity=car_wash and !automated and !self_service"
    override val commitMessage = "Add car wash type"
    override val icon = R.drawable.ic_quest_car_wash

    override fun getTitle(tags: Map<String, String>) = R.string.quest_carWashType_title

    override fun createForm() = AddCarWashTypeForm()

    override fun applyAnswerTo(answer: CarWashType, changes: StringMapChangesBuilder) {
        changes.add("automated", if (answer.isAutomated) "yes" else "no")
        changes.add("self_service", if (answer.isSelfService) "yes" else "no")
    }
}
