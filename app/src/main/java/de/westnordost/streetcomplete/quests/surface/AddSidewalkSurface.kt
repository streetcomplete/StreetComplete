package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.ANYTHING_UNPAVED
import de.westnordost.streetcomplete.data.meta.SIDEWALK_SURFACE_KEYS
import de.westnordost.streetcomplete.data.meta.hasCheckDateForKey
import de.westnordost.streetcomplete.data.meta.removeCheckDatesForKey
import de.westnordost.streetcomplete.data.meta.updateCheckDateForKey
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.WHEELCHAIR

class AddSidewalkSurface : OsmFilterQuestType<SidewalkSurfaceAnswer>() {

    override val elementFilter = """
        ways with
            highway ~ trunk|trunk_link|primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified|residential
            and area != yes
            and motorroad != yes
            and (
                sidewalk = both or sidewalk = left or sidewalk = right or
                (sidewalk:left = yes and sidewalk:right ~ yes|no|separate) or
                (sidewalk:right = yes and sidewalk:left ~ yes|no|separate)
            )
            and (
                ${SIDEWALK_SURFACE_KEYS.joinToString(" and ") {"!$it"}}
                or sidewalk:both:surface ~ ${ANYTHING_UNPAVED.joinToString("|")} and sidewalk:both:surface older today -4 years
                or sidewalk:left:surface ~ ${ANYTHING_UNPAVED.joinToString("|")} and sidewalk:left:surface older today -4 years
                or sidewalk:right:surface ~ ${ANYTHING_UNPAVED.joinToString("|")} and sidewalk:right:surface older today -4 years
                or ${SIDEWALK_SURFACE_KEYS.joinToString(" or ") {"$it older today -8 years"}}
                or (
                  sidewalk:both:surface ~ paved|unpaved|cobblestone
                  and !sidewalk:both:surface:note
                  and !note:sidewalk:both:surface
                )
                or (
                  sidewalk:left:surface ~ paved|unpaved|cobblestone
                  and !sidewalk:left:surface:note
                  and !note:sidewalk:left:surface
                )
                or (
                  sidewalk:right:surface ~ paved|unpaved|cobblestone
                  and !sidewalk:right:surface:note
                  and !note:sidewalk:right:surface
                )
            )
    """

    override val changesetComment = "Add surface of sidewalks"
    override val wikiLink = "Key:sidewalk"
    override val icon = R.drawable.ic_quest_sidewalk_surface
    override val isSplitWayEnabled = true
    override val questTypeAchievements = listOf(PEDESTRIAN, WHEELCHAIR, OUTDOORS)
    override val defaultDisabledMessage = R.string.default_disabled_msg_difficult_and_time_consuming

    override fun getTitle(tags: Map<String, String>) : Int =
        R.string.quest_sidewalk_surface_title

    override fun createForm() = AddSidewalkSurfaceForm()

    override fun applyAnswerTo(answer: SidewalkSurfaceAnswer, tags: Tags, timestampEdited: Long) {
        val leftChanged = answer.left?.let { sideSurfaceChanged(it, Side.LEFT, tags) }
        val rightChanged = answer.right?.let { sideSurfaceChanged(it,  Side.RIGHT, tags) }

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

        // only set the check date if nothing was changed
        for (side in arrayOf("both", "left", "right")) {
            if ((!tags.hasChanges && tags.containsKey("sidewalk:$side:surface")) ||
                    tags.hasCheckDateForKey("sidewalk:$side:surface")) {
                tags.updateCheckDateForKey("sidewalk:$side:surface")
            }
        }
    }

    private enum class Side(val value: String) {
        LEFT("left"), RIGHT("right"), BOTH("both")
    }

    private fun sideSurfaceChanged(surface: SurfaceAnswer, side: Side, tags: Tags): Boolean {
        val previousSideValue = tags["sidewalk:${side.value}:surface"]
        val previousBothOsmValue = tags["sidewalk:both:surface"]
        val osmValue = surface.value.osmValue

        return if (previousSideValue != null && previousSideValue != osmValue) {
            true
        } else previousBothOsmValue != null && previousBothOsmValue != osmValue
    }

    private fun applySidewalkSurfaceAnswerTo(surface: SurfaceAnswer, side: Side, tags: Tags)
    {
        val sidewalkKey = "sidewalk:" + side.value
        val sidewalkSurfaceKey = "$sidewalkKey:surface"

        tags.updateWithCheckDate(sidewalkSurfaceKey, surface.value.osmValue)

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
