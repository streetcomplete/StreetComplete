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

class AddFootwayPartSurfaceTest {
    private val questType = AddFootwayPartSurface()

    @Test fun `applicable to segregated footway`() {
        assertIsApplicable("highway" to "footway", "segregated" to "yes")
        assertIsApplicable("highway" to "path", "foot" to "designated", "segregated" to "yes")
        assertIsApplicable("highway" to "path", "segregated" to "yes")
        assertIsApplicable("highway" to "bridleway", "foot" to "yes", "segregated" to "yes")
        assertIsApplicable("highway" to "cycleway", "foot" to "designated", "segregated" to "yes")
    }

    @Test fun `not applicable to non-segregated footway`() {
        assertIsNotApplicable("highway" to "footway")
        assertIsNotApplicable("highway" to "path", "foot" to "designated", "segregated" to "no")
    }

    @Test fun `not applicable to non-foot path`() {
        assertIsNotApplicable("highway" to "bridleway", "bicycle" to "designated", "segregated" to "yes")
        assertIsNotApplicable("highway" to "path", "foot" to "no", "segregated" to "yes")
        assertIsNotApplicable("highway" to "cycleway", "bicycle" to "designated", "segregated" to "yes")
    }

    @Test fun `not applicable to footway with surface`() {
        assertIsNotApplicable("highway" to "footway", "segregated" to "yes", "footway:surface" to "asphalt")
        assertIsNotApplicable("highway" to "footway", "segregated" to "yes", "footway:surface" to "paved", "footway:surface:note" to "it's complicated")
    }

    @Test fun `not applicable to footway with sidewalk`() {
        assertIsNotApplicable("highway" to "footway", "segregated" to "yes", "sidewalk" to "yes")
    }

    @Test fun `applicable to footway with unspecific surface without note`() {
        assertIsApplicable("highway" to "footway", "segregated" to "yes", "footway:surface" to "paved")
        assertIsApplicable("highway" to "path", "foot" to "designated", "segregated" to "yes", "footway:surface" to "unpaved")
    }

    @Test fun `not applicable to footway with unspecific surface and note`() {
        assertIsNotApplicable("highway" to "footway", "segregated" to "yes", "footway:surface" to "paved", "footway:surface:note" to "it's complicated")
        assertIsNotApplicable("highway" to "path", "foot" to "designated", "segregated" to "yes", "footway:surface" to "unpaved", "note:footway:surface" to "it's complicated")
    }

    @Test fun `not applicable to private footways`() {
        assertIsNotApplicable("highway" to "path", "segregated" to "yes", "access" to "private")
        assertIsNotApplicable("highway" to "path", "segregated" to "yes", "access" to "private", "foot" to "private")
    }

    @Test fun `applicable to access-restricted but foot allowed path`() {
        assertIsApplicable("highway" to "path", "segregated" to "yes")
        assertIsApplicable("highway" to "path", "segregated" to "yes", "foot" to "permissive")
        assertIsApplicable("highway" to "path", "segregated" to "yes", "access" to "yes")
        assertIsApplicable("highway" to "path", "segregated" to "yes", "access" to "yes", "foot" to "permissive")
        assertIsApplicable("highway" to "path", "segregated" to "yes", "access" to "no", "foot" to "yes")
    }

    @Test fun `applicable to access-restricted but foot designated footway`() {
        assertIsApplicable("highway" to "bridleway", "foot" to "designated", "segregated" to "yes", "access" to "private")
    }

    @Test fun `applicable to old enough footway with surface`() {
        val way = way(1L, listOf(1, 2, 3), mapOf(
            "highway" to "footway",
            "segregated" to "yes",
            "footway:surface" to "asphalt",
            "check_date:footway:surface" to "2001-01-01"
        ), timestamp = nowAsEpochMilliseconds())
        val mapData = TestMapDataWithGeometry(listOf(way))

        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        assertTrue(questType.isApplicableTo(way))
    }

    @Test fun `apply asphalt surface`() {
        assertEquals(
            setOf(StringMapEntryAdd("footway:surface", "asphalt")),
            questType.answerApplied(SurfaceAndNote(Surface.ASPHALT))
        )
    }

    @Test fun `surface changes when same for footway and cycleway`() {
        assertEquals(
            setOf(
                StringMapEntryModify("footway:surface", "paving_stones", "concrete"),
                StringMapEntryModify("surface", "paving_stones", "concrete")
            ),
            questType.answerAppliedTo(
                SurfaceAndNote(Surface.CONCRETE),
                mapOf(
                    "surface" to "paving_stones",
                    "footway:surface" to "paving_stones",
                    "cycleway:surface" to "concrete",
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
