package de.westnordost.streetcomplete.quests.parking_capacity

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate

class AddParkingCapacity : OsmFilterQuestType<Int>() {

    override val elementFilter = """
        nodes, ways with
         amenity = parking
         and parking = surface
         and access !~ private|no
         and !capacity
    """

    override val changesetComment = "Specify parking capacities"
    override val wikiLink = "Tag:amenity=parking"
    override val icon = R.drawable.ic_quest_parking_capacity
    override val achievements = listOf(CAR)
    override val defaultDisabledMessage = R.string.default_disabled_msg_ee

    override fun getTitle(tags: Map<String, String>) = R.string.quest_parking_capacity_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes, ways with amenity = parking")

    override fun createForm() = AddParkingCapacityForm()

    override fun applyAnswerTo(answer: Int, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateWithCheckDate("capacity", answer.toString())
    }
}
