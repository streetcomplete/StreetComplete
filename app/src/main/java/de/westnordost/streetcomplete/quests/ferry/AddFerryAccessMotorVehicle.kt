package de.westnordost.streetcomplete.quests.ferry

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CAR
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.RARE
import de.westnordost.streetcomplete.ktx.toYesNo
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddFerryAccessMotorVehicle : OsmFilterQuestType<Boolean>() {

    override val elementFilter = "ways, relations with route = ferry and !motor_vehicle"
    override val changesetComment = "Specify ferry access for motor vehicles"
    override val wikiLink = "Tag:route=ferry"
    override val icon = R.drawable.ic_quest_ferry
    override val hasMarkersAtEnds = true
    override val questTypeAchievements = listOf(RARE, CAR)

    override fun getTitle(tags: Map<String, String>): Int =
        if (tags.containsKey("name"))
            R.string.quest_ferry_motor_vehicle_name_title
        else
            R.string.quest_ferry_motor_vehicle_title

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) {
        tags["motor_vehicle"] = answer.toYesNo()
    }
}
