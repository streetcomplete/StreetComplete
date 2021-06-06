package de.westnordost.streetcomplete.quests.car_wash_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.ktx.toYesNo
import de.westnordost.streetcomplete.quests.car_wash_type.CarWashType.*

class AddCarWashType : OsmFilterQuestType<List<CarWashType>>() {

    override val elementFilter = "nodes, ways with amenity = car_wash and !automated and !self_service"
    override val commitMessage = "Add car wash type"
    override val wikiLink = "Tag:amenity=car_wash"
    override val icon = R.drawable.ic_quest_car_wash

    override fun getTitle(tags: Map<String, String>) = R.string.quest_carWashType_title

    override fun createForm() = AddCarWashTypeForm()

    override fun applyAnswerTo(answer: List<CarWashType>, changes: StringMapChangesBuilder) {
        val isAutomated = answer.contains(AUTOMATED)
        changes.add("automated", isAutomated.toYesNo())

        val hasSelfService = answer.contains(SELF_SERVICE)
        val selfService = when {
            hasSelfService && answer.size == 1 -> "only"
            hasSelfService -> "yes"
            else -> "no"
        }
        changes.add("self_service", selfService)
    }
}
