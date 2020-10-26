package de.westnordost.streetcomplete.quests.bus_stop_shelter

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.quests.bus_stop_shelter.BusStopShelterAnswer.*

class AddBusStopShelter : OsmFilterQuestType<BusStopShelterAnswer>() {

    override val elementFilter = """
        nodes with 
        (
          (public_transport = platform and ~bus|trolleybus|tram ~ yes)
          or
          (highway = bus_stop and public_transport != stop_position)
        )
        and physically_present != no and naptan:BusStopType != HAR
        and !covered and (!shelter or shelter older today -4 years)
    """
    /* Not asking again if it is covered because it means the stop itself is under a large
       building or roof building so this won't usually change */

    override val commitMessage = "Add bus stop shelter"
    override val wikiLink = "Key:shelter"
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
            SHELTER -> changes.updateWithCheckDate("shelter", "yes")
            NO_SHELTER -> changes.updateWithCheckDate("shelter", "no")
            COVERED -> {
                changes.deleteIfExists("shelter")
                changes.add("covered", "yes")
            }
        }
    }
}

