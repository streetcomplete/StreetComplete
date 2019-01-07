package de.westnordost.streetcomplete.quests.playground_access

import android.os.Bundle

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddPlaygroundAccess(o: OverpassMapDataDao) : SimpleOverpassQuestType(o) {

    override val tagFilters = "nodes, ways, relations with leisure=playground and (!access or access=unknown)"
    override val commitMessage = "Add playground access"
    override val icon = R.drawable.ic_quest_playground

    override fun getTitle(tags: Map<String, String>) = R.string.quest_playground_access_title

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        val yesprivate = if (answer.getBoolean(YesNoQuestAnswerFragment.ANSWER)) "yes" else "private"
        changes.add("access", yesprivate)
    }
}
