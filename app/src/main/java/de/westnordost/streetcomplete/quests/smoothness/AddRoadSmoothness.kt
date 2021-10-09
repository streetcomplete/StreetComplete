package de.westnordost.streetcomplete.quests.smoothness

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.ALL_ROADS
import de.westnordost.streetcomplete.data.meta.ANYTHING_PAVED
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.quests.surface.Surface
import de.westnordost.streetcomplete.quests.surface.asItem

class AddRoadSmoothness : OsmFilterQuestType<SmoothnessAnswer>() {

    // maybe exclude service roads? or driveways?
    override val elementFilter = """
        ways with highway
        and highway ~ ${ALL_ROADS.joinToString("|")}
        and surface ~ ${SURFACES_FOR_SMOOTHNESS.joinToString("|")}
        and access !~ private|no
        and (
          !smoothness
          or smoothness older today -4 years
        )
    """

    override val commitMessage = "Add road smoothness"
    override val wikiLink = "Key:smoothness"
    override val icon = R.drawable.ic_quest_street_surface_detail
    override val isSplitWayEnabled = true

    override fun getTitle(tags: Map<String, String>): Int {
        val hasName = tags.containsKey("name")
        val isSquare = tags["area"] == "yes"
        return when {
            hasName ->     R.string.quest_smoothness_name_title
            isSquare ->    R.string.quest_smoothness_square_title
            else ->        R.string.quest_smoothness_road_title
        }
    }

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> {
        val surface = Surface.values().find { it.osmValue == tags["surface"] }!!
        val surfaceString = surface.asItem().title.toString()
        return if (tags.containsKey("name"))
            arrayOf(tags["name"]!!, surfaceString)
        else
            arrayOf(surfaceString)
    }

    override fun createForm() = AddSmoothnessForm()

    override fun applyAnswerTo(answer: SmoothnessAnswer, changes: StringMapChangesBuilder) {
        when (answer) {
            is SmoothnessValueAnswer -> changes.updateWithCheckDate("smoothness", answer.osmValue)
            is WrongSurfaceAnswer -> changes.delete("surface")
        }
    }
}

// surfaces that are actually used in AddSmoothnessForm
// should only contain values that are in the Surface class
val SURFACES_FOR_SMOOTHNESS = listOf(
    "asphalt", "sett", "paving_stones", "compacted", "gravel"
)
