package de.westnordost.streetcomplete.quests.crossing_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.updateCheckDateForKey
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.PEDESTRIAN

class AddCrossingType : OsmFilterQuestType<CrossingType>() {

    override val elementFilter = """
        nodes with highway = crossing
          and foot != no
          and (
            !crossing
            or crossing ~ island|unknown|yes
            or (
              crossing ~ traffic_signals|uncontrolled|zebra|marked|unmarked
              and crossing older today -8 years
            )
          )
    """
    /*
       Always ask for deprecated/meaningless values (island, unknown, yes)

       Only ask again for crossing types that are known to this quest so to be conservative with
       existing data
     */

    override val commitMessage = "Add crossing type"
    override val wikiLink = "Key:crossing"
    override val icon = R.drawable.ic_quest_pedestrian_crossing

    override val questTypeAchievements = listOf(PEDESTRIAN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_crossing_type_title

    override fun createForm() = AddCrossingTypeForm()

    override fun applyAnswerTo(answer: CrossingType, changes: StringMapChangesBuilder) {
        val previous = changes.getPreviousValue("crossing")
        if(previous == "island") {
            changes.modify("crossing", answer.osmValue)
            changes.addOrModify("crossing:island", "yes")
        } else {
            if (answer == CrossingType.MARKED && previous in listOf("zebra", "marked", "uncontrolled")) {
                changes.updateCheckDateForKey("crossing")
            } else {
                changes.updateWithCheckDate("crossing", answer.osmValue)
            }
        }
    }
}
