package de.westnordost.streetcomplete.quests.step_count

import android.content.Context
import android.content.SharedPreferences
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.quests.numberSelectionDialog
import de.westnordost.streetcomplete.quests.questPrefix
import de.westnordost.streetcomplete.util.math.measuredLength

class AddStepCount(private val prefs: SharedPreferences) : OsmElementQuestType<Int> {

    val elementFilter by lazy { """
        ways with highway = steps
         and (!indoor or indoor = no)
         and access !~ private|no
         and (!conveying or conveying = no)
         and !step_count
    """.toElementFilterExpression() }
    override val changesetComment = "Specify step counts"
    override val wikiLink = "Key:step_count"
    override val icon = R.drawable.ic_quest_steps_count
    // because the user needs to start counting at the start of the steps
    override val hasMarkersAtEnds = true
    override val achievements = listOf(PEDESTRIAN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_step_count_title

    override fun isApplicableTo(element: Element): Boolean? {
        if (!elementFilter.matches(element)) return false
        return null
    }

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        return mapData.filter { element ->
            if (!elementFilter.matches(element)) return@filter false
            val geometry = mapData.getWayGeometry(element.id) as? ElementPolylinesGeometry
            val totalLength = geometry?.polylines?.sumOf { it.measuredLength() } ?: return@filter true
            totalLength <= prefs.getInt(questPrefix(prefs) + PREF_MAX_STEPS_LENGTH, 999)
        }
    }

    override fun createForm() = AddStepCountForm()

    override fun applyAnswerTo(answer: Int, tags: Tags, timestampEdited: Long) {
        tags["step_count"] = answer.toString()
    }

    override val hasQuestSettings = true

    override fun getQuestSettingsDialog(context: Context) = numberSelectionDialog(
        context, prefs, questPrefix(prefs) + PREF_MAX_STEPS_LENGTH, 999, R.string.quest_settings_max_steps_length
    )

}

private const val PREF_MAX_STEPS_LENGTH = "qs_AddStepCount_max_length"
