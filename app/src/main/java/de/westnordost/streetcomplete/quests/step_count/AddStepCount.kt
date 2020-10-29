package de.westnordost.streetcomplete.quests.step_count

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquest.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi

class AddStepCount(overpassApi: OverpassMapDataAndGeometryApi)
    : SimpleOverpassQuestType<Int>(overpassApi) {

    override val tagFilters = """
        ways with highway = steps
         and (!indoor or indoor = no)
         and access !~ private|no
         and (!conveying or conveying = no)
         and !step_count
    """

    override val commitMessage = "Add step count"
    override val wikiLink = "Key:step_count"
    override val icon = R.drawable.ic_quest_steps_count
    override val isSplitWayEnabled = true
    // because the user needs to start counting at the start of the steps
    override val hasMarkersAtEnds = true

    override fun getTitle(tags: Map<String, String>) = R.string.quest_step_count_title

    override fun createForm() = AddStepCountForm()

    override fun applyAnswerTo(answer: Int, changes: StringMapChangesBuilder) {
        changes.add("step_count", answer.toString())
    }
}
