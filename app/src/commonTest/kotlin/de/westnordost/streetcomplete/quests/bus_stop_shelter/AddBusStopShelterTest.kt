package de.westnordost.streetcomplete.quests.bus_stop_shelter

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import de.westnordost.streetcomplete.quests.answerApplied
import de.westnordost.streetcomplete.quests.answerAppliedTo
import kotlin.test.Test
import kotlin.test.assertEquals

class AddBusStopShelterTest {

    private val questType = AddBusStopShelter()

    @Test fun `apply shelter yes answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("shelter", "yes"),
                StringMapEntryAdd("check_date:shelter", nowAsCheckDateString())
            ),
            questType.answerApplied(BusStopShelterAnswer.SHELTER)
        )
    }

    @Test fun `apply shelter no answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("shelter", "no"),
                StringMapEntryAdd("check_date:shelter", nowAsCheckDateString())
            ),
            questType.answerApplied(BusStopShelterAnswer.NO_SHELTER)
        )
    }

    @Test fun `apply covered answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("covered", "yes")),
            questType.answerApplied(BusStopShelterAnswer.COVERED)
        )
    }

    @Test fun `apply covered when answer before was shelter answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("covered", "yes"),
                StringMapEntryDelete("shelter", "no")
            ),
            questType.answerAppliedTo(BusStopShelterAnswer.COVERED, mapOf("shelter" to "no"))
        )
    }
}
