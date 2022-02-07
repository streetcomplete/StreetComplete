package de.westnordost.streetcomplete.quests.steps_incline

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.quests.steps_incline.StepsIncline.UP
import de.westnordost.streetcomplete.quests.steps_incline.StepsIncline.UP_REVERSED

class AddStepsIncline : OsmFilterQuestType<StepsIncline>() {

    override val elementFilter = """
        ways with highway = steps
         and (!indoor or indoor = no)
         and area != yes
         and access !~ private|no
         and !incline
    """

    override val changesetComment = "Add which way leads up for these steps"
    override val wikiLink = "Key:incline"
    override val icon = R.drawable.ic_quest_steps
    override val isSplitWayEnabled = true

    override val questTypeAchievements = listOf(PEDESTRIAN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_steps_incline_title

    override fun createForm() = AddStepsInclineForm()

    override fun applyAnswerTo(answer: StepsIncline, tags: Tags, timestampEdited: Long) {
        tags["incline"] = when (answer) {
            UP -> "up"
            UP_REVERSED -> "down"
        }
    }
}
