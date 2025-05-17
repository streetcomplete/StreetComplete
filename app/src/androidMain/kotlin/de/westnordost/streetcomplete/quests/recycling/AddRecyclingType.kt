package de.westnordost.streetcomplete.quests.recycling

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.recycling.RecyclingType.OVERGROUND_CONTAINER
import de.westnordost.streetcomplete.quests.recycling.RecyclingType.RECYCLING_CENTRE
import de.westnordost.streetcomplete.quests.recycling.RecyclingType.UNDERGROUND_CONTAINER

class AddRecyclingType : OsmFilterQuestType<RecyclingType>() {

    override val elementFilter = "nodes, ways with amenity = recycling and !recycling_type"
    override val changesetComment = "Specify type of recycling amenities"
    override val wikiLink = "Key:recycling_type"
    override val icon = R.drawable.ic_quest_recycling
    override val isDeleteElementEnabled = true
    override val achievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_recycling_type_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes, ways with amenity ~ recycling|waste_disposal|waste_basket")

    override fun createForm() = AddRecyclingTypeForm()

    override fun applyAnswerTo(answer: RecyclingType, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            RECYCLING_CENTRE -> {
                tags["recycling_type"] = "centre"
            }
            OVERGROUND_CONTAINER -> {
                tags["recycling_type"] = "container"
                tags["location"] = "overground"
            }
            UNDERGROUND_CONTAINER -> {
                tags["recycling_type"] = "container"
                tags["location"] = "underground"
            }
        }
    }
}
