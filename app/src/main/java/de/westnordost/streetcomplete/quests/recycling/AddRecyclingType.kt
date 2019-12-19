package de.westnordost.streetcomplete.quests.recycling

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao
import de.westnordost.streetcomplete.quests.recycling.RecyclingType.*


class AddRecyclingType(o: OverpassMapDataAndGeometryDao) : SimpleOverpassQuestType<RecyclingType>(o) {

    override val tagFilters = "nodes, ways, relations with amenity = recycling and !recycling_type"
    override val commitMessage = "Add recycling type to recycling amenity"
    override val icon = R.drawable.ic_quest_recycling

    override fun getTitle(tags: Map<String, String>) = R.string.quest_recycling_type_title

    override fun createForm() = AddRecyclingTypeForm()

    override fun applyAnswerTo(answer: RecyclingType, changes: StringMapChangesBuilder) {
        when (answer) {
            RECYCLING_CENTRE -> changes.add("recycling_type", "centre")
            OVERGROUND_CONTAINER -> {
                changes.add("recycling_type", "container")
                changes.add("location", "overground")
            }
            UNDERGROUND_CONTAINER -> {
                changes.add("recycling_type", "container")
                changes.add("location", "underground")
            }
        }
    }
}
