package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.SurfaceAndNote
import de.westnordost.streetcomplete.quests.verifyAnswer
import de.westnordost.streetcomplete.testutils.way
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AddRoadSurfaceTest {
    private val questType = AddRoadSurface()

    @Test fun `not applicable to tagged surface`() {
        assertIsNotApplicable("highway" to "residential", "surface" to "asphalt")
    }

    @Test fun `not applicable to tagged surface:lanes`() {
        assertIsNotApplicable("highway" to "residential", "surface:lanes" to "concrete|asphalt|asphalt")
    }

    @Test fun `applicable to untagged surface`() {
        assertIsApplicable("highway" to "residential")
    }

    @Test fun `applicable where poor tracktype conflicts with paved surface`() {
        assertIsApplicable("highway" to "track", "surface" to "asphalt", "tracktype" to "grade5")
    }

    @Test fun `applicable where high quality tracktype conflicts with poor surface`() {
        assertIsApplicable("highway" to "track", "surface" to "sand", "tracktype" to "grade1")
    }

    @Test fun `not applicable to tagged surface with fitting tracktype`() {
        assertIsNotApplicable("highway" to "track", "surface" to "asphalt", "tracktype" to "grade1")
    }

    @Test fun `applicable to surface tags not providing proper info`() {
        assertIsApplicable("highway" to "residential", "surface" to "paved")
        assertIsNotApplicable("highway" to "residential", "surface" to "paved", "surface:note" to "wildly mixed asphalt, concrete, paving stones and sett")
        assertIsApplicable("highway" to "residential", "surface" to "cobblestone")
        assertIsApplicable("highway" to "residential", "surface" to "cement")
    }

    private fun assertIsApplicable(vararg pairs: Pair<String, String>) {
        assertTrue(questType.isApplicableTo(way(nodes = listOf(1, 2, 3), tags = mapOf(*pairs))))
    }

    private fun assertIsNotApplicable(vararg pairs: Pair<String, String>) {
        assertFalse(questType.isApplicableTo(way(nodes = listOf(1, 2, 3), tags = mapOf(*pairs))))
    }

    @Test fun `not applicable where very poor tracktype and surface match is suspicious, but not conflicting`() {
        assertIsNotApplicable("highway" to "track", "surface" to "gravel", "tracktype" to "grade5")
    }

    @Test fun `not applicable where tracktype and very good surface match is suspicious, but not conflicting`() {
        assertIsNotApplicable("highway" to "track", "surface" to "asphalt", "tracktype" to "grade2")
    }

    @Test fun `tracktype tag is removed when surface match is suspicious`() {
        questType.verifyAnswer(
            mapOf(
                "tracktype" to "grade2",
                "smoothness" to "good"
            ),
            SurfaceAndNote(Surface.ASPHALT),
            StringMapEntryAdd("surface", "asphalt"),
            StringMapEntryDelete("tracktype", "grade2"),
            StringMapEntryDelete("smoothness", "good"),
        )
    }

    @Test fun `tracktype tag is removed when surface match is conflicting`() {
        questType.verifyAnswer(
            mapOf(
                "tracktype" to "grade2",
                "smoothness" to "good"
            ),
            SurfaceAndNote(Surface.SAND),
            StringMapEntryAdd("surface", "sand"),
            StringMapEntryDelete("tracktype", "grade2"),
            StringMapEntryDelete("smoothness", "good"),
        )
    }
}
