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
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.UNPAVED_SURFACES
import de.westnordost.streetcomplete.osm.surface.applyTo
import de.westnordost.streetcomplete.quests.booleanQuestSettingsDialog
import de.westnordost.streetcomplete.quests.fullElementSelectionDialog
import de.westnordost.streetcomplete.quests.questPrefix

class AddRoadSurface : OsmFilterQuestType<Surface>() {

    override val elementFilter = """
        ways with (
          ${prefs.getString("${questPrefix(prefs)}qs_${name}_element_selection", highwaySelection)}
        )
        and (
          !surface
          or surface ~ ${INVALID_SURFACES.joinToString("|")}
          or (
            surface ~ paved|unpaved
            and !surface:note
            and !note:surface
            and !check_date:surface
          )
          or surface ~ ${UNPAVED_SURFACES.joinToString("|")} and surface older today -6 years
          or surface older today -12 years
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

    override fun applyAnswerTo(answer: Surface, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        answer.applyTo(tags)
    }

    override val hasQuestSettings = true

    override fun getQuestSettingsDialog(context: Context): AlertDialog =
        fullElementSelectionDialog(context, prefs, "${questPrefix(prefs)}qs_${name}_element_selection", R.string.quest_settings_element_selection, highwaySelection)
}

private val highwaySelection = """
    highway ~ ${listOf(
    "primary", "primary_link", "secondary", "secondary_link", "tertiary", "tertiary_link",
    "unclassified", "residential", "living_street", "pedestrian", "track", "busway"
).joinToString("|")
}
          or highway = service and service !~ driveway|slipway
""".trimIndent()
