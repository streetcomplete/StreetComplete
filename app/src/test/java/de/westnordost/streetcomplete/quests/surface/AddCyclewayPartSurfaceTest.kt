package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.quests.verifyAnswer
import de.westnordost.streetcomplete.testutils.way
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AddCyclewayPartSurfaceTest {
    private val questType = AddCyclewayPartSurface()

    @Test fun `applicable to segregated cycleway`() {
        assertIsApplicable("highway" to "cycleway", "segregated" to "yes")
        assertIsApplicable("highway" to "path", "bicycle" to "designated", "segregated" to "yes")
        assertIsApplicable("highway" to "bridleway", "bicycle" to "yes", "segregated" to "yes")
        assertIsApplicable("highway" to "footway", "bicycle" to "yes", "segregated" to "yes")
    }

    @Test fun `not applicable to non-segregated cycleway`() {
        assertIsNotApplicable("highway" to "cycleway")
        assertIsNotApplicable("highway" to "path", "bicycle" to "designated", "segregated" to "no")
    }

    @Test fun `not applicable to non-bicycle path`() {
        assertIsNotApplicable("highway" to "bridleway", "segregated" to "yes")
        assertIsNotApplicable("highway" to "footway", "segregated" to "yes")
        assertIsNotApplicable("highway" to "path", "bicycle" to "no", "segregated" to "yes")
    }

    @Test fun `not applicable to cycleway with surface`() {
        assertIsNotApplicable("highway" to "cycleway", "segregated" to "yes", "cycleway:surface" to "asphalt")
        assertIsNotApplicable("highway" to "cycleway", "segregated" to "yes", "cycleway:surface" to "paved", "cycleway:surface:note" to "it's complicated")
    }

    @Test fun `not applicable to cycleway with sidewalk`() {
        assertIsNotApplicable("highway" to "cycleway", "segregated" to "yes", "sidewalk" to "yes")
    }

    @Test fun `applicable to cycleway with unspecific surface without note`() {
        assertIsApplicable("highway" to "cycleway", "segregated" to "yes", "cycleway:surface" to "paved")
        assertIsApplicable("highway" to "path", "bicycle" to "designated", "segregated" to "yes", "cycleway:surface" to "unpaved")
    }

    @Test fun `not applicable to cycleway with unspecific surface and note`() {
        assertIsNotApplicable("highway" to "cycleway", "segregated" to "yes", "cycleway:surface" to "paved", "cycleway:surface:note" to "it's complicated")
        assertIsNotApplicable("highway" to "path", "bicycle" to "designated", "segregated" to "yes", "cycleway:surface" to "unpaved", "note:cycleway:surface" to "it's complicated")
    }

    @Test fun `not applicable to private cycleways`() {
        assertIsNotApplicable("highway" to "cycleway", "segregated" to "yes", "access" to "private")
        assertIsNotApplicable("highway" to "cycleway", "segregated" to "yes", "access" to "private", "bicycle" to "private")
    }

    @Test fun `applicable to access-restricted but cycle allowed cycleway`() {
        assertIsApplicable("highway" to "cycleway", "segregated" to "yes")
        assertIsApplicable("highway" to "cycleway", "segregated" to "yes", "bicycle" to "permissive")
        assertIsApplicable("highway" to "cycleway", "segregated" to "yes", "access" to "yes")
        assertIsApplicable("highway" to "cycleway", "segregated" to "yes", "access" to "yes", "bicycle" to "permissive")
        assertIsApplicable("highway" to "cycleway", "segregated" to "yes", "access" to "no", "bicycle" to "yes")
    }

    @Test fun `applicable to access-restricted but bicycle designated cycleway`() {
        assertIsApplicable("highway" to "bridleway", "bicycle" to "designated", "segregated" to "yes", "access" to "private")
    }

    @Test fun `applicable to old enough cycleway with surface`() {
        val way = way(1L, listOf(1, 2, 3), mapOf(
            "highway" to "cycleway",
            "segregated" to "yes",
            "cycleway:surface" to "asphalt",
            "check_date:cycleway:surface" to "2001-01-01"
        ), timestamp = nowAsEpochMilliseconds())
        val mapData = TestMapDataWithGeometry(listOf(way))

        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        assertTrue(questType.isApplicableTo(way))
    }

    @Test fun `apply asphalt surface`() {
        questType.verifyAnswer(
            SurfaceAnswer(Surface.ASPHALT),
            StringMapEntryAdd("cycleway:surface", "asphalt")
        )
    }

    @Test fun `apply generic surface`() {
        questType.verifyAnswer(
            SurfaceAnswer(Surface.UNPAVED_ROAD, "note"),
            StringMapEntryAdd("cycleway:surface", "unpaved"),
            StringMapEntryAdd("cycleway:surface:note", "note")
        )
    }

    @Test fun `updates check_date`() {
        questType.verifyAnswer(
            mapOf("cycleway:surface" to "asphalt", "check_date:cycleway:surface" to "2000-10-10"),
            SurfaceAnswer(Surface.ASPHALT),
            StringMapEntryModify("cycleway:surface", "asphalt", "asphalt"),
            StringMapEntryModify("check_date:cycleway:surface", "2000-10-10", nowAsCheckDateString()),
        )
    }

    @Test fun `smoothness tag removed when cycleway surface changes`() {
        questType.verifyAnswer(
            mapOf(
                "footway:surface" to "gravel",
                "cycleway:surface" to "asphalt",
                "cycleway:smoothness" to "intermediate"
            ),
            SurfaceAnswer(Surface.PAVING_STONES),
            StringMapEntryDelete("cycleway:smoothness", "intermediate"),
            StringMapEntryModify("cycleway:surface", "asphalt", "paving_stones")
        )
    }

    @Test fun `smoothness tag not removed when surface did not change`() {
        questType.verifyAnswer(
            mapOf(
                "footway:surface" to "paving_stones",
                "surface" to "paving_stones",
                "smoothness" to "good"
            ),
            SurfaceAnswer(Surface.PAVING_STONES),
            StringMapEntryAdd("cycleway:surface", "paving_stones"),
            StringMapEntryModify("surface", "paving_stones", "paving_stones"),
            StringMapEntryAdd("check_date:surface", nowAsCheckDateString()),
        )
    }

    @Test fun `cycleway surface changes`() {
        questType.verifyAnswer(
            mapOf("cycleway:surface" to "asphalt"),
            SurfaceAnswer(Surface.CONCRETE),
            StringMapEntryModify("cycleway:surface", "asphalt", "concrete"),
        )
    }

    @Test fun `surface changes when same for footway and cycleway`() {
        questType.verifyAnswer(
            mapOf(
                "surface" to "paving_stones",
                "cycleway:surface" to "paving_stones",
                "footway:surface" to "concrete",
            ),
            SurfaceAnswer(Surface.CONCRETE),
            StringMapEntryModify("cycleway:surface", "paving_stones", "concrete"),
            StringMapEntryModify("surface", "paving_stones", "concrete")
        )
    }

    @Test fun `surface added when same for footway and cycleway`() {
        questType.verifyAnswer(
            mapOf(
                "cycleway:surface" to "paving_stones",
                "footway:surface" to "concrete",
            ),
            SurfaceAnswer(Surface.CONCRETE),
            StringMapEntryModify("cycleway:surface", "paving_stones", "concrete"),
            StringMapEntryAdd("surface", "concrete")
        )
    }

    @Test fun `surface changes to generic paved when similar for footway and cycleway`() {
        questType.verifyAnswer(
            mapOf(
                "surface" to "paving_stones",
                "cycleway:surface" to "paving_stones",
                "footway:surface" to "asphalt",
            ),
            SurfaceAnswer(Surface.CONCRETE),
            StringMapEntryModify("cycleway:surface", "paving_stones", "concrete"),
            StringMapEntryModify("surface", "paving_stones", "paved")
        )
    }

    @Test fun `surface removed when different for footway and cycleway`() {
        questType.verifyAnswer(
            mapOf(
                "surface" to "paving_stones",
                "cycleway:surface" to "paving_stones",
                "footway:surface" to "gravel",
            ),
            SurfaceAnswer(Surface.CONCRETE),
            StringMapEntryModify("cycleway:surface", "paving_stones", "concrete"),
            StringMapEntryDelete("surface", "paving_stones")
        )
    }

    @Test fun `surface not touched on added cycleway surface when footway surface missing`() {
        questType.verifyAnswer(
            mapOf(
                "surface" to "asphalt",
            ),
            SurfaceAnswer(Surface.CONCRETE),
            StringMapEntryAdd("cycleway:surface", "concrete")
        )
    }

    @Test fun `surface not touched on modified cycleway surface when footway surface missing`() {
        questType.verifyAnswer(
            mapOf(
                "surface" to "asphalt",
                "cycleway:surface" to "paving_stones",
            ),
            SurfaceAnswer(Surface.CONCRETE),
            StringMapEntryModify("cycleway:surface", "paving_stones", "concrete")
        )
    }

    private fun assertIsApplicable(vararg pairs: Pair<String, String>) {
        assertTrue(questType.isApplicableTo(way(nodes = listOf(1, 2, 3), tags = mapOf(*pairs))))
    }

    private fun assertIsNotApplicable(vararg pairs: Pair<String, String>) {
        assertFalse(questType.isApplicableTo(way(nodes = listOf(1, 2, 3), tags = mapOf(*pairs))))
    }
}
