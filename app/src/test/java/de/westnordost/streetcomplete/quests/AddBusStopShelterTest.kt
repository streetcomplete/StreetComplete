package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.meta.toCheckDateString
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryDelete
import de.westnordost.streetcomplete.quests.bus_stop_shelter.AddBusStopShelter
import de.westnordost.streetcomplete.quests.bus_stop_shelter.BusStopShelterAnswer
import org.junit.Test
import java.util.*

class AddBusStopShelterTest {

    private val questType = AddBusStopShelter()

    @Test fun `apply shelter yes answer`() {
        questType.verifyAnswer(
            BusStopShelterAnswer.SHELTER,
            StringMapEntryAdd("shelter","yes")
        )
    }

    @Test fun `apply shelter yes again answer`() {
        questType.verifyAnswer(
            mapOf("shelter" to "yes"),
            BusStopShelterAnswer.SHELTER,
            StringMapEntryAdd("check_date:shelter", Date().toCheckDateString())
        )
    }

    @Test fun `apply shelter no answer`() {
        questType.verifyAnswer(
            BusStopShelterAnswer.NO_SHELTER,
            StringMapEntryAdd("shelter","no")
        )
    }

    @Test fun `apply shelter no again answer`() {
        questType.verifyAnswer(
            mapOf("shelter" to "no"),
            BusStopShelterAnswer.NO_SHELTER,
            StringMapEntryAdd("check_date:shelter", Date().toCheckDateString())
        )
    }

    @Test fun `apply covered answer`() {
        questType.verifyAnswer(
            BusStopShelterAnswer.COVERED,
            StringMapEntryAdd("covered", "yes")
        )
    }

    @Test fun `apply covered when answer before was shelter answer`() {
        questType.verifyAnswer(
            mapOf("shelter" to "no"),
            BusStopShelterAnswer.COVERED,
            StringMapEntryAdd("covered", "yes"),
            StringMapEntryDelete("shelter", "no")
        )
    }
}
