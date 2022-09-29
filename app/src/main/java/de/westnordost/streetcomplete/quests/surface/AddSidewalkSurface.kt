package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.WHEELCHAIR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.hasCheckDateForKey
import de.westnordost.streetcomplete.osm.removeCheckDatesForKey
import de.westnordost.streetcomplete.osm.updateCheckDateForKey

class AddSidewalkSurface : OsmFilterQuestType<SidewalkSurfaceAnswer>() {

    // Only roads with 'complete' sidewalk tagging (at least one side has sidewalk, other side specified)
    override val elementFilter = """
        ways with
            highway ~ motorway|motorway_link|trunk|trunk_link|primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified|residential|service|living_street
            and area != yes
            and (
                sidewalk ~ both|left|right or
                sidewalk:both = yes or
                (sidewalk:left = yes and sidewalk:right ~ yes|no|separate) or
                (sidewalk:right = yes and sidewalk:left ~ yes|no|separate)
            )
            and (
                !sidewalk:both:surface and !sidewalk:left:surface and !sidewalk:right:surface
                or sidewalk:surface older today -8 years
            )
    """
    override val changesetComment = "Specify sidewalk surfaces"
    override val wikiLink = "Key:sidewalk"
    override val icon = R.drawable.ic_quest_sidewalk_surface
    override val achievements = listOf(PEDESTRIAN, WHEELCHAIR)
    override val defaultDisabledMessage = R.string.default_disabled_msg_difficult_and_time_consuming

    override fun getTitle(tags: Map<String, String>) = R.string.quest_sidewalk_surface_title

    override fun createForm() = AddSidewalkSurfaceForm()

    override fun applyAnswerTo(answer: SidewalkSurfaceAnswer, tags: Tags, timestampEdited: Long) {
        val leftChanged = answer.left?.let { sideSurfaceChanged(it, Side.LEFT, tags) }
        val rightChanged = answer.right?.let { sideSurfaceChanged(it, Side.RIGHT, tags) }

        if (leftChanged == true) {
            deleteSmoothnessKeys(Side.LEFT, tags)
            deleteSmoothnessKeys(Side.BOTH, tags)
        }
        if (rightChanged == true) {
            deleteSmoothnessKeys(Side.RIGHT, tags)
            deleteSmoothnessKeys(Side.BOTH, tags)
        }

        if (answer.left == answer.right) {
            answer.left?.let { applySidewalkSurfaceAnswerTo(it, Side.BOTH, tags) }
            deleteSidewalkSurfaceAnswerIfExists(Side.LEFT, tags)
            deleteSidewalkSurfaceAnswerIfExists(Side.RIGHT, tags)
        } else {
            answer.left?.let { applySidewalkSurfaceAnswerTo(it, Side.LEFT, tags) }
            answer.right?.let { applySidewalkSurfaceAnswerTo(it, Side.RIGHT, tags) }
            deleteSidewalkSurfaceAnswerIfExists(Side.BOTH, tags)
        }
        deleteSidewalkSurfaceAnswerIfExists(null, tags)

        // only set the check date if nothing was changed or if check date was already set
        if (!tags.hasChanges || tags.hasCheckDateForKey("sidewalk:surface")) {
            tags.updateCheckDateForKey("sidewalk:surface")
        }
    }

    private enum class Side(val value: String) {
        LEFT("left"), RIGHT("right"), BOTH("both")
    }

    private fun sideSurfaceChanged(surface: SurfaceAnswer, side: Side, tags: Tags): Boolean {
        val previousSideOsmValue = tags["sidewalk:${side.value}:surface"]
        val previousBothOsmValue = tags["sidewalk:both:surface"]
        val osmValue = surface.value.osmValue

        return previousSideOsmValue != null && previousSideOsmValue != osmValue
            || previousBothOsmValue != null && previousBothOsmValue != osmValue
    }

    private fun applySidewalkSurfaceAnswerTo(surface: SurfaceAnswer, side: Side, tags: Tags) {
        val sidewalkKey = "sidewalk:" + side.value
        val sidewalkSurfaceKey = "$sidewalkKey:surface"

        tags[sidewalkSurfaceKey] = surface.value.osmValue

        // add/remove note - used to describe generic surfaces
        if (surface.note != null) {
            tags["$sidewalkSurfaceKey:note"] = surface.note
        } else {
            tags.remove("$sidewalkSurfaceKey:note")
        }
        // clean up old source tags - source should be in changeset tags
        tags.remove("source:$sidewalkSurfaceKey")
    }

    /** clear smoothness tags for the given side*/
    private fun deleteSmoothnessKeys(side: Side, tags: Tags) {
        val sidewalkKey = "sidewalk:" + side.value
        tags.remove("$sidewalkKey:smoothness")
        tags.remove("$sidewalkKey:smoothness:date")
        tags.removeCheckDatesForKey("$sidewalkKey:smoothness")
    }

    /** clear previous answers for the given side */
    private fun deleteSidewalkSurfaceAnswerIfExists(side: Side?, tags: Tags) {
        val sideVal = if (side == null) "" else ":" + side.value
        val sidewalkSurfaceKey = "sidewalk$sideVal:surface"

        // only things are cleared that are set by this quest
        // for example cycleway:surface should only be cleared by a cycleway surface quest etc.
        tags.remove(sidewalkSurfaceKey)
        tags.remove("$sidewalkSurfaceKey:note")
    }
}
