package de.westnordost.streetcomplete.quests.step_count

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.osm.Tags

class AddStepCount : OsmFilterQuestType<Int>() {

    override val elementFilter = """
        ways with highway = steps
         and (!indoor or indoor = no)
         and access !~ private|no
         and (!conveying or conveying = no)
         and !step_count
    """
    override val changesetComment = "Specify step counts"
    override val wikiLink = "Key:step_count"
    override val icon = R.drawable.ic_quest_steps_count
    // because the user needs to start counting at the start of the steps
    override val hasMarkersAtEnds = true
    override val achievements = listOf(PEDESTRIAN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_step_count_title

    override fun createForm() = AddStepCountForm()

    override fun applyAnswerTo(answer: Int, tags: Tags, timestampEdited: Long) {
        tags["step_count"] = answer.toString()
    }
}
