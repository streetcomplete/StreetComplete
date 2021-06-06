package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.quests.max_height.*
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.way
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AddMaxHeightTest {

    private val questType = AddMaxHeight()

    @Test fun `applicable to parking entrance node that is a vertex of a road`() {
        val parkingEntrance = node(2, tags = mapOf(
            "amenity" to "parking_entrance",
            "parking" to "underground"
        ))
        val road = way(1, listOf(1,2), mapOf(
            "highway" to "service"
        ))

        val mapData = TestMapDataWithGeometry(listOf(road, parkingEntrance))

        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        assertNull(questType.isApplicableTo(parkingEntrance))
    }

    @Test fun `not applicable to parking entrance node that is not vertex of a road`() {
        val parkingEntrance = node(2, tags = mapOf(
            "amenity" to "parking_entrance",
            "parking" to "underground"
        ))
        val footway = way(1, listOf(1,2), mapOf(
            "highway" to "footway"
        ))

        val mapData = TestMapDataWithGeometry(listOf(footway, parkingEntrance))

        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertNull(questType.isApplicableTo(parkingEntrance))
    }

    @Test fun `applicable to road below bridge`() {
        val mapData = TestMapDataWithGeometry(listOf(
            way(1, listOf(1,2), mapOf(
                "highway" to "residential",
                "layer" to "1",
                "bridge" to "yes"
            )),
            way(2, listOf(3,4), mapOf(
                "highway" to "residential"
            ))
        ))
        mapData.wayGeometriesById[1] = ElementPolylinesGeometry(listOf(listOf(
            p(-0.1,0.0),
            p(+0.1,0.0),
        )), p(0.0,0.0))
        mapData.wayGeometriesById[2] = ElementPolylinesGeometry(listOf(listOf(
            p(0.0,-0.1),
            p(0.0,+0.1),
        )), p(0.0,0.0))

        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `not applicable to road on same layer as bridge, even if they intersect`() {
        val mapData = TestMapDataWithGeometry(listOf(
            way(1, listOf(1,2), mapOf(
                "highway" to "residential",
                "bridge" to "yes",
                "layer" to "1"
            )),
            way(2, listOf(3,4), mapOf(
                "highway" to "residential",
                "layer" to "1"
            ))
        ))
        mapData.wayGeometriesById[1] = ElementPolylinesGeometry(listOf(listOf(
            p(-0.1,0.0),
            p(+0.1,0.0),
        )), p(0.0,0.0))
        mapData.wayGeometriesById[2] = ElementPolylinesGeometry(listOf(listOf(
            p(0.0,-0.1),
            p(0.0,+0.1),
        )), p(0.0,0.0))

        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `not applicable to road that shares a node with the bridge`() {
        val mapData = TestMapDataWithGeometry(listOf(
            way(1, listOf(1,5,2), mapOf(
                "highway" to "residential",
                "layer" to "1",
                "bridge" to "yes"
            )),
            way(2, listOf(3,5,4), mapOf(
                "highway" to "residential"
            ))
        ))
        mapData.wayGeometriesById[1] = ElementPolylinesGeometry(listOf(listOf(
            p(-0.1,0.0),
            p(0.0,0.0),
            p(+0.1,0.0),
        )), p(0.0,0.0))
        mapData.wayGeometriesById[2] = ElementPolylinesGeometry(listOf(listOf(
            p(0.0,-0.1),
            p(0.0,0.0),
            p(0.0,+0.1),
        )), p(0.0,0.0))

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
