package de.westnordost.streetcomplete.quests.baby_changing_table

import android.os.Bundle

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddBabyChangingTable(o: OverpassMapDataDao) : SimpleOverpassQuestType(o) {

    override val tagFilters = """
        nodes, ways with (((amenity ~ restaurant|cafe|fuel|fast_food or shop ~ mall|department_store)
        and name and toilets=yes) or amenity=toilets) and !diaper
    """
    override val commitMessage = "Add baby changing table"
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside
    override val icon = R.drawable.ic_quest_baby

    override fun getTitle(tags: Map<String, String>) =
        if (tags.containsKey("name"))
            R.string.quest_baby_changing_table_title
        else
            R.string.quest_baby_changing_table_toilets_title

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        val yesno = if (answer.getBoolean(YesNoQuestAnswerFragment.ANSWER)) "yes" else "no"
        changes.add("diaper", yesno)
    }
}
