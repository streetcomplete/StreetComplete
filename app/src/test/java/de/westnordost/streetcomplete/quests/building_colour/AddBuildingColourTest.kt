package de.westnordost.streetcomplete.quests.building_colour

import de.westnordost.streetcomplete.StreetCompleteApplication
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.quests.verifyAnswer
import de.westnordost.streetcomplete.testutils.mockPrefs
import de.westnordost.streetcomplete.testutils.way
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AddBuildingColourTest {
    private lateinit var questType: AddBuildingColour

    @Before
    fun setUp() {
        StreetCompleteApplication.preferences = mockPrefs()
        questType = AddBuildingColour()
    }

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
    fun `not applicable to building under construction `() {
        assertFalse(
            questType.isApplicableTo(
                way(tags = mapOf("building" to "construction"))
            )
        )
    }

    @Test
    fun `not applicable to building part under construction `() {
        assertFalse(
            questType.isApplicableTo(
                way(tags = mapOf("building:part" to "construction"))
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
        questType.verifyAnswer(
            BuildingColour.DESERT_SAND,
            StringMapEntryAdd("building:colour", BuildingColour.DESERT_SAND.osmValue)
        )
    }

    @Test
    fun `apply named answer`() {
        questType.verifyAnswer(
            BuildingColour.LIME,
            StringMapEntryAdd("building:colour", BuildingColour.LIME.osmValue)
        )
    }
}

