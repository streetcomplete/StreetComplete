package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.quests.sidewalk.AddSidewalk
import de.westnordost.streetcomplete.quests.sidewalk.SeparatelyMapped
import de.westnordost.streetcomplete.quests.sidewalk.SidewalkSides
import de.westnordost.streetcomplete.util.translate
import de.westnordost.streetcomplete.testutils.way
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

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
        assertNull(questType.isApplicableTo(road))
    }

    @Test fun `not applicable to road with nearby footway`() {
        val road = way(1, listOf(1,2), mapOf(
            "highway" to "primary",
            "lit" to "yes",
            "width" to "18"
        ))
        val footway = way(2, listOf(3,4), mapOf(
            "highway" to "footway"
        ))
        val mapData = TestMapDataWithGeometry(listOf(road, footway))
        val p1 = p(0.0,0.0)
        val p2 = p1.translate(50.0, 45.0)
        val p3 = p1.translate(14.0, 135.0)
        val p4 = p3.translate(50.0, 45.0)

        mapData.wayGeometriesById[1L] = ElementPolylinesGeometry(listOf(listOf(p1, p2)), p1)
        mapData.wayGeometriesById[2L] = ElementPolylinesGeometry(listOf(listOf(p3, p4)), p3)

        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertNull(questType.isApplicableTo(road))
    }

    @Test fun `applicable to road with nearby footway that is not aligned to the road`() {
        val road = way(1, listOf(1,2), mapOf(
            "highway" to "primary",
            "lit" to "yes",
            "width" to "18"
        ))
        val footway = way(2, listOf(3,4), mapOf(
            "highway" to "footway"
        ))
        val mapData = TestMapDataWithGeometry(listOf(road, footway))
        val p1 = p(0.0,0.0)
        val p2 = p1.translate(50.0, 45.0)
        val p3 = p1.translate(10.0, 135.0)
        val p4 = p3.translate(50.0, 75.0)

        mapData.wayGeometriesById[1L] = ElementPolylinesGeometry(listOf(listOf(p1, p2)), p1)
        mapData.wayGeometriesById[2L] = ElementPolylinesGeometry(listOf(listOf(p3, p4)), p3)

        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        assertNull(questType.isApplicableTo(road))
    }

    @Test fun `applicable to road with footway that is far away enough`() {
        val road = way(1L, listOf(1,2), mapOf(
            "highway" to "primary",
            "lit" to "yes",
            "width" to "18"
        ))
        val footway = way(2L, listOf(3,4), mapOf(
            "highway" to "footway"
        ))

        val mapData = TestMapDataWithGeometry(listOf(road, footway))
        val p1 = p(0.0,0.0)
        val p2 = p1.translate(50.0, 45.0)
        val p3 = p1.translate(16.0, 135.0)
        val p4 = p3.translate(50.0, 45.0)

        mapData.wayGeometriesById[1L] = ElementPolylinesGeometry(listOf(listOf(p1, p2)), p1)
        mapData.wayGeometriesById[2L] = ElementPolylinesGeometry(listOf(listOf(p3, p4)), p3)

        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        assertNull(questType.isApplicableTo(road))
    }

    @Test fun `applicable to small road with footway that is far away enough`() {
        val road = way(1, listOf(1,2), mapOf(
            "highway" to "primary",
            "lit" to "yes",
            "lanes" to "2"
        ))
        val footway = way(2, listOf(3,4), mapOf(
            "highway" to "cycleway"
        ))

        val mapData = TestMapDataWithGeometry(listOf(road, footway))
        val p1 = p(0.0,0.0)
        val p2 = p1.translate(50.0, 45.0)
        val p3 = p1.translate(10.0, 135.0)
        val p4 = p3.translate(50.0, 45.0)

        mapData.wayGeometriesById[1L] = ElementPolylinesGeometry(listOf(listOf(p1, p2)), p1)
        mapData.wayGeometriesById[2L] = ElementPolylinesGeometry(listOf(listOf(p3, p4)), p3)

        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        assertNull(questType.isApplicableTo(road))
    }

    @Test fun `apply no sidewalk answer`() {
        questType.verifyAnswer(
            SidewalkSides(left = false, right = false),
            StringMapEntryAdd("sidewalk", "no")
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
