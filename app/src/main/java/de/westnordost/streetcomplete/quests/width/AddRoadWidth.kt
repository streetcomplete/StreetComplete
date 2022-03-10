package de.westnordost.streetcomplete.quests.width

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement
import de.westnordost.streetcomplete.measure.ArSupportChecker
import de.westnordost.streetcomplete.osm.ANYTHING_PAVED
import de.westnordost.streetcomplete.osm.ROADS_ASSUMED_TO_BE_PAVED

class AddRoadWidth(
    private val checkArSupport: ArSupportChecker
) : OsmFilterQuestType<WidthAnswer>() {

    override val elementFilter = """
        ways with (
          (
            highway ~ trunk|primary|secondary|tertiary|unclassified|residential
            and (lane_markings = no or lanes < 2)
          ) or (
            highway = residential
            and (maxspeed <= 30 or maxspeed ~ "([1-9]|1[0-9]|20) mph")
            and lane_markings != yes and (!lanes or lanes < 2)
          )
          or highway = living_street
          or highway = service and service = alley
        )
        and area != yes
        and (!width or source:width ~ ".*estimat.*")
        and (surface ~ ${ANYTHING_PAVED.joinToString("|")} or highway ~ ${ROADS_ASSUMED_TO_BE_PAVED.joinToString("|")})
        and (access !~ private|no or (foot and foot !~ private|no))
        and placement != transition
    """
    override val changesetComment = "Determine road width"
    override val wikiLink = "Key:width"
    override val icon = R.drawable.ic_quest_street_width
    override val isSplitWayEnabled = true
    override val questTypeAchievements = listOf(QuestTypeAchievement.CAR)
    override val defaultDisabledMessage: Int
        get() = if (!checkArSupport()) R.string.default_disabled_msg_no_ar else 0

    override fun getTitle(tags: Map<String, String>) = R.string.quest_road_width_title

    override fun createForm() = AddWidthForm()

    override fun applyAnswerTo(answer: WidthAnswer, tags: Tags, timestampEdited: Long) {
        tags["width"] = answer.width.toOsmValue()

        if (answer.isARMeasurement) {
            tags["source:width"] = "ARCore"
        } else {
            tags.remove("source:width")
        }

        // update width:carriageway if it is set
        if (tags.containsKey("width:carriageway")) tags["width:carriageway"] = answer.width.toOsmValue()
    }
}
