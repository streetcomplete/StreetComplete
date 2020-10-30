package de.westnordost.streetcomplete.quests

import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.map.data.OsmWay
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.quests.sidewalk.AddSidewalk
import de.westnordost.streetcomplete.quests.sidewalk.SeparatelyMapped
import de.westnordost.streetcomplete.quests.sidewalk.SidewalkSides
import de.westnordost.streetcomplete.util.translate
import org.junit.Assert.assertEquals
import org.junit.Test

class AddSidewalkTest {

    private val questType = AddSidewalk()

    @Test fun `applicable to road with missing sidewalk`() {
        val mapData = TestMapDataWithGeometry(listOf(
            OsmWay(1L, 1, listOf(1,2,3), mapOf(
                "highway" to "primary",
                "lit" to "yes"
            ))
        ))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `not applicable to road with nearby footway`() {
        val mapData = TestMapDataWithGeometry(listOf(
            OsmWay(1L, 1, listOf(1,2), mapOf(
                "highway" to "primary",
                "lit" to "yes"
            )),
            OsmWay(2L, 1, listOf(3,4), mapOf(
                "highway" to "footway"
            ))
        ))
        val p1 = OsmLatLon(0.0,0.0)
        val p2 = p1.translate(50.0, 45.0)
        val p3 = p1.translate(14.0, 135.0)
        val p4 = p3.translate(50.0, 45.0)

        mapData.wayGeometriesById[1L] = ElementPolylinesGeometry(listOf(listOf(p1, p2)), p1)
        mapData.wayGeometriesById[2L] = ElementPolylinesGeometry(listOf(listOf(p3, p4)), p3)

        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `applicable to road with footway that is far away enough`() {
        val mapData = TestMapDataWithGeometry(listOf(
            OsmWay(1L, 1, listOf(1,2), mapOf(
                "highway" to "primary",
                "lit" to "yes"
            )),
            OsmWay(2L, 1, listOf(3,4), mapOf(
                "highway" to "footway"
            ))
        ))
        val p1 = OsmLatLon(0.0,0.0)
        val p2 = p1.translate(50.0, 45.0)
        val p3 = p1.translate(16.0, 135.0)
        val p4 = p3.translate(50.0, 45.0)

        mapData.wayGeometriesById[1L] = ElementPolylinesGeometry(listOf(listOf(p1, p2)), p1)
        mapData.wayGeometriesById[2L] = ElementPolylinesGeometry(listOf(listOf(p3, p4)), p3)

        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `apply no sidewalk answer`() {
        questType.verifyAnswer(
            SidewalkSides(left = false, right = false),
            StringMapEntryAdd("sidewalk", "none")
        )
    }

    @Test fun `apply sidewalk left answer`() {
        questType.verifyAnswer(
            SidewalkSides(left = true, right = false),
            StringMapEntryAdd("sidewalk", "left")
        )
    }

    @Test fun `apply sidewalk right answer`() {
        questType.verifyAnswer(
            SidewalkSides(left = false, right = true),
            StringMapEntryAdd("sidewalk", "right")
        )
    }

    @Test fun `apply sidewalk on both sides answer`() {
        questType.verifyAnswer(
            SidewalkSides(left = true, right = true),
            StringMapEntryAdd("sidewalk", "both")
        )
    }

    @Test fun `apply separate sidewalk answer`() {
        questType.verifyAnswer(
            SeparatelyMapped,
            StringMapEntryAdd("sidewalk", "separate")
        )
    }
}
