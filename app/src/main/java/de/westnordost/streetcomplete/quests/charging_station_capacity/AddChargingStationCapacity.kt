package de.westnordost.streetcomplete.quests.charging_station_capacity

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.ktx.containsAnyKey
import de.westnordost.streetcomplete.ktx.asSingleArray

class AddChargingStationCapacity : OsmFilterQuestType<Int>() {

    override val elementFilter = """
        nodes, ways with
          amenity = charging_station
          and !capacity
          and bicycle != yes and scooter != yes and motorcar != no
    """
    override val commitMessage = "Add charging station capacities"
    override val wikiLink = "Tag:amenity=charging_station"
    override val icon = R.drawable.ic_quest_car_charger_capacity
    override val isDeleteElementEnabled = true

    override fun getTitle(tags: Map<String, String>): Int {
        val hasName = tags.containsAnyKey("name", "brand", "operator")
        return if (hasName) R.string.quest_charging_station_name_capacity_title
        else R.string.quest_charging_station_capacity_title
    }

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> =
        (tags["name"] ?: tags["brand"] ?: tags["operator"]).asSingleArray()

    override fun createForm() = AddChargingStationCapacityForm()

    override fun applyAnswerTo(answer: Int, changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate("capacity", answer.toString())
    }
}
