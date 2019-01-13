package de.westnordost.streetcomplete.quests.building_levels

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao

class AddBuildingLevels(o: OverpassMapDataDao) : SimpleOverpassQuestType<BuildingLevelsAnswer>(o) {

    // building:height is undocumented, but used the same way as height and currently over 50k times
    override val tagFilters =
        "ways, relations with " +
        " building ~ " + arrayOf(
            "house","residential","apartments","detached","terrace","dormitory","semi",
            "semidetached_house","bungalow","school","civic","college","university","public",
            "hospital","kindergarten","transportation","train_station", "hotel","retail",
            "commercial","office","warehouse","industrial","manufacture","parking","farm",
            "farm_auxiliary","barn","cabin").joinToString("|") +
        " and !building:levels and !height and !building:height " +
        " and !man_made and location!=underground "
    override val commitMessage = "Add building and roof levels"
    override val icon = R.drawable.ic_quest_building_levels

    override fun getTitle(tags: Map<String, String>) =
        if (tags.containsKey("building:part"))
            R.string.quest_buildingLevels_title_buildingPart
        else
            R.string.quest_buildingLevels_title

    override fun createForm() = AddBuildingLevelsForm()

    override fun applyAnswerTo(answer: BuildingLevelsAnswer, changes: StringMapChangesBuilder) {
        changes.add("building:levels", answer.levels.toString())
        answer.roofLevels?.let { changes.addOrModify("roof:levels", it.toString()) }
    }
}
