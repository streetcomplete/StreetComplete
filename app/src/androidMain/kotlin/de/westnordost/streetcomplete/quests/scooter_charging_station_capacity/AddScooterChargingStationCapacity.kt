package de.westnordost.streetcomplete.quests.scooter_charging_station_capacity

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate

class AddScooterChargingStationCapacity : OsmFilterQuestType<Int>(), AndroidQuest {

    override val elementFilter = """
        nodes, ways with
          amenity = charging_station
          and scooter ~ yes|designated
          and access !~ private|no
          and (
           !capacity
           or capacity older today -8 years
         )
    """
    override val changesetComment = "Specify scooter charging stations capacities"
    override val wikiLink = "Tag:amenity=charging_station"
    override val icon = R.drawable.ic_quest_car_charger_capacity // using the car charger icon because the logo also works for scooters
    override val isDeleteElementEnabled = true
    override val achievements = listOf(BICYCLIST) // no scooter achievement

    override fun getTitle(tags: Map<String, String>) = R.string.quest_scooter_charging_station_capacity_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes, ways with amenity = charging_station and scooter ~ yes|designated")

    override fun createForm() = AddScooterChargingStationCapacityForm()

    override fun applyAnswerTo(answer: Int, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateWithCheckDate("capacity", answer.toString())
    }
}
