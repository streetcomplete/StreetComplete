package de.westnordost.streetcomplete.quests.building_underground

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.BUILDING
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddIsBuildingUnderground : OsmFilterQuestType<Boolean>() {

    override val elementFilter = "ways, relations with building and !location and layer~-[0-9]+"
    override val commitMessage = "Determine whether building is fully underground"
    override val wikiLink = "Key:location"
    override val icon = R.drawable.ic_quest_building_underground

    override val questTypeAchievements = listOf(BUILDING)

    override fun getTitle(tags: Map<String, String>): Int =
        if (tags.containsKey("name"))
            R.string.quest_building_underground_name_title
        else
            R.string.quest_building_underground_title

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        changes.add("location", if (answer) "underground" else "surface")
    }
}
