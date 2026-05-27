package de.westnordost.streetcomplete.quests.segregated

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.quests.answerApplied
import de.westnordost.streetcomplete.testutils.way
import kotlin.test.Test
import kotlin.test.assertEquals

class AddCyclewaySegregationTest {
    private val questType = AddCyclewaySegregation()

    @Test fun `not applicable to greengrocer shops`() {
        val mapData = TestMapDataWithGeometry(
            listOf(
                way(1, tags = mapOf("shop" to "greengrocer", "name" to "Foobar")),
            ),
        )
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `not applicable to unpaved ways`() {
        val mapData = TestMapDataWithGeometry(
            listOf(
                way(1, tags = mapOf(
                    "highway" to "path",
                    "foot" to "designated",
                    "bicycle" to "designated",
                    "surface" to "gravel",
                )
                ),
            ),
        )
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `applicable to matching paved ways`() {
        val mapData = TestMapDataWithGeometry(
            listOf(
                way(1, tags = mapOf(
                    "highway" to "path",
                    "foot" to "designated",
                    "bicycle" to "designated",
                    "surface" to "asphalt",
                )
                ),
            ),
        )
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `applicable to ways which suggest split cycling and walking`() {
        // ask about segregation if segregated=* is not tagged
        // and way has footway:surface or cycleway:surface
        // this allows to finish tagging where it is segregated (as it usual is)
        // and review weird cases for followup checks
        val mapData = TestMapDataWithGeometry(
            listOf(
                way(1, tags = mapOf(
                    "highway" to "path",
                    "footway:surface" to "asphalt",
                    "surface" to "asphalt",
                )
                ),
            ),
        )
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `not applicable to ways which ban cycling or walking`() {
        val mapData = TestMapDataWithGeometry(
            listOf(
                way(1, tags = mapOf(
                    "highway" to "path",
                    "footway:surface" to "asphalt",
                    "surface" to "asphalt",
                    "foot" to "private",
                )
                ),
            ),
        )
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `sets expected tags on answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("segregated", "yes")),
            questType.answerApplied(CyclewaySegregation.YES)
        )
        assertEquals(
            setOf(StringMapEntryAdd("segregated", "no")),
            questType.answerApplied(CyclewaySegregation.NO)
        )
        assertEquals(
            setOf(StringMapEntryAdd("sidewalk", "yes")),
            questType.answerApplied(CyclewaySegregation.SIDEWALK)
        )
    }
}
