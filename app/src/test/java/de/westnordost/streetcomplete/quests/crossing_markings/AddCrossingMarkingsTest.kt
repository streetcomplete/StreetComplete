package de.westnordost.streetcomplete.quests.crossing_markings

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.quests.answerApplied
import de.westnordost.streetcomplete.quests.answerAppliedTo
import de.westnordost.streetcomplete.quests.crossing_island.AddCrossingIsland
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.way
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AddCrossingMarkingsTest {
    private val questType = AddCrossingMarkings()

    @Test fun `apply answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("crossing:markings", "yes")),
            questType.answerApplied(true)
        )
        assertEquals(
            setOf(StringMapEntryAdd("crossing:markings", "no")),
            questType.answerApplied(false)
        )
    }

    @Test fun `apply answer when nothing changed adds check date`() {
        assertEquals(
            setOf(
                StringMapEntryModify("crossing:markings", "yes", "yes"),
                StringMapEntryAdd("check_date:crossing:markings", nowAsCheckDateString()),
            ),
            questType.answerAppliedTo(true, mapOf("crossing:markings" to "yes"))
        )
        assertEquals(
            setOf(
                StringMapEntryModify("crossing:markings", "no", "no"),
                StringMapEntryAdd("check_date:crossing:markings", nowAsCheckDateString()),
            ),
            questType.answerAppliedTo(false, mapOf("crossing:markings" to "no"))
        )
    }

    @Test fun `apply when there is a more specific crossing markings value`() {
        assertEquals(
            setOf(StringMapEntryAdd("check_date:crossing:markings", nowAsCheckDateString())),
            questType.answerAppliedTo(true, mapOf("crossing:markings" to "squiggly lines"))
        )
        assertEquals(
            setOf(StringMapEntryModify("crossing:markings", "squiggly lines", "no")),
            questType.answerAppliedTo(false, mapOf("crossing:markings" to "squiggly lines"))
        )
    }

    @Test fun `apply when there is a crossing value`() {
        assertEquals(
            setOf(StringMapEntryAdd("crossing:markings", "yes")),
            questType.answerAppliedTo(true, mapOf("crossing" to "zebra"))
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("crossing:markings", "no"),
                StringMapEntryDelete("crossing", "marked"),
            ),
            questType.answerAppliedTo(false, mapOf("crossing" to "marked"))
        )

        assertEquals(
            setOf(StringMapEntryAdd("crossing:markings", "no"),),
            questType.answerAppliedTo(false, mapOf("crossing" to "unmarked"))
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("crossing:markings", "yes"),
                StringMapEntryDelete("crossing", "unmarked"),
            ),
            questType.answerAppliedTo(true, mapOf("crossing" to "unmarked"))
        )
    }
}
