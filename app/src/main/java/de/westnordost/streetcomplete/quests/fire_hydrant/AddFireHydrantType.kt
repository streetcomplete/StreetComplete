package de.westnordost.streetcomplete.quests.fire_hydrant

import android.os.Bundle

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment

class AddFireHydrantType(o: OverpassMapDataDao) : SimpleOverpassQuestType(o) {

    override val tagFilters = "nodes with emergency=fire_hydrant and !fire_hydrant:type"
    override val commitMessage = "Add fire hydrant type"
    override val icon = R.drawable.ic_quest_fire_hydrant

    override fun getTitle(tags: Map<String, String>) = R.string.quest_fireHydrant_type_title

    override fun createForm() = AddFireHydrantTypeForm()

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        changes.add("fire_hydrant:type",  answer.getStringArrayList(ImageListQuestAnswerFragment.OSM_VALUES)!![0])
    }
}
