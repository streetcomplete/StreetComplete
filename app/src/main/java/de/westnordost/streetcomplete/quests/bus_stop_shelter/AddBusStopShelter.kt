package de.westnordost.streetcomplete.quests.bus_stop_shelter

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao
import de.westnordost.streetcomplete.quests.bus_stop_shelter.BusStopShelterAnswer.*

class AddBusStopShelter(o: OverpassMapDataAndGeometryDao) : SimpleOverpassQuestType<BusStopShelterAnswer>(o) {

    override val tagFilters = """
        nodes with 
        (
          (public_transport = platform and ~bus|trolleybus|tram ~ yes)
          or
          (highway = bus_stop and public_transport != stop_position)
        )
        and !shelter and !covered
    """
    override val commitMessage = "Add bus stop shelter"
    override val icon = R.drawable.ic_quest_bus_stop_shelter

    override fun getTitle(tags: Map<String, String>): Int {
        val hasName = tags.containsKey("name")
        val isTram = tags["tram"] == "yes"
        return if (isTram) {
            if (hasName) R.string.quest_busStopShelter_tram_name_title
            else         R.string.quest_busStopShelter_tram_title
        } else {
            if (hasName) R.string.quest_busStopShelter_name_title
            else         R.string.quest_busStopShelter_title
        }
    }

    override fun createForm() = AddBusStopShelterForm()

    override fun applyAnswerTo(answer: BusStopShelterAnswer, changes: StringMapChangesBuilder) {
        when(answer) {
            SHELTER -> changes.add("shelter", "yes")
            NO_SHELTER -> changes.add("shelter", "no")
            COVERED -> changes.add("covered", "yes")
        }
    }
}

