package de.westnordost.streetcomplete.quests.surface

import androidx.appcompat.app.AlertDialog
import android.content.Context
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.surface.INVALID_SURFACES
import de.westnordost.streetcomplete.osm.surface.INVALID_SURFACES_FOR_TRACKTYPES
import de.westnordost.streetcomplete.osm.surface.SurfaceAndNote
import de.westnordost.streetcomplete.osm.surface.UNPAVED_SURFACES
import de.westnordost.streetcomplete.osm.surface.applyTo
import de.westnordost.streetcomplete.quests.booleanQuestSettingsDialog
import de.westnordost.streetcomplete.quests.fullElementSelectionDialog
import de.westnordost.streetcomplete.quests.questPrefix

class AddRoadSurface : OsmFilterQuestType<SurfaceAndNote>() {

    override val elementFilter = """
        ways with (
          ${prefs.getString("${questPrefix(prefs)}qs_${name}_element_selection", highwaySelection)}
        )
        and (
          !surface
          or surface ~ ${UNPAVED_SURFACES.joinToString("|")} and surface older today -6 years
          or surface older today -12 years
          or (
            surface ~ ${if (prefs.getBoolean(questPrefix(prefs) + ALLOW_GENERIC_ROAD, false)) "" else "paved|unpaved|"}${INVALID_SURFACES.joinToString("|")}
            and !surface:note
            and !note:surface
          )
          ${INVALID_SURFACES_FOR_TRACKTYPES.entries.joinToString("\n") { (tracktype, surfaces) ->
              "or tracktype = $tracktype and surface ~ ${surfaces.joinToString("|")}"
          }}
        )
        and (access !~ private|no or (foot and foot !~ private|no))
    """

    override val changesetComment = "Specify road surfaces"
    override val wikiLink = "Key:surface"
    override val icon = R.drawable.ic_quest_street_surface
    override val achievements = listOf(CAR, BICYCLIST)

    override fun getTitle(tags: Map<String, String>) =
        if (tags["area"] == "yes") {
            R.string.quest_streetSurface_square_title
        } else {
            R.string.quest_streetSurface_title
        }

    override fun createForm() = AddRoadSurfaceForm()

    override fun applyAnswerTo(answer: SurfaceAndNote, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        answer.applyTo(tags)
    }

    override val hasQuestSettings = true

    override fun getQuestSettingsDialog(context: Context): AlertDialog =
        AlertDialog.Builder(context)
            .setTitle(R.string.quest_settings_what_to_edit)
            .setPositiveButton(R.string.quest_generic_surface_button) { _, _ ->
                booleanQuestSettingsDialog(context, prefs, questPrefix(prefs) + ALLOW_GENERIC_ROAD,
                    R.string.quest_generic_surface_message,
                    R.string.quest_generic_surface_yes,
                    R.string.quest_generic_surface_no
                ).show()
            }
            .setNeutralButton(android.R.string.cancel, null)
            .setNegativeButton(R.string.element_selection_button) { _, _ ->
                fullElementSelectionDialog(context, prefs, "${questPrefix(prefs)}qs_${name}_element_selection", R.string.quest_settings_element_selection, highwaySelection)
                    .show()
            }
            .create()
}

const val ALLOW_GENERIC_ROAD = "qs_AddRoadSurface_allow_generic"

private val highwaySelection = """
    highway ~ ${listOf(
    "primary", "primary_link", "secondary", "secondary_link", "tertiary", "tertiary_link",
    "unclassified", "residential", "living_street", "pedestrian", "track",
).joinToString("|")
}
          or highway = service and service !~ driveway|slipway
""".trimIndent()
