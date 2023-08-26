package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.testutils.way
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AddPitchSurfaceTest {
    private val questType = AddPitchSurface()

    @Test fun `not applicable to pitch area without tagged sport`() {
        assertIsNotApplicable("leisure" to "pitch")
    }

    @Test fun `applicable to pitch area with single valid sport`() {
        assertIsApplicable("leisure" to "pitch", "sport" to "soccer")
    }

    @Test fun `applicable to pitch area with single valid sport in the middle of a list`() {
        assertIsApplicable("leisure" to "pitch", "sport" to "rhubarb;soccer;tree_planting")
    }

    @Test fun `applicable to pitch area with single valid sport in the end of a list`() {
        assertIsApplicable("leisure" to "pitch", "sport" to "rhubarb;soccer")
    }

    @Test fun `applicable to pitch area with single valid sport in the start of a list`() {
        assertIsApplicable("leisure" to "pitch", "sport" to "soccer;zażółć")
    }

    @Test fun `not applicable to pitch area with single invalid sport`() {
        assertIsNotApplicable("leisure" to "pitch", "sport" to "table_tennis")
    }

    @Test fun `applicable to pitch area with both valid and invalid sport`() {
        assertIsApplicable("leisure" to "pitch", "sport" to "multi;table_tennis")
    }

    @Test fun `not applicable to pitch area with sport value that is substring of valid one`() {
        assertIsNotApplicable("leisure" to "pitch", "sport" to "table_soccer")
    }

    private fun assertIsApplicable(vararg pairs: Pair<String, String>) {
        assertTrue(questType.isApplicableTo(way(nodes = listOf(1, 2, 3), tags = mapOf(*pairs))))
    }

    private fun assertIsNotApplicable(vararg pairs: Pair<String, String>) {
        assertFalse(questType.isApplicableTo(way(nodes = listOf(1, 2, 3), tags = mapOf(*pairs))))
    }
}
