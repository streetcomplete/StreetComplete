package de.westnordost.streetcomplete.quests.building_levels

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.BUILDING

class AddBuildingLevels : OsmFilterQuestType<BuildingLevelsAnswer>() {

    override val elementFilter = """
        ways, relations with building ~ ${BUILDINGS_WITH_LEVELS.joinToString("|")}
         and !building:levels and !man_made and location != underground and ruins != yes
    """
    override val commitMessage = "Add building and roof levels"
    override val wikiLink = "Key:building:levels"
    override val icon = R.drawable.ic_quest_building_levels

    override val questTypeAchievements = listOf(BUILDING)

    override fun getTitle(tags: Map<String, String>) =
        if (tags.containsKey("building:part"))
            R.string.quest_buildingLevels_title_buildingPart2
        else
            R.string.quest_buildingLevels_title2

    override fun createForm() = AddBuildingLevelsForm()

    override fun applyAnswerTo(answer: BuildingLevelsAnswer, changes: StringMapChangesBuilder) {
        changes.add("building:levels", answer.levels.toString())
        answer.roofLevels?.let { changes.addOrModify("roof:levels", it.toString()) }
    }
}

private val BUILDINGS_WITH_LEVELS = arrayOf(
    "house","residential","apartments","detached","terrace","dormitory","semi",
    "semidetached_house","bungalow","school","civic","college","university","public",
    "hospital","kindergarten","transportation","train_station", "hotel","retail",
    "commercial","office","manufacture","parking","farm","farm_auxiliary",
    "cabin")
