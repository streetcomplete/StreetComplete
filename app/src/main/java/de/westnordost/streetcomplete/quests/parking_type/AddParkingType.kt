package de.westnordost.streetcomplete.quests.parking_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CAR

class AddParkingType : OsmFilterQuestType<ParkingType>() {

    override val elementFilter = """
        nodes, ways, relations with
          amenity = parking
          and (!parking or parking = yes)
    """
    override val commitMessage = "Add parking type"
    override val wikiLink = "Tag:amenity=parking"
    override val icon = R.drawable.ic_quest_parking

    override val questTypeAchievements = listOf(CAR)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_parkingType_title

    override fun createForm() = AddParkingTypeForm()

    override fun applyAnswerTo(answer: ParkingType, changes: StringMapChangesBuilder) {
        changes.addOrModify("parking", answer.osmValue)
    }
}
