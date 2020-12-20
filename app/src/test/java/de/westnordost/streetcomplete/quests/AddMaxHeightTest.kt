package de.westnordost.streetcomplete.quests

import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.map.data.OsmWay
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.quests.max_height.*
import org.junit.Assert.assertEquals
import org.junit.Test

class AddMaxHeightTest {

    private val questType = AddMaxHeight()

    @Test fun `applicable to road below bridge`() {
        val mapData = TestMapDataWithGeometry(listOf(
            OsmWay(1L, 1, listOf(1,2), mapOf(
                "highway" to "residential",
                "layer" to "1",
                "bridge" to "yes"
            )),
            OsmWay(2L, 1, listOf(3,4), mapOf(
                "highway" to "residential"
            ))
        ))
        mapData.wayGeometriesById[1] = ElementPolylinesGeometry(listOf(listOf(
            OsmLatLon(-0.1,0.0),
            OsmLatLon(+0.1,0.0),
        )), OsmLatLon(0.0,0.0))
        mapData.wayGeometriesById[2] = ElementPolylinesGeometry(listOf(listOf(
            OsmLatLon(0.0,-0.1),
            OsmLatLon(0.0,+0.1),
        )), OsmLatLon(0.0,0.0))

        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `not applicable to road not below bridge`() {
        val mapData = TestMapDataWithGeometry(listOf(
            OsmWay(1L, 1, listOf(1,2), mapOf(
                "highway" to "residential",
                "bridge" to "yes",
                "layer" to "1"
            )),
            OsmWay(2L, 1, listOf(3,4), mapOf(
                "highway" to "residential",
                "layer" to "1"
            ))
        ))
        mapData.wayGeometriesById[1] = ElementPolylinesGeometry(listOf(listOf(
            OsmLatLon(-0.1,0.0),
            OsmLatLon(+0.1,0.0),
        )), OsmLatLon(0.0,0.0))
        mapData.wayGeometriesById[2] = ElementPolylinesGeometry(listOf(listOf(
            OsmLatLon(0.0,-0.1),
            OsmLatLon(0.0,+0.1),
        )), OsmLatLon(0.0,0.0))

        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `apply metric height answer`() {
        questType.verifyAnswer(
            MaxHeight(Meters(3.5)),
            StringMapEntryAdd("maxheight","3.5")
        )
    }

    @Test fun `apply imperial height answer`() {
        questType.verifyAnswer(
            MaxHeight(ImperialFeetAndInches(10, 6)),
            StringMapEntryAdd("maxheight","10'6\"")
        )
    }

    @Test fun `apply default height answer`() {
        questType.verifyAnswer(
            NoMaxHeightSign(true),
            StringMapEntryAdd("maxheight","default")
        )
    }

    @Test fun `apply below default height answer`() {
        questType.verifyAnswer(
            NoMaxHeightSign(false),
            StringMapEntryAdd("maxheight","below_default")
        )
    }
}
