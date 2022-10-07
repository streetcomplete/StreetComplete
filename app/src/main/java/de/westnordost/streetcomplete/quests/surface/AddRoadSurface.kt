package de.westnordost.streetcomplete.quests.surface

import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.ANYTHING_UNPAVED
import de.westnordost.streetcomplete.quests.questPrefix
import de.westnordost.streetcomplete.osm.INVALID_SURFACES_FOR_TRACKTYPES
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.booleanQuestSettingsDialog

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
          ${INVALID_SURFACES_FOR_TRACKTYPES.map{tracktypeConflictClause(it)}.joinToString("\n")}
        )
        and (access !~ private|no or (foot and foot !~ private|no))
    """

    private fun tracktypeConflictClause(conflictEntry: Map.Entry<String, Set<String>>): String {
        return "          or tracktype = " + conflictEntry.key + " and surface ~ ${conflictEntry.value.joinToString("|")}"
    }

    override val changesetComment = "Specify road surfaces"
    override val wikiLink = "Key:surface"
    override val icon = R.drawable.ic_quest_street_surface
    override val achievements = listOf(CAR, BICYCLIST)

    override fun getTitle(tags: Map<String, String>) =
        if (tags["area"] == "yes") R.string.quest_streetSurface_square_title
        else                       R.string.quest_streetSurface_title

    override fun createForm() = AddRoadSurfaceForm(prefs.getBoolean(questPrefix(prefs) + ALLOW_GENERIC_ROAD, false))

    override fun applyAnswerTo(answer: SurfaceAnswer, tags: Tags, timestampEdited: Long) {
        answer.applyTo(tags, "surface")
    }

    override val hasQuestSettings = true

    override fun getQuestSettingsDialog(context: Context): AlertDialog =
        booleanQuestSettingsDialog(context, prefs, questPrefix(prefs) + ALLOW_GENERIC_ROAD, R.string.quest_generic_surface_message)
}

private const val ALLOW_GENERIC_ROAD = "qs_AddRoadSurface_allow_generic"
