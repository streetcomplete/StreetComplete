package de.westnordost.streetcomplete.quests.smoothness

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.removeCheckDatesForKey
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.quests.questPrefix
import de.westnordost.streetcomplete.screens.settings.SettingsFragment

class AddRoadSmoothness(private val prefs: SharedPreferences) : OsmFilterQuestType<SmoothnessAnswer>() {

    override val elementFilter = """
        ways with (
            highway ~ ${ROADS_TO_ASK_SMOOTHNESS_FOR.joinToString("|")}
            or highway = service and service !~ driveway|slipway
          )
          and surface ${if (prefs.getBoolean(questPrefix(prefs) + SMOOTHNESS_FOR_ALL_SURFACES, false)) "" else "~ ${SURFACES_FOR_SMOOTHNESS.joinToString("|")}"}
          and (access !~ private|no or (foot and foot !~ private|no))
          and (
            !smoothness
            or smoothness older today -4 years
            or smoothness:date < today -4 years
          )
    """

    override val changesetComment = "Add road smoothness"
    override val wikiLink = "Key:smoothness"
    override val icon = R.drawable.ic_quest_street_surface_detail
    override val achievements = listOf(CAR, BICYCLIST)
    override val defaultDisabledMessage = R.string.default_disabled_msg_difficult_and_time_consuming

    override fun getTitle(tags: Map<String, String>) =
        if (tags["area"] == "yes") R.string.quest_smoothness_square_title2
        else                       R.string.quest_smoothness_road_title2

    override fun getTitleArgs(tags: Map<String, String>): Array<String>
        = arrayOf(tags["surface"].toString())

    override fun createForm() = AddSmoothnessForm()

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry): Sequence<Element> {
        val nodes = (element as Way).nodeIds
        return getMapData().nodes.asSequence().filter { it.id in nodes && barrierFilter.matches(it) }
    }

    private val barrierFilter by lazy {
        "nodes with barrier or traffic_calming".toElementFilterExpression()
    }

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
            is IsActuallyStepsAnswer -> throw IllegalStateException()
        }
    }

    override val hasQuestSettings = true

    override fun getQuestSettingsDialog(context: Context): AlertDialog =
        AlertDialog.Builder(context)
            .setMessage(R.string.quest_smoothness_generic_surface_message)
            .setNeutralButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.quest_smoothness_generic_surface_yes) { _,_ ->
                prefs.edit().putBoolean(questPrefix(prefs) + SMOOTHNESS_FOR_ALL_SURFACES, true).apply()
                SettingsFragment.restartNecessary = true
            }
            .setNegativeButton(R.string.quest_smoothness_generic_surface_no) { _,_ ->
                prefs.edit().putBoolean(questPrefix(prefs) + SMOOTHNESS_FOR_ALL_SURFACES, false).apply()
                SettingsFragment.restartNecessary = true
            }
            .create()
}

// surfaces that are actually used in AddSmoothnessForm
// should only contain values that are in the Surface class
val SURFACES_FOR_SMOOTHNESS = listOf(
    "asphalt", "concrete", "concrete:plates", "sett", "paving_stones", "compacted", "gravel", "fine_gravel"
)

private val ROADS_TO_ASK_SMOOTHNESS_FOR = arrayOf(
    // "trunk","trunk_link","motorway","motorway_link", // too much, motorways are almost by definition smooth asphalt (or concrete)
    "primary", "primary_link", "secondary", "secondary_link", "tertiary", "tertiary_link",
    "unclassified", "residential", "living_street", "pedestrian", "track",
    // "service", // this is too much, and the information value is very low
)

private const val SMOOTHNESS_FOR_ALL_SURFACES = "qs_AddRoadSmoothness_all_surfaces"
