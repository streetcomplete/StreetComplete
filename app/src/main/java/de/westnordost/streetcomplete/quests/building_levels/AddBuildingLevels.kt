package de.westnordost.streetcomplete.quests.building_levels

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestController
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BUILDING
import de.westnordost.streetcomplete.osm.BUILDINGS_WITH_LEVELS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.questPrefix

class AddBuildingLevels : OsmFilterQuestType<BuildingLevelsAnswer>() {

    override val elementFilter = """
        ways, relations with
           building ~ ${BUILDINGS_WITH_LEVELS.joinToString("|")}
           and (
               !building:levels
               ${if (prefs.getBoolean(questPrefix(prefs) + MANDATORY_ROOF_LEVELS, true))
                   "or !roof:levels and roof:shape and roof:shape != flat"
                   else ""
               }
           )
           and !building:min_level
           and !man_made
           and location != underground
           and ruins != yes
    """
    override val changesetComment = "Specify building and roof levels"
    override val wikiLink = "Key:building:levels"
    override val icon = R.drawable.ic_quest_building_levels
    override val achievements = listOf(BUILDING)
    override val defaultDisabledMessage = R.string.default_disabled_msg_difficult_and_time_consuming

    override val hint = R.string.quest_buildingLevels_hint

    override fun getTitle(tags: Map<String, String>) = when {
        tags.containsKey("building:part") -> R.string.quest_buildingLevels_title_buildingPart2
        else -> R.string.quest_buildingLevels_title2
    }

    override fun createForm() = AddBuildingLevelsForm()

    override fun applyAnswerTo(answer: BuildingLevelsAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["building:levels"] = answer.levels.toString()
        answer.roofLevels?.let { tags["roof:levels"] = it.toString() }
    }

    override val hasQuestSettings = true

    override fun getQuestSettingsDialog(context: Context): AlertDialog {
        val array = arrayOf(
            context.getString(R.string.quest_settings_building_levels_mandatory_roof),
            context.getString(R.string.quest_settings_building_levels_optional_roof)
        )
        return AlertDialog.Builder(context)
            .setSingleChoiceItems(array, if (prefs.getBoolean(questPrefix(prefs) + MANDATORY_ROOF_LEVELS, true)) 0 else 1) { d, i ->
                if (i == 0)
                    prefs.edit { remove(questPrefix(prefs) + MANDATORY_ROOF_LEVELS) }
                else
                    prefs.edit { putBoolean(questPrefix(prefs) + MANDATORY_ROOF_LEVELS, false) }
                d.dismiss()
                OsmQuestController.reloadQuestTypes()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .setNeutralButton(R.string.element_selection_button) { _, _ ->
                super.getQuestSettingsDialog(context)?.show()
            }.create()
    }
}

const val MANDATORY_ROOF_LEVELS = "qs_AddBuildingLevels_mandatory_roof_levels"
