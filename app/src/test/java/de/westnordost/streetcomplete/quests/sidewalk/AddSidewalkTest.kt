package de.westnordost.streetcomplete.quests.sidewalk

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.osm.sidewalk.LeftAndRightSidewalk
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.NO
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.SEPARATE
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.YES
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.quests.verifyAnswer
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.testutils.way
import de.westnordost.streetcomplete.util.math.translate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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

    @Test fun `applicable to road with incomplete sidewalk tagging`() {
        val road = way(tags = mapOf(
            "highway" to "residential",
            "sidewalk:left" to "yes"
        ))
        val mapData = TestMapDataWithGeometry(listOf(road))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        assertTrue(questType.isApplicableTo(road)!!)
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
        assertTrue(questType.isApplicableTo(road)!!)
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
        assertTrue(questType.isApplicableTo(road)!!)
    }

    @Test fun `not applicable to road with nearby footway`() {
        val road = way(1, listOf(1, 2), mapOf(
            "highway" to "primary",
            "lit" to "yes",
            "width" to "18"
        ))
        val footway = way(2, listOf(3, 4), mapOf(
            "highway" to "footway"
        ))
        val mapData = TestMapDataWithGeometry(listOf(road, footway))
        val p1 = p(0.0, 0.0)
        val p2 = p1.translate(50.0, 45.0)
        val p3 = p1.translate(12.999, 135.0)
        val p4 = p3.translate(50.0, 45.0)

        mapData.wayGeometriesById[1L] = ElementPolylinesGeometry(listOf(listOf(p1, p2)), p1)
        mapData.wayGeometriesById[2L] = ElementPolylinesGeometry(listOf(listOf(p3, p4)), p3)

        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertNull(questType.isApplicableTo(road))
    }

    @Test fun `applicable to road with nearby footway that is not aligned to the road`() {
        val road = way(1, listOf(1, 2), mapOf(
            "highway" to "primary",
            "lit" to "yes",
            "width" to "18"
        ))
        val footway = way(2, listOf(3, 4), mapOf(
            "highway" to "footway"
        ))
        val mapData = TestMapDataWithGeometry(listOf(road, footway))
        val p1 = p(0.0, 0.0)
        val p2 = p1.translate(50.0, 45.0)
        val p3 = p1.translate(10.0, 135.0)
        val p4 = p3.translate(50.0, 75.0)

        mapData.wayGeometriesById[1L] = ElementPolylinesGeometry(listOf(listOf(p1, p2)), p1)
        mapData.wayGeometriesById[2L] = ElementPolylinesGeometry(listOf(listOf(p3, p4)), p3)

        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        assertNull(questType.isApplicableTo(road))
    }

    @Test fun `applicable to road with footway that is far away enough`() {
        val road = way(1L, listOf(1, 2), mapOf(
            "highway" to "primary",
            "lit" to "yes",
            "width" to "18"
        ))
        val footway = way(2L, listOf(3, 4), mapOf(
            "highway" to "footway"
        ))

        val mapData = TestMapDataWithGeometry(listOf(road, footway))
        val p1 = p(0.0, 0.0)
        val p2 = p1.translate(50.0, 45.0)
        val p3 = p1.translate(16.0, 135.0)
        val p4 = p3.translate(50.0, 45.0)

        mapData.wayGeometriesById[1L] = ElementPolylinesGeometry(listOf(listOf(p1, p2)), p1)
        mapData.wayGeometriesById[2L] = ElementPolylinesGeometry(listOf(listOf(p3, p4)), p3)

        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        assertNull(questType.isApplicableTo(road))
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
        assertNull(questType.isApplicableTo(road))
    }

    @Test fun `applicable to motorways marked as legally accessible to pedestrians and with tagged speed limit`() {
        val road = way(tags = mapOf(
            "highway" to "motorway",
            "foot" to "yes",
            "maxspeed" to "65 mph",
        ))
        val mapData = TestMapDataWithGeometry(listOf(road))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        assertNull(questType.isApplicableTo(road))
    }

    @Test fun `apply no sidewalk answer`() {
        questType.verifyAnswer(
            LeftAndRightSidewalk(left = NO, right = NO),
            StringMapEntryAdd("sidewalk", "no")
        )
    }

    @Test fun `apply sidewalk left answer`() {
        questType.verifyAnswer(
            LeftAndRightSidewalk(left = YES, right = NO),
            StringMapEntryAdd("sidewalk", "left")
        )
    }

    @Test fun `apply sidewalk right answer`() {
        questType.verifyAnswer(
            LeftAndRightSidewalk(left = NO, right = YES),
            StringMapEntryAdd("sidewalk", "right")
        )
    }

    @Test fun `apply sidewalk on both sides answer`() {
        questType.verifyAnswer(
            LeftAndRightSidewalk(left = YES, right = YES),
            StringMapEntryAdd("sidewalk", "both")
        )
    }

    @Test fun `apply separate sidewalk answer`() {
        questType.verifyAnswer(
            LeftAndRightSidewalk(left = SEPARATE, right = SEPARATE),
            StringMapEntryAdd("sidewalk", "separate")
        )
    }

    @Test fun `apply separate sidewalk on one side answer`() {
        questType.verifyAnswer(
            LeftAndRightSidewalk(left = YES, right = SEPARATE),
            StringMapEntryAdd("sidewalk:left", "yes"),
            StringMapEntryAdd("sidewalk:right", "separate"),
        )

        questType.verifyAnswer(
            LeftAndRightSidewalk(left = SEPARATE, right = NO),
            StringMapEntryAdd("sidewalk:left", "separate"),
            StringMapEntryAdd("sidewalk:right", "no"),
        )
    }

    @Test fun `replace incomplete sidewalk tagging`() {
        questType.verifyAnswer(
            mapOf("sidewalk:left" to "yes"),
            LeftAndRightSidewalk(left = YES, right = NO),
            StringMapEntryAdd("sidewalk", "left"),
            StringMapEntryDelete("sidewalk:left", "yes")
        )
        questType.verifyAnswer(
            mapOf("sidewalk:left" to "yes"),
            LeftAndRightSidewalk(left = YES, right = SEPARATE),
            StringMapEntryModify("sidewalk:left", "yes", "yes"),
            StringMapEntryAdd("sidewalk:right", "separate"),
        )
    }
}
