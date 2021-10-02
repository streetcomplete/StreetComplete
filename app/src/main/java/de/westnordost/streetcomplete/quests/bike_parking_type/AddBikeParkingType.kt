package de.westnordost.streetcomplete.quests.bike_parking_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.BICYCLIST

class AddBikeParkingType : OsmFilterQuestType<BikeParkingType>() {

    override val elementFilter = """
        nodes, ways with
          amenity = bicycle_parking
          and access !~ private|no
          and !bicycle_parking
    """
    override val commitMessage = "Add bicycle parking type"
    override val wikiLink = "Key:bicycle_parking"
    override val icon = R.drawable.ic_quest_bicycle_parking
    override val isDeleteElementEnabled = true

    override val questTypeAchievements = listOf(BICYCLIST)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bicycle_parking_type_title

    override fun createForm() = AddBikeParkingTypeForm()

    override fun applyAnswerTo(answer: BikeParkingType, changes: StringMapChangesBuilder) {
        changes.add("bicycle_parking", answer.osmValue)
    }
}
