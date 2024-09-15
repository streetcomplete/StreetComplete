package de.westnordost.streetcomplete.quests.existence

import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CheckExistenceTest {
    private val questType = CheckExistence { element ->
        if (element.tags["amenity"] == "telephone") mock() else null
    }

    @Test fun `isApplicableTo returns false for known places with recently edited amenity=telephone`() {
        assertFalse(
            questType.isApplicableTo(
                node(
                    tags = mapOf(
                        "amenity" to "telephone",
                    ), timestamp = System.currentTimeMillis()
                )
            )
        )
    }

    @Test fun `isApplicableTo returns true for known places with old amenity=telephone`() {
        val millisecondsFor800Days: Long = 1000L * 60 * 60 * 24 * 800
        assertTrue(
            questType.isApplicableTo(
                node(
                    tags = mapOf(
                        "amenity" to "telephone",
                    ), timestamp = System.currentTimeMillis() - millisecondsFor800Days
                )
            )
        )
    }
}
