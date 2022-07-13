package de.westnordost.streetcomplete.quests.steps_incline

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.osm.Tags
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
    override val changesetComment = "Specify which way leads up for steps"
    override val wikiLink = "Key:incline"
    override val icon = R.drawable.ic_quest_steps
    override val achievements = listOf(PEDESTRIAN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_steps_incline_title

    override fun createForm() = AddStepsInclineForm()

    override fun applyAnswerTo(answer: StepsIncline, tags: Tags, timestampEdited: Long) {
        tags["incline"] = when (answer) {
            UP -> "up"
            UP_REVERSED -> "down"
        }
    }
}
