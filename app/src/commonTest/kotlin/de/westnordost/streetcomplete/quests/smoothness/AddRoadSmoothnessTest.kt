package de.westnordost.streetcomplete.quests.smoothness

import de.westnordost.streetcomplete.testutils.way
import kotlin.test.Test
import kotlin.test.assertTrue

class AddRoadSmoothnessTest {
    private val questType = AddRoadSmoothness()

    @Test fun `applicable to old way tagged with smoothness-date`() {
        assertTrue(questType.isApplicableTo(way(tags = mapOf(
            "highway" to "residential",
            "surface" to "asphalt",
            "smoothness" to "excellent",
            "smoothness:date" to "2014-10-10"
        ))))
    }
}
