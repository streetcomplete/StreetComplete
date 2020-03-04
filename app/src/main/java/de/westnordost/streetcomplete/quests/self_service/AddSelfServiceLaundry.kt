package de.westnordost.streetcomplete.quests.self_service

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddSelfServiceLaundry(o: OverpassMapDataAndGeometryDao) : SimpleOverpassQuestType<Boolean>(o) {

    override val tagFilters = "nodes, ways with shop = laundry and !self_service"
    override val commitMessage = "Add self service info"
    override val icon = R.drawable.ic_quest_laundry

    override fun getTitle(tags: Map<String, String>) = R.string.quest_laundrySelfService_title

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        changes.add("self_service", if (answer) "yes" else "no")
    }
}
