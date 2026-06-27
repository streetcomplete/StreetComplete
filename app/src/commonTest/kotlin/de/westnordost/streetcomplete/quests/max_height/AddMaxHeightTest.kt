package de.westnordost.streetcomplete.quests.max_height

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.osm.length.Length
import de.westnordost.streetcomplete.testutils.TestMapDataWithGeometry
import de.westnordost.streetcomplete.quests.answerApplied
import de.westnordost.streetcomplete.quests.createMapData
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.testutils.way
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AddMaxHeightTest {

    private val questType = AddMaxHeight()

    @Test fun `applicable to parking entrance node that is a vertex of a road`() {
        val parkingEntrance = node(2, tags = mapOf(
            "amenity" to "parking_entrance",
            "parking" to "underground"
        ))
        val road = way(1, listOf(1, 2), mapOf(
            "highway" to "service"
        ))

        assertEquals(
            listOf(parkingEntrance),
            questType.getApplicableElements(TestMapDataWithGeometry(listOf(road, parkingEntrance)))
        )
        assertNull(questType.isApplicableTo(parkingEntrance))
    }

    @Test fun `not applicable to parking entrance node that is a vertex of a private road`() {
        val parkingEntrance = node(2, tags = mapOf(
            "amenity" to "parking_entrance",
            "parking" to "underground"
        ))
        val road = way(1, listOf(1, 2), mapOf(
            "highway" to "service",
            "access" to "private"
        ))

        assertEquals(
            0,
            questType.getApplicableElements(TestMapDataWithGeometry(listOf(road, parkingEntrance))).count()
        )
        assertNull(questType.isApplicableTo(parkingEntrance))
    }

    @Test fun `not applicable to parking entrance node that is not vertex of a road`() {
        val parkingEntrance = node(2, tags = mapOf(
            "amenity" to "parking_entrance",
            "parking" to "underground"
        ))
        val footway = way(1, listOf(1, 2), mapOf(
            "highway" to "footway"
        ))

        val mapData = TestMapDataWithGeometry(listOf(footway, parkingEntrance))

        assertEquals(0, questType.getApplicableElements(mapData).count())
        assertNull(questType.isApplicableTo(parkingEntrance))
    }

    @Test fun `applicable to railway crossing node that is a vertex of an electrified railway and a road or path`() {
        val crossing = node(2, tags = mapOf("railway" to "level_crossing"))
        val railway = way(1, listOf(1, 2, 3), mapOf(
            "railway" to "rail",
            "electrified" to "contact_line"
        ))
        val road = way(2, listOf(4, 2, 5), mapOf("highway" to "residential"))
        val path = way(2, listOf(4, 2, 5), mapOf("highway" to "footway"))

        assertEquals(
            listOf(crossing),
            questType.getApplicableElements(TestMapDataWithGeometry(listOf(railway, crossing, road)))
        )
        assertEquals(
            listOf(crossing),
            questType.getApplicableElements(TestMapDataWithGeometry(listOf(railway, crossing, path)))
        )
        assertNull(questType.isApplicableTo(crossing))
    }

    @Test fun `not applicable to railway crossing node that is a vertex of a normal railway`() {
        val crossing = node(2, tags = mapOf("railway" to "level_crossing"))
        val railway = way(1, listOf(1, 2, 3), mapOf("railway" to "rail"))
        val road = way(2, listOf(4, 2, 5), mapOf("highway" to "residential"))

        val mapData = TestMapDataWithGeometry(listOf(railway, crossing, road))

        assertEquals(0, questType.getApplicableElements(mapData).count())
        assertNull(questType.isApplicableTo(crossing))
    }

    @Test fun `applicable to tunnel or covered`() {
        val tunnel = way(1, listOf(1, 2), mapOf("highway" to "residential", "tunnel" to "yes"))
        val covered = way(1, listOf(1, 2), mapOf("highway" to "residential", "covered" to "yes"))

        assertEquals(
            listOf(tunnel),
            questType.getApplicableElements(TestMapDataWithGeometry(listOf(tunnel)))
        )
        assertEquals(
            listOf(covered),
            questType.getApplicableElements(TestMapDataWithGeometry(listOf(covered)))
        )
    }

    @Test fun `not applicable to private tunnel`() {
        val mapData = TestMapDataWithGeometry(listOf(
            way(1, listOf(1, 2), mapOf(
                "highway" to "residential",
                "tunnel" to "yes",
                "access" to "private"
            ))
        ))
        assertEquals(0, questType.getApplicableElements(mapData).count())
    }

    @Test fun `applicable to road below bridge`() {
        val bridge = way(1, listOf(1, 2), mapOf(
            "highway" to "residential",
            "layer" to "1",
            "bridge" to "yes"
        ))
        val bridgeGeometry = ElementPolylinesGeometry(listOf(listOf(
            p(-0.1, 0.0),
            p(+0.1, 0.0),
        )), p(0.0, 0.0))

        val wayBelowBridge = way(2, listOf(3, 4), mapOf(
            "highway" to "residential"
        ))
        val wayBelowBridgeGeometry = ElementPolylinesGeometry(listOf(listOf(
            p(0.0, -0.1),
            p(0.0, +0.1),
        )), p(0.0, 0.0))

        assertEquals(
            listOf(wayBelowBridge),
            questType.getApplicableElements(createMapData(mapOf(
                bridge to bridgeGeometry,
                wayBelowBridge to wayBelowBridgeGeometry
            )))
        )
    }

    @Test fun `not applicable to private road below bridge`() {
        val bridge = way(1, listOf(1, 2), mapOf(
            "highway" to "residential",
            "layer" to "1",
            "bridge" to "yes"
        ))
        val bridgeGeometry = ElementPolylinesGeometry(listOf(listOf(
            p(-0.1, 0.0),
            p(+0.1, 0.0),
        )), p(0.0, 0.0))

        val wayBelowBridge = way(2, listOf(3, 4), mapOf(
            "highway" to "residential",
            "access" to "private"
        ))
        val wayBelowBridgeGeometry = ElementPolylinesGeometry(listOf(listOf(
            p(0.0, -0.1),
            p(0.0, +0.1),
        )), p(0.0, 0.0))

        assertEquals(
            0,
            questType.getApplicableElements(createMapData(mapOf(
                bridge to bridgeGeometry,
                wayBelowBridge to wayBelowBridgeGeometry
            ))).count()
        )
    }

    @Test fun `not applicable to road on same layer as bridge, even if they intersect`() {
        val bridge = way(1, listOf(1, 2), mapOf(
            "highway" to "residential",
            "layer" to "1",
            "bridge" to "yes"
        ))
        val bridgeGeometry = ElementPolylinesGeometry(listOf(listOf(
            p(-0.1, 0.0),
            p(+0.1, 0.0),
        )), p(0.0, 0.0))

        val wayNotBelowBridge = way(2, listOf(3, 4), mapOf(
            "highway" to "residential",
            "layer" to "1"
        ))
        val wayNotBelowBridgeGeometry = ElementPolylinesGeometry(listOf(listOf(
            p(0.0, -0.1),
            p(0.0, +0.1),
        )), p(0.0, 0.0))

        assertEquals(
            0,
            questType.getApplicableElements(createMapData(mapOf(
                bridge to bridgeGeometry,
                wayNotBelowBridge to wayNotBelowBridgeGeometry
            ))).count()
        )
    }

    @Test fun `not applicable to road that shares a node with the bridge`() {
        val bridge = way(1, listOf(1, 2, 3), mapOf(
            "highway" to "residential",
            "layer" to "1",
            "bridge" to "yes"
        ))
        val bridgeGeometry = ElementPolylinesGeometry(listOf(listOf(
            p(-0.1, 0.0),
            p(+0.1, 0.0),
        )), p(0.0, 0.0))

        val wayConnectingWithBridge = way(2, listOf(3, 5, 4), mapOf(
            "highway" to "residential"
        ))
        val wayConnectingWithBridgeGeometry = ElementPolylinesGeometry(listOf(listOf(
            p(0.0, -0.1),
            p(0.0, +0.1),
        )), p(0.0, 0.0))

        val mapData =

        assertEquals(
            0,
            questType.getApplicableElements(createMapData(mapOf(
                bridge to bridgeGeometry,
                wayConnectingWithBridge to wayConnectingWithBridgeGeometry
            ))).count()
        )
    }

    @Test fun `apply metric height answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("maxheight", "3.5")),
            questType.answerApplied(MaxHeight(Length.Meters(3.5)))
        )
    }

    @Test fun `apply imperial height answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("maxheight", "10'6\"")),
            questType.answerApplied(MaxHeight(Length.FeetAndInches(10, 6)))
        )
    }

    @Test fun `apply no height sign answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("maxheight:signed", "no")),
            questType.answerApplied(NoMaxHeightSign)
        )
    }
}
