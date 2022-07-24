package de.westnordost.streetcomplete.quests.surface

import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.WHEELCHAIR
import de.westnordost.streetcomplete.osm.ANYTHING_UNPAVED
import de.westnordost.streetcomplete.quests.questPrefix
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.screens.settings.SettingsFragment

class AddPathSurface(private val prefs: SharedPreferences) : OsmFilterQuestType<SurfaceOrIsStepsAnswer>() {

    override val elementFilter = """
        ways with highway ~ path|footway|cycleway|bridleway|steps
        and segregated != yes
        and access !~ private|no
        and (!conveying or conveying = no)
        and (!indoor or indoor = no)
        and (
          !surface
          or surface ~ ${ANYTHING_UNPAVED.joinToString("|")} and surface older today -6 years
          or surface older today -8 years
          or (
            surface ~ ${if (prefs.getBoolean(questPrefix(prefs) + ALLOW_GENERIC_PATH, false)) "" else "paved|unpaved|"}cobblestone
            and !surface:note
            and !note:surface
          )
        )
    """
    /* ~paved ways are less likely to change the surface type */

    override val changesetComment = "Specify path surfaces"
    override val wikiLink = "Key:surface"
    override val icon = R.drawable.ic_quest_way_surface
    override val achievements = listOf(PEDESTRIAN, WHEELCHAIR, BICYCLIST, OUTDOORS)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_surface_title

    override fun createForm() = AddPathSurfaceForm(prefs.getBoolean(questPrefix(prefs) + ALLOW_GENERIC_PATH, false))

    override fun applyAnswerTo(answer: SurfaceOrIsStepsAnswer, tags: Tags, timestampEdited: Long) {
        when (answer) {
            is SurfaceAnswer -> {
                answer.applyTo(tags, "surface")
            }
            is IsActuallyStepsAnswer -> {
                tags["highway"] = "steps"
            }
            is IsIndoorsAnswer -> {
                tags["indoor"] = "yes"
            }
        }
    }

    override val hasQuestSettings = true

    override fun getQuestSettingsDialog(context: Context): AlertDialog {
        return AlertDialog.Builder(context)
            .setMessage(R.string.quest_generic_surface_message)
            .setNeutralButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.quest_generic_surface_yes) { _,_ ->
                prefs.edit().putBoolean(questPrefix(prefs) + ALLOW_GENERIC_PATH, true).apply()
                SettingsFragment.restartNecessary = true
            }
            .setNegativeButton(R.string.quest_generic_surface_no) { _,_ ->
                prefs.edit().putBoolean(questPrefix(prefs) + ALLOW_GENERIC_PATH, false).apply()
                SettingsFragment.restartNecessary = true
            }
            .create()
    }
}

private const val ALLOW_GENERIC_PATH = "qs_AddPathSurface_allow_generic"
