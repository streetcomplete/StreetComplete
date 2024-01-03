package de.westnordost.streetcomplete.quests.bike_parking_capacity

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate

class AddBikeParkingCapacity : OsmFilterQuestType<Int>() {

    override val elementFilter = """
        nodes, ways with
         amenity = bicycle_parking
         and access !~ private|no
         and bicycle_parking !~ floor|informal
         and (
           !capacity
           or bicycle_parking ~ stands|wall_loops and capacity older today -4 years
         )
    """
    /* Bike capacity may change more often for stands and wheelbenders as adding or
       removing a few of them is minor work
     */

    override val changesetComment = "Specify bicycle parking capacities"
    override val wikiLink = "Tag:amenity=bicycle_parking"
    override val icon = R.drawable.ic_quest_bicycle_parking_capacity
    override val isDeleteElementEnabled = true
    override val achievements = listOf(BICYCLIST)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bikeParkingCapacity_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes, ways with amenity = bicycle_parking")

    override fun createForm() = AddBikeParkingCapacityForm.create(showClarificationText = true)

    override fun applyAnswerTo(answer: Int, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateWithCheckDate("capacity", answer.toString())
    }
}
