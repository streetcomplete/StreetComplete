package de.westnordost.streetcomplete.quests.bike_parking_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.osm.Tags

class AddBikeParkingType : OsmFilterQuestType<BikeParkingType>() {

    override val elementFilter = """
        nodes, ways with
          amenity = bicycle_parking
          and access !~ private|no
          and !bicycle_parking
    """
    override val changesetComment = "Specify bicycle parking types"
    override val wikiLink = "Key:bicycle_parking"
    override val icon = R.drawable.ic_quest_bicycle_parking
    override val isDeleteElementEnabled = true
    override val achievements = listOf(BICYCLIST)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bicycle_parking_type_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes, ways with amenity = bicycle_parking")

    override fun createForm() = AddBikeParkingTypeForm()

    override fun applyAnswerTo(answer: BikeParkingType, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["bicycle_parking"] = answer.osmValue
    }
}
