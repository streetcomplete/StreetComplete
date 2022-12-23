package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.testutils.way
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AddSidewalkSurfaceTest {
    private val questType = AddSidewalkSurface()

    @Test fun `not applicable to road with separate sidewalks`() {
        assertIsNotApplicable("sidewalk" to "separate")
    }

    @Test fun `not applicable to road with no sidewalks`() {
        assertIsNotApplicable("sidewalk" to "no")
    }

    @Test fun `applicable to road with sidewalk on both sides`() {
        assertIsApplicable("highway" to "residential", "sidewalk" to "both")
    }

    @Test fun `applicable to road with sidewalk on only one side`() {
        assertIsApplicable("highway" to "residential", "sidewalk" to "left")
        assertIsApplicable("highway" to "residential", "sidewalk" to "right")
    }

    @Test fun `applicable to road with sidewalk on one side and separate sidewalk on the other`() {
        assertIsApplicable("highway" to "residential", "sidewalk:left" to "yes", "sidewalk:right" to "separate")
        assertIsApplicable("highway" to "residential", "sidewalk:left" to "separate", "sidewalk:right" to "yes")
    }

    @Test fun `applicable to road with sidewalk on one side and no sidewalk on the other`() {
        assertIsApplicable("highway" to "residential", "sidewalk:left" to "yes", "sidewalk:right" to "no")
        assertIsApplicable("highway" to "residential", "sidewalk:left" to "no", "sidewalk:right" to "yes")
    }

    private fun assertIsApplicable(vararg pairs: Pair<String, String>) {
        assertTrue(questType.isApplicableTo(way(nodes = listOf(1, 2, 3), tags = mapOf(*pairs))))
    }

    private fun assertIsNotApplicable(vararg pairs: Pair<String, String>) {
        assertFalse(questType.isApplicableTo(way(nodes = listOf(1, 2, 3), tags = mapOf(*pairs))))
    }
}
