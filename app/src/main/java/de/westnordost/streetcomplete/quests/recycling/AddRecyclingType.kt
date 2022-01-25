package de.westnordost.streetcomplete.quests.recycling

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.quests.recycling.RecyclingType.*


class AddRecyclingType : OsmFilterQuestType<RecyclingType>() {

    override val elementFilter = "nodes, ways, relations with amenity = recycling and !recycling_type"
    override val changesetComment = "Add recycling type to recycling amenity"
    override val wikiLink = "Key:recycling_type"
    override val icon = R.drawable.ic_quest_recycling
    override val isDeleteElementEnabled = true

    override val questTypeAchievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_recycling_type_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with amenity = recycling")

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
