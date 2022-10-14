package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.testutils.way
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AddRoadSurfaceTest {
    private val questType = AddRoadSurface()

    @Test fun `not applicable to tagged surface`() {
        assertIsNotApplicable("highway" to "residential", "surface" to "asphalt")
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

    private fun assertIsApplicable(vararg pairs: Pair<String, String>) {
        assertTrue(questType.isApplicableTo(way(nodes = listOf(1, 2, 3), tags = mapOf(*pairs))))
    }

    private fun assertIsNotApplicable(vararg pairs: Pair<String, String>) {
        assertFalse(questType.isApplicableTo(way(nodes = listOf(1, 2, 3), tags = mapOf(*pairs))))
    }
}
