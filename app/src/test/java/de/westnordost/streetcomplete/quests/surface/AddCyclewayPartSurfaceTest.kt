package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.toCheckDateString
import de.westnordost.streetcomplete.quests.verifyAnswer
import de.westnordost.streetcomplete.testutils.way
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class AddCyclewayPartSurfaceTest {
    private val questType = AddCyclewayPartSurface()

    @Test fun `applicable to segregated cycleway`() {
        assertIsApplicable("highway" to "cycleway", "segregated" to "yes")
        assertIsApplicable("highway" to "path", "bicycle" to "designated", "segregated" to "yes")
    }

    @Test fun `not applicable to non-segregated cycleway`() {
        assertIsNotApplicable("highway" to "cycleway")
        assertIsNotApplicable("highway" to "path", "bicycle" to "designated", "segregated" to "no")
    }

    @Test fun `not applicable to cycleway with surface`() {
        assertIsNotApplicable("highway" to "cycleway", "segregated" to "yes", "cycleway:surface" to "asphalt")
        assertIsNotApplicable("highway" to "cycleway", "segregated" to "yes", "cycleway:surface" to "paved")
    }

    @Test fun `applicable to cycleway with unspecific surface and note`() {
        assertIsApplicable("highway" to "cycleway", "segregated" to "yes", "cycleway:surface" to "paved", "cycleway:surface:note" to "it's complicated")
    }

    @Test fun `not applicable to access-restricted cycleways`() {
        assertIsNotApplicable("highway" to "cycleway", "segregated" to "yes", "bicycle" to "no")
        assertIsNotApplicable("highway" to "cycleway", "segregated" to "yes", "access" to "private")
    }

    @Test fun `applicable to old enough cycleway with surface`() {
        val way = way(1L, listOf(1, 2, 3), mapOf(
            "highway" to "cycleway",
            "segregated" to "yes",
            "cycleway:surface" to "asphalt",
            "check_date:cycleway:surface" to "2001-01-01"
        ), timestamp = Instant.now().toEpochMilli())
        val mapData = TestMapDataWithGeometry(listOf(way))

        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        assertTrue(questType.isApplicableTo(way)!!)
    }


    private fun assertIsApplicable(vararg pairs: Pair<String, String>) {
        assertTrue(questType.isApplicableTo(way(nodes = listOf(1, 2, 3), tags = mapOf(*pairs))))
    }

    private fun assertIsNotApplicable(vararg pairs: Pair<String, String>) {
        assertFalse(questType.isApplicableTo(way(nodes = listOf(1, 2, 3), tags = mapOf(*pairs))))
    }
}
