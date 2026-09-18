package de.westnordost.streetcomplete.quests.tactile_paving

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.testutils.TestMapDataWithGeometry
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.way
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AddTactilePavingCrosswalkTest {
    private val questType = AddTactilePavingCrosswalk()

    @Test fun `not applicable to non-crossing`() {
        val node = node(tags = mapOf("nub" to "dub"))
        val mapData = TestMapDataWithGeometry(listOf(node))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertEquals(false, questType.isApplicableTo(node))
    }

    @Test fun `applicable to crossing`() {
        val crossing = node(tags = mapOf("highway" to "crossing"))
        val mapData = TestMapDataWithGeometry(listOf(crossing))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        assertNull(questType.isApplicableTo(crossing))
    }

    @Test fun `not applicable to crossing with private road`() {
        val crossing = node(id = 1, tags = mapOf("highway" to "crossing"))
        val privateRoad = way(nodes = listOf(1, 2, 3), tags = mapOf(
            "highway" to "residential",
            "access" to "private"
        ))
        val mapData = TestMapDataWithGeometry(listOf(crossing, privateRoad))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertNull(questType.isApplicableTo(crossing))
    }

    private val crossingNode = node(2, tags = mapOf("highway" to "crossing"))
    private val kerb1 = node(1, tags = mapOf("barrier" to "kerb"))
    private val kerb3 = node(3, tags = mapOf("barrier" to "kerb"))
    private val crosswalk = way(1, nodes = listOf(1, 2, 3), tags = mapOf(
        "highway" to "footway",
        "footway" to "crossing"
    ))

    @Test fun `answering yes also applies tactile paving to the kerbs at both ends`() {
        val mapData = TestMapDataWithGeometry(listOf(kerb1, crossingNode, kerb3, crosswalk))

        val nearbyChanges = questType.applyAnswerToNearbyElements(
            TactilePavingCrosswalkAnswer.YES, crossingNode, mapData
        )

        assertEquals(setOf(1L, 3L), nearbyChanges.map { it.first.id }.toSet())
        for ((_, changes) in nearbyChanges) {
            assertTrue(StringMapEntryAdd("tactile_paving", "yes") in changes.changes)
        }
    }

    @Test fun `answering no does not change the kerbs`() {
        val mapData = TestMapDataWithGeometry(listOf(kerb1, crossingNode, kerb3, crosswalk))

        assertEquals(
            emptyList(),
            questType.applyAnswerToNearbyElements(TactilePavingCrosswalkAnswer.NO, crossingNode, mapData)
        )
    }

    @Test fun `answering yes overrides a kerb tagged no`() {
        val answeredKerb = node(1, tags = mapOf("barrier" to "kerb", "tactile_paving" to "no"))
        val mapData = TestMapDataWithGeometry(listOf(answeredKerb, crossingNode, kerb3, crosswalk))

        val nearbyChanges = questType.applyAnswerToNearbyElements(
            TactilePavingCrosswalkAnswer.YES, crossingNode, mapData
        )

        assertEquals(setOf(1L, 3L), nearbyChanges.map { it.first.id }.toSet())
        val answeredKerbChanges = nearbyChanges.single { it.first.id == 1L }.second
        assertTrue(
            StringMapEntryModify("tactile_paving", "no", "yes") in answeredKerbChanges.changes
        )
    }

    @Test fun `answering yes changes nothing when the crosswalk is not mapped as a way`() {
        val road = way(1, nodes = listOf(4, 2, 5), tags = mapOf("highway" to "residential"))
        val mapData = TestMapDataWithGeometry(listOf(crossingNode, road))

        assertEquals(
            emptyList(),
            questType.applyAnswerToNearbyElements(TactilePavingCrosswalkAnswer.YES, crossingNode, mapData)
        )
    }
}
