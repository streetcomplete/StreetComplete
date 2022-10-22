package de.westnordost.streetcomplete.quests.motorcycle_parking_capacity

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate

class AddMotorcycleParkingCapacity : OsmFilterQuestType<Int>() {

    override val elementFilter = """
        nodes, ways with amenity = motorcycle_parking
         and access !~ private|no
         and (!capacity or capacity older today -4 years)
    """
    override val changesetComment = "Specify motorcycle parking capacities"
    override val wikiLink = "Tag:amenity=motorcycle_parking"
    override val icon = R.drawable.ic_quest_motorcycle_parking_capacity
    override val isDeleteElementEnabled = true
    override val achievements = listOf(CAR)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_motorcycleParkingCapacity_title

    override fun createForm() = AddMotorcycleParkingCapacityForm()

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes, ways with amenity = motorcycle_parking")

    override fun applyAnswerTo(answer: Int, tags: Tags, timestampEdited: Long) {
        tags.updateWithCheckDate("capacity", answer.toString())
    }
}
