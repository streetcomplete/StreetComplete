package de.westnordost.streetcomplete.quests.ferry

import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.quests.ferry.AddFerryAccessPedestrian
import de.westnordost.streetcomplete.testutils.member
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.rel
import de.westnordost.streetcomplete.testutils.way
import kotlin.test.Test
import kotlin.test.assertEquals

class AddFerryAccessTest {

    private val questType = AddFerryAccessPedestrian()

    private fun generalTestData(): TestMapDataWithGeometry =
        TestMapDataWithGeometry(
            listOf(
                node(1),
                node(2),
                node(3),
                node(4),
                way(5L, listOf(1, 2), mapOf(
                    "route" to "ferry",
                )),
                way(6L, listOf(3, 4), mapOf(
                    "route" to "ferry",
                )),
                rel(7L, listOf(member(ElementType.WAY, 5L)), mapOf(
                    "route" to "ferry",
                )),
            ),
        )

    @Test
    fun `applicable to cases where corridor through building is mapped`() {
        val mapData = generalTestData()
        assertEquals(4, questType.getApplicableElements(mapData).toList().size)
    }
}
