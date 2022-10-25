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
        assertIsApplicable("highway" to "bridleway", "bicycle" to "yes", "segregated" to "yes")
    }

    @Test fun `not applicable to non-segregated cycleway`() {
        assertIsNotApplicable("highway" to "cycleway")
        assertIsNotApplicable("highway" to "path", "bicycle" to "designated", "segregated" to "no")
    }

    @Test fun `not applicable to non-bicycle path`() {
        assertIsNotApplicable("highway" to "bridleway", "segregated" to "yes")
        assertIsNotApplicable("highway" to "path", "bicycle" to "no", "segregated" to "yes")
    }

    @Test fun `not applicable to cycleway with surface`() {
        assertIsNotApplicable("highway" to "cycleway", "segregated" to "yes", "cycleway:surface" to "asphalt")
        assertIsNotApplicable("highway" to "cycleway", "segregated" to "yes", "cycleway:surface" to "paved")
    }

    @Test fun `applicable to cycleway with unspecific surface and note`() {
        assertIsApplicable("highway" to "cycleway", "segregated" to "yes", "cycleway:surface" to "paved", "cycleway:surface:note" to "it's complicated")
        assertIsApplicable("highway" to "path", "bicycle" to "designated", "segregated" to "yes", "cycleway:surface" to "unpaved", "note:cycleway:surface" to "it's complicated")
    }

    @Test fun `not applicable to access-restricted cycleways`() {
        assertIsNotApplicable("highway" to "cycleway", "segregated" to "yes", "bicycle" to "private")
        assertIsNotApplicable("highway" to "cycleway", "segregated" to "yes", "access" to "no")
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

    @Test fun `apply asphalt surface`() {
        questType.verifyAnswer(
            SurfaceAnswer(Surface.ASPHALT),
            StringMapEntryAdd("cycleway:surface", "asphalt")
        )
    }

    @Test fun `apply generic surface`() {
        questType.verifyAnswer(
            SurfaceAnswer(Surface.PAVED_ROAD, "note"),
            StringMapEntryAdd("cycleway:surface", "unpaved"),
            StringMapEntryAdd("cycleway:surface:note", "note")
        )
    }

    @Test fun `updates check_date`() {
        questType.verifyAnswer(
            mapOf("cycleway:surface" to "asphalt", "check_date:cycleway:surface" to "2000-10-10"),
            SurfaceAnswer(Surface.ASPHALT),
            StringMapEntryModify("cycleway:surface", "asphalt", "asphalt"),
            StringMapEntryModify("check_date:sidewalk:surface", "2000-10-10", LocalDate.now().toCheckDateString()),
        )
    }

    @Test fun `smoothness tag removed when cycleway surface changes`() {
        questType.verifyAnswer(
            mapOf("cycleway:surface" to "asphalt", "smoothness" to "excellent"),
            SurfaceAnswer(Surface.PAVING_STONES),
            StringMapEntryDelete("smoothness", "excellent"),
            StringMapEntryModify("cycleway:surface", "asphalt", "paving_stones")
        )
    }

    @Test fun `cycleway surface changes`() {
        questType.verifyAnswer(
            mapOf("cycleway:surface" to "asphalt"),
            SurfaceAnswer(Surface.CONCRETE),
            StringMapEntryModify("cycleway:surface", "asphalt", "concrete"),
        )
    }



    private fun assertIsApplicable(vararg pairs: Pair<String, String>) {
        assertTrue(questType.isApplicableTo(way(nodes = listOf(1, 2, 3), tags = mapOf(*pairs))))
    }

    private fun assertIsNotApplicable(vararg pairs: Pair<String, String>) {
        assertFalse(questType.isApplicableTo(way(nodes = listOf(1, 2, 3), tags = mapOf(*pairs))))
    }
}
