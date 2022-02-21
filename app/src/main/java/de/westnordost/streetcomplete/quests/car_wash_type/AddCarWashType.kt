package de.westnordost.streetcomplete.quests.car_wash_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CAR
import de.westnordost.streetcomplete.ktx.toYesNo
import de.westnordost.streetcomplete.quests.car_wash_type.CarWashType.AUTOMATED
import de.westnordost.streetcomplete.quests.car_wash_type.CarWashType.SELF_SERVICE

class AddCarWashType : OsmFilterQuestType<List<CarWashType>>() {

    override val elementFilter = "nodes, ways with amenity = car_wash and !automated and !self_service"
    override val changesetComment = "Add car wash type"
    override val wikiLink = "Tag:amenity=car_wash"
    override val icon = R.drawable.ic_quest_car_wash

    override val questTypeAchievements = listOf(CAR)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_carWashType_title

    override fun createForm() = AddCarWashTypeForm()

    override fun applyAnswerTo(answer: List<CarWashType>, tags: Tags, timestampEdited: Long) {
        val isAutomated = answer.contains(AUTOMATED)
        tags["automated"] = isAutomated.toYesNo()

        val hasSelfService = answer.contains(SELF_SERVICE)
        val selfService = when {
            hasSelfService && answer.size == 1 -> "only"
            hasSelfService -> "yes"
            else -> "no"
        }
        tags["self_service"] = selfService
    }
}
