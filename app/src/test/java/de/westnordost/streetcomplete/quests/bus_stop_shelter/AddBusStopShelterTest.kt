package de.westnordost.streetcomplete.quests.bus_stop_shelter

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import de.westnordost.streetcomplete.quests.verifyAnswer
import org.junit.Test

class AddBusStopShelterTest {

    private val questType = AddBusStopShelter()

    @Test fun `apply shelter yes answer`() {
        questType.verifyAnswer(
            BusStopShelterAnswer.SHELTER,
            StringMapEntryAdd("shelter", "yes")
        )
    }

    @Test fun `apply shelter yes again answer`() {
        questType.verifyAnswer(
            mapOf("shelter" to "yes"),
            BusStopShelterAnswer.SHELTER,
            StringMapEntryModify("shelter", "yes", "yes"),
            StringMapEntryAdd("check_date:shelter", nowAsCheckDateString())
        )
    }

    @Test fun `apply shelter no answer`() {
        questType.verifyAnswer(
            BusStopShelterAnswer.NO_SHELTER,
            StringMapEntryAdd("shelter", "no")
        )
    }

    @Test fun `apply shelter no again answer`() {
        questType.verifyAnswer(
            mapOf("shelter" to "no"),
            BusStopShelterAnswer.NO_SHELTER,
            StringMapEntryModify("shelter", "no", "no"),
            StringMapEntryAdd("check_date:shelter", nowAsCheckDateString())
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
