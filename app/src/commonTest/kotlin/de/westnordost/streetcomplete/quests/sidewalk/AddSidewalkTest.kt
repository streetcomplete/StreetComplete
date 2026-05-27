package de.westnordost.streetcomplete.quests.sidewalk

import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.testutils.way
import de.westnordost.streetcomplete.util.math.translate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AddSidewalkTest {

    private val questType = AddSidewalk()

    @Test fun `not applicable to road with sidewalk`() {
        val road = way(tags = mapOf(
            "highway" to "primary",
            "sidewalk" to "both"
        ))
        val mapData = TestMapDataWithGeometry(listOf(road))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertEquals(false, questType.isApplicableTo(road))
    }

    @Test fun `applicable to road with missing sidewalk`() {
        val road = way(tags = mapOf(
            "highway" to "primary",
            "lit" to "yes"
        ))
        val mapData = TestMapDataWithGeometry(listOf(road))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        assertTrue(questType.isApplicableTo(road))
    }

    @Test fun `applicable to road with incomplete sidewalk tagging`() {
        val road = way(tags = mapOf(
            "highway" to "residential",
            "sidewalk:left" to "yes"
        ))
        val mapData = TestMapDataWithGeometry(listOf(road))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        assertTrue(questType.isApplicableTo(road))
    }

    @Test fun `applicable to road with invalid sidewalk tagging`() {
        val road = way(tags = mapOf(
            "highway" to "residential",
            "sidewalk" to "something"
        ))
        val footway = way(2, listOf(3, 4), mapOf(
            "highway" to "footway"
        ))
        val mapData = TestMapDataWithGeometry(listOf(road, footway))
        val p1 = p(0.0, 0.0)
        val p2 = p1.translate(50.0, 45.0)
        val p3 = p1.translate(13.0, 135.0)
        val p4 = p3.translate(50.0, 45.0)

        mapData.wayGeometriesById[1L] = ElementPolylinesGeometry(listOf(listOf(p1, p2)), p1)
        mapData.wayGeometriesById[2L] = ElementPolylinesGeometry(listOf(listOf(p3, p4)), p3)

        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        assertTrue(questType.isApplicableTo(road))
    }

    @Test fun `applicable to road with overloaded sidewalk tagging`() {
        val road = way(tags = mapOf(
            "highway" to "residential",
            "sidewalk" to "left",
            "sidewalk:right" to "yes"
        ))
        val footway = way(2, listOf(3, 4), mapOf(
            "highway" to "footway"
        ))
        val mapData = TestMapDataWithGeometry(listOf(road, footway))
        val p1 = p(0.0, 0.0)
        val p2 = p1.translate(50.0, 45.0)
        val p3 = p1.translate(13.0, 135.0)
        val p4 = p3.translate(50.0, 45.0)

        mapData.wayGeometriesById[1L] = ElementPolylinesGeometry(listOf(listOf(p1, p2)), p1)
        mapData.wayGeometriesById[2L] = ElementPolylinesGeometry(listOf(listOf(p3, p4)), p3)

        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        assertTrue(questType.isApplicableTo(road))
    }

    @Test fun `not applicable to motorways`() {
        val road = way(tags = mapOf(
            "highway" to "motorway",
        ))
        val mapData = TestMapDataWithGeometry(listOf(road))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertEquals(false, questType.isApplicableTo(road))
    }

    @Test fun `applicable to motorways marked as legally accessible to pedestrians`() {
        val road = way(tags = mapOf(
            "highway" to "motorway",
            "foot" to "yes"
        ))
        val mapData = TestMapDataWithGeometry(listOf(road))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        assertTrue(questType.isApplicableTo(road))
    }

    @Test fun `applicable to motorways marked as legally accessible to pedestrians and with tagged speed limit`() {
        val road = way(tags = mapOf(
            "highway" to "motorway",
            "foot" to "yes",
            "maxspeed" to "65 mph",
        ))
        val mapData = TestMapDataWithGeometry(listOf(road))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        assertTrue(questType.isApplicableTo(road))
    }

    @Test fun `not applicable to road with very low speed limit`() {
        val road = way(tags = mapOf(
            "highway" to "residential",
            "maxspeed" to "9",
        ))
        val mapData = TestMapDataWithGeometry(listOf(road))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertEquals(false, questType.isApplicableTo(road))
    }

    @Test fun `applicable to road with implicit speed limit`() {
        val road = way(tags = mapOf(
            "highway" to "primary",
            "maxspeed" to "DE:zone30",
        ))
        val mapData = TestMapDataWithGeometry(listOf(road))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        assertTrue(questType.isApplicableTo(road))
    }

    @Test fun `applicable to road with urban speed limit`() {
        val road = way(tags = mapOf(
            "highway" to "primary",
            "maxspeed" to "60",
        ))
        val mapData = TestMapDataWithGeometry(listOf(road))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        assertTrue(questType.isApplicableTo(road))
    }
}
