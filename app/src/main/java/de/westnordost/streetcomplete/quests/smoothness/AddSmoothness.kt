package de.westnordost.streetcomplete.quests.smoothness

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.ALL_ROADS
import de.westnordost.streetcomplete.data.meta.ANYTHING_PAVED
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder

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
        changes.updateWithCheckDate("smoothness", answer.osmValue)
    }
}

val ALL_PATHS_EXCEPT_STEPS = listOf("footway", "cycleway", "path", "bridleway")
private val SURFACES_FOR_SMOOTHNESS = listOf( // surfaces that are actually used in AddSmoothnessForm
    "asphalt", "sett", "concrete", "paving_stones", "metal", "unhewn_cobblestone",
    "compacted", "grass_paver", "gravel", "fine_gravel", "pebbles", "dirt", "grass"
)
