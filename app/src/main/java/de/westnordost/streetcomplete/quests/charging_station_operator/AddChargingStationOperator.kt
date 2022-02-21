package de.westnordost.streetcomplete.quests.charging_station_operator

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CAR

class AddChargingStationOperator : OsmFilterQuestType<String>() {

    override val elementFilter = """
        nodes, ways with
          amenity = charging_station
          and !operator and !name and !brand
    """
    override val changesetComment = "Add charging station operator"
    override val wikiLink = "Tag:amenity=charging_station"
    override val icon = R.drawable.ic_quest_car_charger
    override val isDeleteElementEnabled = true

    override val questTypeAchievements = listOf(CAR)

    override fun getTitle(tags: Map<String, String>): Int = R.string.quest_charging_station_operator_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes, ways with amenity = charging_station")

    override fun createForm() = AddChargingStationOperatorForm()

    override fun applyAnswerTo(answer: String, tags: Tags, timestampEdited: Long) {
        tags["operator"] = answer
    }
}
