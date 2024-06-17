package de.westnordost.streetcomplete.quests.existence

import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import io.mockative.Mock
import io.mockative.classOf
import io.mockative.mock
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CheckExistenceTest {
    // dummy
    @Mock
    private lateinit var feature: Feature

    private val questType = CheckExistence { element ->
        if (element.tags["amenity"] == "telephone") mock(classOf<Feature>()) else null
    }

    @Test fun `isApplicableTo returns false for known places with recently edited amenity=telephone`() {
        assertFalse(
            questType.isApplicableTo(
                node(
                    tags = mapOf(
                        "amenity" to "telephone",
                    ), timestamp = nowAsEpochMilliseconds()
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
                    ), timestamp = nowAsEpochMilliseconds() - millisecondsFor800Days
                )
            )
        )
    }
}
