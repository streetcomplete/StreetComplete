package de.westnordost.streetcomplete.quests.surface

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.ANYTHING_UNPAVED

class AddRoadSurface(private val prefs: SharedPreferences) : OsmFilterQuestType<SurfaceOrIsStepsAnswer>() {

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
            surface ~ ${if (prefs.getBoolean(ALLOW_GENERIC_ROAD, false)) "" else "paved|unpaved|"}cobblestone
            and !surface:note
            and !note:surface
          )
        )
        and (access !~ private|no or (foot and foot !~ private|no))
    """
    override val changesetComment = "Add road surface info"
    override val wikiLink = "Key:surface"
    override val icon = R.drawable.ic_quest_street_surface
    override val isSplitWayEnabled = true
    override val questTypeAchievements = listOf(CAR)

    override fun getTitle(tags: Map<String, String>) =
        if (tags["area"] == "yes") R.string.quest_streetSurface_square_title
        else                       R.string.quest_streetSurface_title

    override fun createForm() = AddRoadSurfaceForm(prefs.getBoolean(ALLOW_GENERIC_ROAD, false))

    override fun applyAnswerTo(answer: SurfaceOrIsStepsAnswer, tags: Tags, timestampEdited: Long) {
        if (answer is SurfaceAnswer)
            answer.applyTo(tags, "surface")
        else if (answer is IsPrivateAnswer)
            tags["access"] = "private"
    }

    override val hasQuestSettings = true

    override fun getQuestSettingsDialog(context: Context): AlertDialog? {
        return AlertDialog.Builder(context)
            .setMessage(R.string.quest_generic_surface_message)
            .setNeutralButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.quest_generic_surface_yes) { _,_ ->
                prefs.edit().putBoolean(ALLOW_GENERIC_ROAD, true).apply()
            }
            .setNegativeButton(R.string.quest_generic_surface_no) { _,_ ->
                prefs.edit().putBoolean(ALLOW_GENERIC_ROAD, false).apply()
            }
            .create()
    }
}

private const val ALLOW_GENERIC_ROAD = "allow_generic_road"
