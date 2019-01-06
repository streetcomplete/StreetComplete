package de.westnordost.streetcomplete.quests.building_levels

import android.os.Bundle

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao

class AddBuildingLevels(o: OverpassMapDataDao) : SimpleOverpassQuestType(o) {

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

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        changes.add("building:levels", "" + answer.getInt(AddBuildingLevelsForm.BUILDING_LEVELS))

        // only set the roof levels if the user supplied that in the form
        val roofLevels = answer.getInt(AddBuildingLevelsForm.ROOF_LEVELS, -1)
        if (roofLevels != -1) {
            changes.addOrModify("roof:levels", "" + roofLevels)
        }
    }
}
