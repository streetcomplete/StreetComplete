package de.westnordost.streetcomplete.quests.surface

import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.ANYTHING_UNPAVED
import de.westnordost.streetcomplete.quests.questPrefix
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.screens.settings.SettingsFragment

class AddRoadSurface(private val prefs: SharedPreferences) : OsmFilterQuestType<SurfaceAnswer>() {

    override val elementFilter = """
        ways with (
          highway ~ ${listOf(
            "primary", "primary_link", "secondary", "secondary_link", "tertiary", "tertiary_link",
            "unclassified", "residential", "living_street", "pedestrian", "track",
            ).joinToString("|")
          }
          or highway = service and service !~ driveway|slipway
        )
        and (
          !surface
          or surface ~ ${ANYTHING_UNPAVED.joinToString("|")} and surface older today -6 years
          or surface older today -12 years
          or (
            surface ~ ${if (prefs.getBoolean(questPrefix(prefs) + ALLOW_GENERIC_ROAD, false)) "" else "paved|unpaved|"}cobblestone
            and !surface:note
            and !note:surface
          )
        )
        and (access !~ private|no or (foot and foot !~ private|no))
    """
    override val changesetComment = "Add road surface info"
    override val wikiLink = "Key:surface"
    override val icon = R.drawable.ic_quest_street_surface
    override val achievements = listOf(CAR)

    override fun getTitle(tags: Map<String, String>) =
        if (tags["area"] == "yes") R.string.quest_streetSurface_square_title
        else                       R.string.quest_streetSurface_title

    override fun createForm() = AddRoadSurfaceForm(prefs.getBoolean(questPrefix(prefs) + ALLOW_GENERIC_ROAD, false))

    override fun applyAnswerTo(answer: SurfaceAnswer, tags: Tags, timestampEdited: Long) {
        answer.applyTo(tags, "surface")
    }

    override val hasQuestSettings = true

    override fun getQuestSettingsDialog(context: Context): AlertDialog {
        return AlertDialog.Builder(context)
            .setMessage(R.string.quest_generic_surface_message)
            .setNeutralButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.quest_generic_surface_yes) { _,_ ->
                prefs.edit().putBoolean(questPrefix(prefs) + ALLOW_GENERIC_ROAD, true).apply()
                SettingsFragment.restartNecessary = true
            }
            .setNegativeButton(R.string.quest_generic_surface_no) { _,_ ->
                prefs.edit().putBoolean(questPrefix(prefs) + ALLOW_GENERIC_ROAD, false).apply()
                SettingsFragment.restartNecessary = true
            }
            .create()
    }
}

private const val ALLOW_GENERIC_ROAD = "qs_AddRoadSurface_allow_generic"
