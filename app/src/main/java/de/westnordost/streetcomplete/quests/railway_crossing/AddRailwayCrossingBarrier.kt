package de.westnordost.streetcomplete.quests.railway_crossing

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao

class AddRailwayCrossingBarrier(o: OverpassMapDataDao) : SimpleOverpassQuestType<String>(o) {

    override val tagFilters = "nodes with railway=level_crossing and !crossing:barrier"
    override val commitMessage = "Add type of barrier for railway crossing"
    override val icon = R.drawable.ic_quest_railway

    override fun getTitle(tags: Map<String, String>) = R.string.quest_railway_crossing_barrier_title

    override fun createForm() = AddRailwayCrossingBarrierForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.add("crossing:barrier", answer)
    }
}
