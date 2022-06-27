package de.westnordost.streetcomplete.quests.smoothness

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.WHEELCHAIR
import de.westnordost.streetcomplete.osm.removeCheckDatesForKey
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.quests.questPrefix
import de.westnordost.streetcomplete.screens.settings.SettingsFragment

class AddPathSmoothness(private val prefs: SharedPreferences) : OsmFilterQuestType<SmoothnessAnswer>() {

    override val elementFilter = """
        ways with
          highway ~ ${ALL_PATHS_EXCEPT_STEPS.joinToString("|")}
          and surface ${if (prefs.getBoolean(questPrefix(prefs) + SMOOTHNESS_FOR_ALL_SURFACES, false)) "" else "~ ${SURFACES_FOR_SMOOTHNESS.joinToString("|")}"}
          and access !~ private|no
          and segregated != yes
          and (!conveying or conveying = no)
          and (!indoor or indoor = no)
          and !cycleway:surface and !footway:surface
          and (
            !smoothness
            or smoothness older today -4 years
            or smoothness:date < today -4 years
          )
    """
    override val changesetComment = "Add path smoothness"
    override val wikiLink = "Key:smoothness"
    override val icon = R.drawable.ic_quest_way_surface_detail
    override val isSplitWayEnabled = true
    override val questTypeAchievements = listOf(WHEELCHAIR, BICYCLIST)
    override val defaultDisabledMessage = R.string.default_disabled_msg_difficult_and_time_consuming

    override fun getTitle(tags: Map<String, String>) = R.string.quest_smoothness_title2

    override fun getTitleArgs(tags: Map<String, String>): Array<String>
        = arrayOf(tags["surface"].toString())

    override fun createForm() = AddSmoothnessForm()

    override fun applyAnswerTo(answer: SmoothnessAnswer, tags: Tags, timestampEdited: Long) {
        when (answer) {
            is SmoothnessValueAnswer -> {
                tags.updateWithCheckDate("smoothness", answer.value.osmValue)
                tags.remove("smoothness:date")
            }
            is WrongSurfaceAnswer -> {
                tags.remove("surface")
                tags.remove("smoothness")
                tags.remove("smoothness:date")
                tags.removeCheckDatesForKey("smoothness")
            }
            is IsActuallyStepsAnswer -> {
                tags["highway"] = "steps"
                tags.remove("smoothness")
                tags.remove("smoothness:date")
                tags.removeCheckDatesForKey("smoothness")
            }
        }
    }

    override val hasQuestSettings = true

    override fun getQuestSettingsDialog(context: Context): AlertDialog =
        AlertDialog.Builder(context)
            .setMessage(R.string.quest_smoothness_generic_surface_message)
            .setNeutralButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.quest_smoothness_generic_surface_yes) { _,_ ->
                prefs.edit().putBoolean(questPrefix(prefs) + SMOOTHNESS_FOR_ALL_SURFACES, false).apply()
                SettingsFragment.restartNecessary = true
            }
            .setNegativeButton(R.string.quest_smoothness_generic_surface_no) { _,_ ->
                prefs.edit().putBoolean(questPrefix(prefs) + SMOOTHNESS_FOR_ALL_SURFACES, false).apply()
                SettingsFragment.restartNecessary = true
            }
            .create()
}

// smoothness is not asked for steps
// "pedestrian" is in here so the path answers are shown instead of road answers (which focus on cars)
val ALL_PATHS_EXCEPT_STEPS = listOf("footway", "cycleway", "path", "bridleway", "pedestrian")

private const val SMOOTHNESS_FOR_ALL_SURFACES = "qs_AddPathSmoothness_all_surfaces"
