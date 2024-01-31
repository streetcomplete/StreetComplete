package de.westnordost.streetcomplete.quests.crossing_type

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import de.westnordost.streetcomplete.quests.answerApplied
import de.westnordost.streetcomplete.quests.answerAppliedTo
import de.westnordost.streetcomplete.quests.crossing_type.CrossingType.MARKED
import de.westnordost.streetcomplete.quests.crossing_type.CrossingType.TRAFFIC_SIGNALS
import kotlin.test.Test
import kotlin.test.assertEquals

class AddCrossingTypeTest {

    private val questType = AddCrossingType()

    @Test fun `apply normal answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("crossing", "traffic_signals")),
            questType.answerApplied(TRAFFIC_SIGNALS)
        )
    }

    @Test fun `apply answer for crossing = island`() {
        assertEquals(
            setOf(
                StringMapEntryModify("crossing", "island", "traffic_signals"),
                StringMapEntryAdd("crossing:island", "yes")
            ),
            questType.answerAppliedTo(TRAFFIC_SIGNALS, mapOf("crossing" to "island"))
        )
    }

    @Test fun `apply answer for crossing = island and crossing_island set`() {
        assertEquals(
            setOf(
                StringMapEntryModify("crossing", "island", "traffic_signals"),
                StringMapEntryModify("crossing:island", "something", "yes")
            ),
            questType.answerAppliedTo(
                TRAFFIC_SIGNALS,
                mapOf("crossing" to "island", "crossing:island" to "something")
            )
        )
    }

    @Test fun `apply marked answer does not change the type of marked value`() {
        assertEquals(
            setOf(StringMapEntryAdd("check_date:crossing", nowAsCheckDateString())),
            questType.answerAppliedTo(MARKED, mapOf("crossing" to "zebra"))
        )

        assertEquals(
            setOf(StringMapEntryAdd("check_date:crossing", nowAsCheckDateString())),
            questType.answerAppliedTo(MARKED, mapOf("crossing" to "marked"))
        )

        assertEquals(
            setOf(StringMapEntryAdd("check_date:crossing", nowAsCheckDateString())),
            questType.answerAppliedTo(MARKED, mapOf("crossing" to "uncontrolled"))
        )

        assertEquals(
            setOf(StringMapEntryModify("crossing", "unmarked", "marked")),
            questType.answerAppliedTo(MARKED, mapOf("crossing" to "unmarked"))
        )
    }
}
