package de.westnordost.streetcomplete.quests.parking_access

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.BICYCLIST

class AddBikeParkingAccess : OsmFilterQuestType<ParkingAccess>() {

    // Only include these bicycle_parking types, because access for these types is needed for
    // AddBikeParkingFee and because those are uncontroversial. See #2496 and #2517
    override val elementFilter = """
        nodes, ways, relations with amenity = bicycle_parking
        and bicycle_parking ~ building|lockers|shed
        and (!access or access = unknown)
    """

    override val commitMessage = "Add type of bike parking access"
    override val wikiLink = "Tag:amenity=bicycle_parking"
    override val icon = R.drawable.ic_quest_bicycle_parking_access

    override val questTypeAchievements = listOf(BICYCLIST)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bicycle_parking_access_title

    override fun createForm() = AddParkingAccessForm()

    override fun applyAnswerTo(answer: ParkingAccess, changes: StringMapChangesBuilder) {
        changes.addOrModify("access", answer.osmValue)
    }
}
