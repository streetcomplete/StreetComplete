package de.westnordost.streetcomplete.quests.railway_crossing

import android.os.Bundle

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao

class AddRailwayCrossingBarrier(o: OverpassMapDataDao) : SimpleOverpassQuestType(o) {

    override val tagFilters = "nodes with railway=level_crossing and !crossing:barrier"
    override val commitMessage = "Add type of barrier for railway crossing"
    override val icon = R.drawable.ic_quest_railway

	override fun getTitle(tags: Map<String, String>) = R.string.quest_railway_crossing_barrier_title

	override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        val values = answer.getStringArrayList(AddRailwayCrossingBarrierForm.OSM_VALUES)
        changes.add("crossing:barrier", values!![0])
    }

    override fun createForm() = AddRailwayCrossingBarrierForm()
}
