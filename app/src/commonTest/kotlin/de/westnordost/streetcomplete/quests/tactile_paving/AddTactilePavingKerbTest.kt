package de.westnordost.streetcomplete.quests.tactile_paving

import de.westnordost.streetcomplete.testutils.TestMapDataWithGeometry
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.way
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AddTactilePavingKerbTest {
    private val questType = AddTactilePavingKerb()

    private val kerb1 = node(1, tags = mapOf("barrier" to "kerb"))
    private val kerb3 = node(3, tags = mapOf("barrier" to "kerb"))
    private val crosswalk = way(1, nodes = listOf(1, 2, 3), tags = mapOf(
        "highway" to "footway",
        "footway" to "crossing"
    ))

    @Test fun `applicable to kerbs of crosswalk without a crossing node`() {
        val middleNode = node(2)
        val mapData = TestMapDataWithGeometry(listOf(kerb1, middleNode, kerb3, crosswalk))
        assertEquals(
            setOf(1L, 3L),
            questType.getApplicableElements(mapData).map { it.id }.toSet()
        )
        assertNull(questType.isApplicableTo(kerb1))
    }

    @Test fun `not applicable to kerbs of crosswalk with a crossing node`() {
        val crossingNode = node(2, tags = mapOf("highway" to "crossing"))
        val mapData = TestMapDataWithGeometry(listOf(kerb1, crossingNode, kerb3, crosswalk))
        assertEquals(0, questType.getApplicableElements(mapData).count())
    }

    @Test fun `not applicable to kerbs of crosswalk whose crossing node has tactile paving on both ends`() {
        val crossingNode = node(2, tags = mapOf(
            "highway" to "crossing",
            "tactile_paving" to "yes"
        ))
        val mapData = TestMapDataWithGeometry(listOf(kerb1, crossingNode, kerb3, crosswalk))
        assertEquals(0, questType.getApplicableElements(mapData).count())
    }

    @Test fun `applicable to kerbs of crosswalk whose crossing node does not have tactile paving on both ends`() {
        val crossingNode = node(2, tags = mapOf(
            "highway" to "crossing",
            "tactile_paving" to "no"
        ))
        val mapData = TestMapDataWithGeometry(listOf(kerb1, crossingNode, kerb3, crosswalk))
        assertEquals(
            setOf(1L, 3L),
            questType.getApplicableElements(mapData).map { it.id }.toSet()
        )
    }

    @Test fun `editing a crossing node may affect nearby quests`() {
        assertTrue(questType.mayAffectNearbyQuests(node(tags = mapOf("highway" to "crossing"))))
        assertFalse(questType.mayAffectNearbyQuests(node(tags = mapOf("barrier" to "kerb"))))
        assertFalse(questType.mayAffectNearbyQuests(node()))
    }

    @Test fun `applicable to kerbs of crosswalk whose crossing node is on an excluded way`() {
        val crossingNode = node(2, tags = mapOf("highway" to "crossing"))
        val driveway = way(2, nodes = listOf(4, 2, 5), tags = mapOf(
            "highway" to "service",
            "service" to "driveway"
        ))
        val mapData = TestMapDataWithGeometry(listOf(kerb1, crossingNode, kerb3, crosswalk, driveway))
        assertEquals(
            setOf(1L, 3L),
            questType.getApplicableElements(mapData).map { it.id }.toSet()
        )
    }
}
