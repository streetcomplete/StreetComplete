package de.westnordost.streetcomplete.quests.charging_station_capacity

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

class AddBikeChargingStationCapacity : OsmFilterQuestType<Int>(), AndroidQuest {

    override val elementFilter = """
        nodes, ways with
          amenity = charging_station
          and bicycle ~ yes|designated
          and access !~ private|no
          and (
           or !capacity:bicycle
           or capacity:bicycle older today -8 years
         )
    """
    override val changesetComment = "Specify bicycle charging stations capacities"
    override val wikiLink = "Tag:amenity=charging_station"
    override val icon = R.drawable.ic_quest_bicycle_charger_capacity
    override val isDeleteElementEnabled = true
    override val achievements = listOf(BICYCLIST)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bicycle_charging_station_capacity_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes, ways with amenity = charging_station and bicycle ~ yes|designated")

    override fun createForm() = AddBikeChargingStationCapacityForm()

    override fun applyAnswerTo(answer: Int, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateWithCheckDate("capacity:bicycle", answer.toString())
    }
}
