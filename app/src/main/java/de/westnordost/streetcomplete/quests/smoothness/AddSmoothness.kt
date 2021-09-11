package de.westnordost.streetcomplete.quests.smoothness

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.ALL_ROADS
import de.westnordost.streetcomplete.data.meta.ANYTHING_PAVED
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.quests.surface.Surface

class AddSmoothness : OsmFilterQuestType<SmoothnessAnswer>() {

    // maybe exclude service roads? or driveways?
    override val elementFilter = """
        ways with highway
        and highway ~ ${(ALL_PATHS_EXCEPT_STEPS + ALL_ROADS).joinToString("|")}
        and surface ~ ${SURFACES_FOR_SMOOTHNESS.joinToString("|")}
        and access !~ private|no
        and (!conveying or conveying = no)
        and (!indoor or indoor = no)
        and !cycleway:surface and !footway:surface
        and (
          !smoothness
          or smoothness older today -4 years
        )
    """
    /* resurvey:
     * what it more/less likely to change?
     * paved and good/intermediate might stay that way for a long time
     * excellent might soon change to good
     * bad roads might get repaired
     * unpaved roads are likely to change anyway
     * metal should stay the way it is for a long time
     *  */

    override val commitMessage = "Add smoothness"
    override val wikiLink = "Key:smoothness"
    override val icon = R.drawable.ic_quest_street_surface_detail
    override val isSplitWayEnabled = true

    override fun getTitle(tags: Map<String, String>): Int {
        val hasName = tags.containsKey("name")
        val isSquare = tags["area"] == "yes"
        val isPath = tags["highway"] in ALL_PATHS_EXCEPT_STEPS
        return when {
            hasName ->     R.string.quest_smoothness_name_title
            isSquare ->    R.string.quest_smoothness_square_title
            isPath ->      R.string.quest_smoothness_path_title
            else ->        R.string.quest_smoothness_road_title
        }
    }

    override fun createForm() = AddSmoothnessForm()

    override fun applyAnswerTo(answer: SmoothnessAnswer, changes: StringMapChangesBuilder) {
        when (answer) {
            is SmoothnessValueAnswer -> changes.updateWithCheckDate("smoothness", answer.osmValue)
            is WrongSurfaceAnswer -> changes.delete("surface")
        }
    }
}

// smoothness is not asked for steps
// "pedestrian" is in here so the path answers are shown instead of road answers (which focus on cars)
val ALL_PATHS_EXCEPT_STEPS = listOf("footway", "cycleway", "path", "bridleway", "pedestrian")

// surfaces that are actually used in AddSmoothnessForm
// should only contain values that are in the Surface class
private val SURFACES_FOR_SMOOTHNESS = listOf(
    "asphalt", "sett", "paving_stones", "compacted", "gravel"
)
