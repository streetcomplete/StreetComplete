package de.westnordost.streetcomplete.quests.surface

import de.westnordost.osmapi.map.data.OsmNode
import de.westnordost.osmapi.map.data.OsmWay
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import org.junit.Assert.*
import org.junit.Test

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
        assertTrue(questType.isApplicableTo(create(*pairs)))
    }

    private fun assertIsNotApplicable(vararg pairs: Pair<String, String>) {
        assertFalse(questType.isApplicableTo(create(*pairs)))
    }

    private fun create(vararg pairs: Pair<String, String>) = OsmWay(1L, 1, listOf(1,2,3), mapOf(*pairs))
}
