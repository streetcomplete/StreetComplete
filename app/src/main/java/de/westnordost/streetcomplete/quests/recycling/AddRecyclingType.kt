package de.westnordost.streetcomplete.quests.recycling

import android.os.Bundle

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao

class AddRecyclingType(o: OverpassMapDataDao) : SimpleOverpassQuestType(o) {

    override val tagFilters = "nodes, ways, relations with amenity = recycling and !recycling_type"
    override val commitMessage = "Add recycling type to recycling amenity"
    override val icon = R.drawable.ic_quest_recycling

	override fun getTitle(tags: Map<String, String>) = R.string.quest_recycling_type_title

	override fun createForm() = AddRecyclingTypeForm()

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        val values = answer.getStringArrayList(AddRecyclingTypeForm.OSM_VALUES)
        val recycling = values!![0]

        when (recycling) {
            "centre" -> changes.add("recycling_type", "centre")
            "overground" -> {
                changes.add("recycling_type", "container")
                changes.add("location", "overground")
            }
            "underground" -> {
                changes.add("recycling_type", "container")
                changes.add("location", "underground")
            }
        }
    }
}
