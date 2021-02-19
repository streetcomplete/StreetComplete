package de.westnordost.streetcomplete.quests.charging_station_operator

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType
import de.westnordost.streetcomplete.ktx.containsAnyKey

class AddChargingStationOperator : OsmFilterQuestType<String>() {

    override val elementFilter = "nodes with amenity = charging_station and !operator and !name and !brand"
    override val commitMessage = "Add charging station operator"
    override val wikiLink = "Tag:amenity=charging_station"
    override val icon = R.drawable.ic_quest_car_charger
    override val isDeleteElementEnabled = true

    override fun getTitle(tags: Map<String, String>): Int {
        val hasName = tags.containsAnyKey("name", "brand")
        return if (hasName) R.string.quest_charging_station_name_operator_title
        else R.string.quest_charging_station_operator_title
    }

    override fun createForm() = AddChargingStationOperatorForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.add("operator", answer)
    }
}
