package de.westnordost.streetcomplete.quests.surface

import androidx.appcompat.app.AlertDialog
import android.content.Context
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.WHEELCHAIR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.changeToSteps
import de.westnordost.streetcomplete.osm.surface.INVALID_SURFACES
import de.westnordost.streetcomplete.osm.surface.applyTo
import de.westnordost.streetcomplete.quests.booleanQuestSettingsDialog
import de.westnordost.streetcomplete.quests.questPrefix
import de.westnordost.streetcomplete.quests.singleTypeElementSelectionDialog

class AddPathSurface : OsmFilterQuestType<SurfaceOrIsStepsAnswer>() {

    override val elementFilter = """
        ways with highway ~ ${prefs.getString("${questPrefix(prefs)}qs_${name}_highway_selection", HIGHWAY_TYPES)}
        and segregated != yes
        and access !~ private|no
        and (!conveying or conveying = no)
        and (!indoor or indoor = no)
        and (
          !surface
          or surface ~ ${INVALID_SURFACES.joinToString("|")}
          or (
            surface ~ paved|unpaved
            and !surface:note
            and !note:surface
            and !check_date:surface
          )
          or surface older today -8 years
        )
        and ~path|footway|cycleway|bridleway !~ link
    """

    override val changesetComment = "Specify path surfaces"
    override val wikiLink = "Key:surface"
    override val icon = R.drawable.ic_quest_way_surface
    override val achievements = listOf(PEDESTRIAN, WHEELCHAIR, BICYCLIST, OUTDOORS)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_surface_title

    override fun createForm() = AddPathSurfaceForm()

    override fun applyAnswerTo(answer: SurfaceOrIsStepsAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is SurfaceAnswer -> {
                answer.value.applyTo(tags)
            }
            is IsActuallyStepsAnswer -> {
                tags.changeToSteps()
            }
            is IsIndoorsAnswer -> {
                tags["indoor"] = "yes"
            }
        }
    }

    override val hasQuestSettings = true

    override fun getQuestSettingsDialog(context: Context): AlertDialog =
        singleTypeElementSelectionDialog(context, prefs, "${questPrefix(prefs)}qs_${name}_highway_selection", HIGHWAY_TYPES, R.string.quest_settings_eligible_highways)
}

private const val HIGHWAY_TYPES = "path|footway|cycleway|bridleway|steps"
