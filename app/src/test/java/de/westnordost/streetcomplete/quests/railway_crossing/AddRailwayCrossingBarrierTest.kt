package de.westnordost.streetcomplete.quests.railway_crossing

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.quests.answerApplied
import de.westnordost.streetcomplete.quests.answerAppliedTo
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.way
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AddRailwayCrossingBarrierTest {
    private val questType = AddRailwayCrossingBarrier()

    @Test fun `not applicable to non-crossing`() {
        val node = node(tags = mapOf("plumps" to "didumps"))
        val mapData = TestMapDataWithGeometry(listOf(node))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertEquals(false, questType.isApplicableTo(node))
    }

    @Test fun `applicable to crossing`() {
        val crossing = node(1, tags = mapOf("railway" to "level_crossing"))
        val crossing2 = node(2, tags = mapOf("railway" to "level_crossing"))
        val mapData = TestMapDataWithGeometry(listOf(crossing, crossing2))
        assertEquals(2, questType.getApplicableElements(mapData).toList().size)
        assertNull(questType.isApplicableTo(crossing))
        assertNull(questType.isApplicableTo(crossing2))
    }

    @Test fun `not applicable to crossing with crossing barrier`() {
        val crossing = node(tags = mapOf("railway" to "level_crossing", "crossing:barrier" to "yes"))
        val mapData = TestMapDataWithGeometry(listOf(crossing))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertEquals(false, questType.isApplicableTo(crossing))
    }

    @Test fun `not applicable to crossing with crossing chicane`() {
        val crossing = node(tags = mapOf("railway" to "level_crossing", "crossing:chicane" to "yes"))
        val mapData = TestMapDataWithGeometry(listOf(crossing))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertEquals(false, questType.isApplicableTo(crossing))
    }

    @Test fun `not applicable to crossing with private road`() {
        val mapData = TestMapDataWithGeometry(listOf(
            node(id = 1, tags = mapOf("railway" to "level_crossing")),
            way(nodes = listOf(1, 2, 3), tags = mapOf(
                "highway" to "residential",
                "access" to "private"
            ))
        ))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `chicane answer sets barrier to no and chicane to yes`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("crossing:barrier", "no"),
                StringMapEntryAdd("crossing:chicane", "yes"),
            ),
            questType.answerApplied(RailwayCrossingBarrier.CHICANE)
        )
    }

    @Test fun `barrier answer sets barrier and deletes chicane`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("crossing:barrier", "full"),
                StringMapEntryDelete("crossing:chicane", "yes")
            ),
            questType.answerAppliedTo(
                RailwayCrossingBarrier.FULL,
                mapOf("crossing:chicane" to "yes")
            )
        )
    }
}
