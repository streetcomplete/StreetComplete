package de.westnordost.streetcomplete.quests.charging_station_capacity

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder

class AddChargingStationCapacity : OsmFilterQuestType<Int>() {

    override val elementFilter = """
        nodes, ways with
          amenity = charging_station
          and !capacity
          and bicycle != yes and scooter != yes
    """
    override val commitMessage = "Add charging station capacities"
    override val wikiLink = "Tag:amenity=charging_station"
    override val icon = R.drawable.ic_quest_car_charger_capacity

    override fun getTitle(tags: Map<String, String>) = R.string.quest_charging_station_capacity_title

    override fun createForm() = AddChargingStationCapacityForm()

    override fun applyAnswerTo(answer: Int, changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate("capacity", answer.toString())
    }
}
