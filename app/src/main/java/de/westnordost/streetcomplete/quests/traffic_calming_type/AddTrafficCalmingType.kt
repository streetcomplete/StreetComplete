package de.westnordost.streetcomplete.quests.traffic_calming_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.osm.Tags

class AddTrafficCalmingType : OsmFilterQuestType<TrafficCalmingType>() {

    override val elementFilter = "nodes with traffic_calming = yes"
    override val changesetComment = "Specify traffic calming types"
    override val wikiLink = "Key:traffic_calming"
    override val icon = R.drawable.ic_quest_car_bumpy
    override val isDeleteElementEnabled = true
    override val achievements = listOf(PEDESTRIAN, CAR)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_traffic_calming_type_title

    override fun createForm() = AddTrafficCalmingTypeForm()

    override fun applyAnswerTo(answer: TrafficCalmingType, tags: Tags, timestampEdited: Long) {
        tags["traffic_calming"] = answer.osmValue
    }
}
