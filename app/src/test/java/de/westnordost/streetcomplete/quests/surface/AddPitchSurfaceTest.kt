package de.westnordost.streetcomplete.quests.surface

import de.westnordost.osmapi.map.data.OsmNode
import de.westnordost.osmapi.map.data.OsmWay
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import org.junit.Assert.*
import org.junit.Test

class AddPitchSurfaceTest {
    private val questType = AddPitchSurface()

    @Test fun `not applicable to pitch area without tagged sport`() {
        val mapData = TestMapDataWithGeometry(listOf(
            OsmWay(1L, 1, listOf(1,2,3), mapOf(
                "leisure" to "pitch",
            ))
        ))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `applicable to pitch area with single valid sport`() {
        val mapData = TestMapDataWithGeometry(listOf(
            OsmWay(1L, 1, listOf(1,2,3), mapOf(
                "leisure" to "pitch",
                "sport" to "soccer",
            ))
        ))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `applicable to pitch area with single valid sport in the middle of a list`() {
        val mapData = TestMapDataWithGeometry(listOf(
            OsmWay(1L, 1, listOf(1,2,3), mapOf(
                "leisure" to "pitch",
                "sport" to "rhubarb;soccer;tree_planting",
            ))
        ))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `applicable to pitch area with single valid sport in the end of a list`() {
        val mapData = TestMapDataWithGeometry(listOf(
            OsmWay(1L, 1, listOf(1,2,3), mapOf(
                "leisure" to "pitch",
                "sport" to "rhubarb;soccer",
            ))
        ))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `applicable to pitch area with single valid sport in the start of a list`() {
        val mapData = TestMapDataWithGeometry(listOf(
            OsmWay(1L, 1, listOf(1,2,3), mapOf(
                "leisure" to "pitch",
                "sport" to "soccer;filler",
            ))
        ))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `not applicable to pitch area with single invalid sport`() {
        val mapData = TestMapDataWithGeometry(listOf(
            OsmWay(1L, 1, listOf(1,2,3), mapOf(
                "leisure" to "pitch",
                "sport" to "table_tennis",
            ))
        ))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `applicable to pitch area with both valid and invalid sport`() {
        val mapData = TestMapDataWithGeometry(listOf(
            OsmWay(1L, 1, listOf(1,2,3), mapOf(
                "leisure" to "pitch",
                "sport" to "multi;table_tennis",
            ))
        ))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `not applicable to pitch area with with sport value that is substring of valid one`() {
        val mapData = TestMapDataWithGeometry(listOf(
            OsmWay(1L, 1, listOf(1,2,3), mapOf(
                "leisure" to "pitch",
                "sport" to "table_soccer",
            ))
        ))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }
}
