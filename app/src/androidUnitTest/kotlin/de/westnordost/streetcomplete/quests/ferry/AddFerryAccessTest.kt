package de.westnordost.streetcomplete.quests.ferry

import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.testutils.member
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.rel
import de.westnordost.streetcomplete.testutils.way
import kotlin.test.Test
import kotlin.test.assertEquals

class AddFerryAccessTest {

    private val pedestrianQuest = AddFerryAccessPedestrian()
    private val motorQuest = AddFerryAccessMotorVehicle()

    @Test
    fun `test with one relation and one unassigned way`() {
        val mapData = TestMapDataWithGeometry(
            listOf(
                node(1),
                node(2),
                node(3),
                node(4),
                way(5L, listOf(1, 2), mapOf(
                    "route" to "ferry",
                )),
                rel(6L, listOf(member(ElementType.WAY, 5L)), mapOf(
                    "route" to "ferry",
                    "type" to "route"
                )),
                way(7L, listOf(3, 4), mapOf(
                    "route" to "ferry",
                )),
            ),
        )
        assertEquals(2, pedestrianQuest.getApplicableElements(mapData).toList().size)
        assertEquals(2, motorQuest.getApplicableElements(mapData).toList().size)
    }

    @Test
    fun `test with one relation and no unassigned ways`() {
        val mapData = TestMapDataWithGeometry(
            listOf(
                node(1),
                node(2),
                node(3),
                node(4),
                way(5L, listOf(1, 2), mapOf(
                    "route" to "ferry",
                )),
                way(7L, listOf(3, 4), mapOf(
                    "route" to "ferry",
                )),
                rel(6L, listOf(member(ElementType.WAY, 5L), member(ElementType.WAY, 7L)), mapOf(
                    "route" to "ferry",
                    "type" to "route"
                )),
            ),
        )
        assertEquals(1, pedestrianQuest.getApplicableElements(mapData).toList().size)
        assertEquals(1, motorQuest.getApplicableElements(mapData).toList().size)
    }

    @Test
    fun `test with two unassigned ways`() {
        val mapData = TestMapDataWithGeometry(
            listOf(
                node(1),
                node(2),
                node(3),
                node(4),
                way(5L, listOf(1, 2), mapOf(
                    "route" to "ferry",
                )),
                way(6L, listOf(2, 3)),
                way(7L, listOf(3, 4), mapOf(
                    "route" to "ferry",
                )),
            ),
        )
        assertEquals(2, pedestrianQuest.getApplicableElements(mapData).toList().size)
        assertEquals(2, motorQuest.getApplicableElements(mapData).toList().size)
    }
}
