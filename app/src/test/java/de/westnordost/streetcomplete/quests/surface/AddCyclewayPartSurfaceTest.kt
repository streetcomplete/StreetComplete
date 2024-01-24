package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.SurfaceAndNote
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.quests.answerApplied
import de.westnordost.streetcomplete.quests.answerAppliedTo
import de.westnordost.streetcomplete.testutils.way
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
        assertEquals(
            setOf(StringMapEntryAdd("cycleway:surface", "asphalt")),
            questType.answerApplied(SurfaceAndNote(Surface.ASPHALT))
        )
    }

    @Test fun `surface changes when same for footway and cycleway`() {
        assertEquals(
            setOf(
                StringMapEntryModify("cycleway:surface", "paving_stones", "concrete"),
                StringMapEntryModify("surface", "paving_stones", "concrete")
            ),
            questType.answerAppliedTo(
                SurfaceAndNote(Surface.CONCRETE),
                mapOf(
                    "surface" to "paving_stones",
                    "footway:surface" to "concrete",
                    "cycleway:surface" to "paving_stones",
                )
            )
        )
    }

    private fun assertIsApplicable(vararg pairs: Pair<String, String>) {
        assertTrue(questType.isApplicableTo(way(nodes = listOf(1, 2, 3), tags = mapOf(*pairs))))
    }

    private fun assertIsNotApplicable(vararg pairs: Pair<String, String>) {
        assertFalse(questType.isApplicableTo(way(nodes = listOf(1, 2, 3), tags = mapOf(*pairs))))
    }
}
