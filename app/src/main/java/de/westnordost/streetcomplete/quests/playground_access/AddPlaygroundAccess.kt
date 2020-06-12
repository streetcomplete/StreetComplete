package de.westnordost.streetcomplete.quests.playground_access

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquest.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddPlaygroundAccess(o: OverpassMapDataAndGeometryApi) : SimpleOverpassQuestType<Boolean>(o) {

    override val tagFilters = "nodes, ways, relations with leisure = playground and (!access or access = unknown)"
    override val commitMessage = "Add playground access"
    override val wikiLink = "Tag:leisure=playground"
    override val icon = R.drawable.ic_quest_playground

    override fun getTitle(tags: Map<String, String>) = R.string.quest_playground_access_title

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        changes.add("access", if (answer) "yes" else "private")
    }
}
