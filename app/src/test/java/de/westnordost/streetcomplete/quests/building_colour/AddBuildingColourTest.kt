package de.westnordost.streetcomplete.quests.building_colour

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.quests.answerApplied
import de.westnordost.streetcomplete.testutils.way
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AddBuildingColourTest {
    private val questType = AddBuildingColour()

    @Test
    fun `not applicable to building with colour already set`() {
        assertFalse(
            questType.isApplicableTo(
                way(tags = mapOf("building" to "1", "building:colour" to "something"))
            )
        )
    }

    @Test
    fun `not applicable to building part with colour already set`() {
        assertFalse(
            questType.isApplicableTo(
                way(tags = mapOf("building:part" to "1", "building:colour" to "something"))
            )
        )
    }

    @Test
    fun `not applicable to negated building that's not a building part`() {
        assertFalse(
            questType.isApplicableTo(
                way(tags = mapOf("building" to "no"))
            )
        )
    }

    @Test
    fun `not applicable to negated building part that's not a building`() {
        assertFalse(
            questType.isApplicableTo(
                way(tags = mapOf("building" to "no"))
            )
        )
    }

    @Test
    fun `not applicable to building under construction`() {
        assertFalse(
            questType.isApplicableTo(
                way(tags = mapOf("building" to "construction"))
            )
        )
    }

    @Test
    fun `not applicable to building part under construction`() {
        assertFalse(
            questType.isApplicableTo(
                way(tags = mapOf("building:part" to "construction"))
            )
        )
    }

    @Test
    fun `not applicable to indoor room`() {
        assertFalse(
            questType.isApplicableTo(
                way(tags = mapOf("building" to "yes", "indoor" to "yes"))
            )
        )
    }

    @Test
    fun `not applicable to building part indoor room`() {
        assertFalse(
            questType.isApplicableTo(
                way(tags = mapOf("building:part" to "yes", "indoor" to "yes"))
            )
        )
    }

    @Test
    fun `not applicable to building without wall`() {
        assertFalse(
            questType.isApplicableTo(
                way(tags = mapOf("building" to "yes", "wall" to "no"))
            )
        )
    }

    @Test
    fun `not applicable to building part without wall`() {
        assertFalse(
            questType.isApplicableTo(
                way(tags = mapOf("building:part" to "yes", "wall" to "no"))
            )
        )
    }

    @Test
    fun `not applicable to roof building`() {
        assertFalse(
            questType.isApplicableTo(
                way(tags = mapOf("building" to "roof"))
            )
        )
    }

    @Test
    fun `not applicable to roof building part`() {
        assertFalse(
            questType.isApplicableTo(
                way(tags = mapOf("building:part" to "roof"))
            )
        )
    }

    @Test
    fun `applicable to building with indoor=no`() {
        assertTrue(
            questType.isApplicableTo(
                way(tags = mapOf("building" to "yes", "indoor" to "no"))
            )
        )
    }

    @Test
    fun `applicable to building part with indoor=no`() {
        assertTrue(
            questType.isApplicableTo(
                way(tags = mapOf("building:part" to "yes", "indoor" to "no"))
            )
        )
    }

    @Test
    fun `applicable to negated building that's a building part`() {
        assertTrue(
            questType.isApplicableTo(
                way(tags = mapOf("building" to "no", "building:part" to "yes"))
            )
        )
    }

    @Test
    fun `applicable to negated building part that's a building`() {
        assertTrue(
            questType.isApplicableTo(
                way(tags = mapOf("building:part" to "no", "building:part" to "yes"))
            )
        )
    }

    @Test
    fun `applicable to buildings or building parts`() {
        assertTrue(
            questType.isApplicableTo(
                way(
                    tags = mapOf(
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
                        "building:part" to "deck",
                    )
                )
            )
        )
    }

    @Test
    fun `apply hex answer`() {
        assertEquals(
            questType.answerApplied(BuildingColour.BEIGEISH),
            setOf(StringMapEntryAdd("building:colour", BuildingColour.BEIGEISH.osmValue))
        )
    }

    @Test
    fun `apply named answer`() {
        assertEquals(
            questType.answerApplied(BuildingColour.LIME),
            setOf(StringMapEntryAdd("building:colour", BuildingColour.LIME.osmValue))
        )
    }
}

