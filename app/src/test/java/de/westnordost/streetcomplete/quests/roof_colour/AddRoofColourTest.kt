package de.westnordost.streetcomplete.quests.roof_colour

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.quests.answerApplied
import de.westnordost.streetcomplete.testutils.way
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AddRoofColourTest {
    private val questType = AddRoofColour()

    @Test
    fun `not applicable to roofs with colour already set`() {
        assertFalse(
            questType.isApplicableTo(
                way(tags = mapOf("roof:levels" to "1", "roof:colour" to "something"))
            )
        )
    }

    @Test
    fun `not applicable to building parts`() {
        assertFalse(
            questType.isApplicableTo(
                way(tags = mapOf("building:levels" to "1", "building:part" to "something"))
            )
        )
    }

    @Test
    fun `not applicable to demolished building`() {
        assertFalse(
            questType.isApplicableTo(
                way(tags = mapOf("building:levels" to "1", "demolished:building" to "something"))
            )
        )
    }

    @Test
    fun `not applicable to negated building`() {
        assertFalse(
            questType.isApplicableTo(
                way(tags = mapOf("building:levels" to "1", "building" to "no"))
            )
        )
    }

    @Test
    fun `not applicable to building under construction`() {
        assertFalse(
            questType.isApplicableTo(
                way(tags = mapOf("building:levels" to "1", "building" to "construction"))
            )
        )
    }

    @Test
    fun `not applicable to roofs without shape`() {
        assertFalse(
            questType.isApplicableTo(
                way(tags = mapOf("roof:levels" to "1", "building" to "apartments"))
            )
        )
    }

    @Test
    fun `not applicable to roofs with shape = flat`() {
        assertFalse(
            questType.isApplicableTo(
                way(
                    tags = mapOf(
                        "roof:shape" to "flat",
                        "roof:levels" to "1",
                        "building" to "apartments"
                    )
                )
            )
        )
    }

    @Test
    fun `applicable to roofs with shapes != flat`() {
        assertTrue(
            questType.isApplicableTo(
                way(
                    tags = mapOf(
                        "roof:shape" to "quadruple_saltbox",
                        "roof:levels" to "1",
                        "building" to "apartments"
                    )
                )
            )
        )
    }

    @Test
    fun `applicable to buildings with many levels and enough roof levels to be visible from below`() {
        assertTrue(
            questType.isApplicableTo(
                way(
                    tags = mapOf(
                        "roof:shape" to "pyramidal",
                        "building:levels" to "6",
                        "roof:levels" to "1.5",
                        "building" to "apartments"
                    )
                )
            )
        )
        assertTrue(
            questType.isApplicableTo(
                way(
                    tags = mapOf(
                        "roof:shape" to "round",
                        "building:levels" to "8",
                        "roof:levels" to "3",
                        "building" to "apartments"
                    )
                )
            )
        )
        assertTrue(
            questType.isApplicableTo(
                way(
                    tags = mapOf(
                        "roof:shape" to "skillion",
                        "building:levels" to "4.5",
                        "roof:levels" to "0.5",
                        "building" to "apartments"
                    )
                )
            )
        )
    }

    @Test
    fun `apply hex answer`() {
        assertEquals(
            questType.answerApplied(RoofColour.DESERT_SAND),
            setOf(StringMapEntryAdd("roof:colour", "#bbad8e"))
        )
    }

    @Test
    fun `apply named answer`() {
        assertEquals(
            questType.answerApplied(RoofColour.LIME),
            setOf(StringMapEntryAdd("roof:colour", "lime"))
        )
    }
}

