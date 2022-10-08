package de.westnordost.streetcomplete.quests.building_levels

import android.content.Context
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BUILDING
import de.westnordost.streetcomplete.quests.singleTypeElementSelectionDialog
import de.westnordost.streetcomplete.osm.BUILDINGS_WITH_LEVELS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.questPrefix

class AddBuildingLevels : OsmFilterQuestType<BuildingLevelsAnswer>() {

    override val elementFilter = """
        ways, relations with
         building ~ ${prefs.getString(questPrefix(prefs) + PREF_BUILDING_LEVELS_SELECTION, BUILDINGS_WITH_LEVELS.joinToString("|"))}
         and !building:levels
         and !man_made
         and location != underground
         and ruins != yes
    """
    override val changesetComment = "Specify building and roof levels"
    override val wikiLink = "Key:building:levels"
    override val icon = R.drawable.ic_quest_building_levels
    override val achievements = listOf(BUILDING)
    override val defaultDisabledMessage = R.string.default_disabled_msg_difficult_and_time_consuming

    override fun getTitle(tags: Map<String, String>) = when {
        tags.containsKey("building:part") -> R.string.quest_buildingLevels_title_buildingPart2
        else -> R.string.quest_buildingLevels_title2
    }

    override fun createForm() = AddBuildingLevelsForm()

    override fun applyAnswerTo(answer: BuildingLevelsAnswer, tags: Tags, timestampEdited: Long) {
        tags["building:levels"] = answer.levels.toString()
        answer.roofLevels?.let { tags["roof:levels"] = it.toString() }
    }

    override val hasQuestSettings = true

    override fun getQuestSettingsDialog(context: Context) =
        singleTypeElementSelectionDialog(context, prefs, questPrefix(prefs) + PREF_BUILDING_LEVELS_SELECTION, BUILDINGS_WITH_LEVELS.joinToString("|"), R.string.quest_settings_building_levels_message)
}

private const val PREF_BUILDING_LEVELS_SELECTION = "qs_AddBuildingLevels_element_selection"
