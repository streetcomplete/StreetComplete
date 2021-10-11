package de.westnordost.streetcomplete.quests.bike_parking_capacity

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.BICYCLIST

class AddBikeParkingCapacity : OsmFilterQuestType<Int>() {

    override val elementFilter = """
        nodes, ways with amenity = bicycle_parking
         and access !~ private|no
         and bicycle_parking !~ floor
         and (
           !capacity
           or bicycle_parking ~ stands|wall_loops and capacity older today -4 years
         )
    """
    /* Bike capacity may change more often for stands and wheelbenders as adding or
       removing a few of them is minor work
     */

    override val commitMessage = "Add bicycle parking capacities"
    override val wikiLink = "Tag:amenity=bicycle_parking"
    override val icon = R.drawable.ic_quest_bicycle_parking_capacity
    override val isDeleteElementEnabled = true

    override val questTypeAchievements = listOf(BICYCLIST)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bikeParkingCapacity_title

    override fun createForm() = AddBikeParkingCapacityForm()

    override fun applyAnswerTo(answer: Int, changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate("capacity", answer.toString())
    }
}
