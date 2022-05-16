package de.westnordost.streetcomplete.quests.building_entrance

import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.way
import org.junit.Assert
import org.junit.Test

class AddBuildingEntranceTest {

    private val questType = AddEntrance()

    @Test
    fun `not applicable to building passage openings`() {
        val mapData = TestMapDataWithGeometry(
            listOf(
                node(1),
                node(2),
                node(3),
                node(4),
                node(30),
                way(1L, listOf(1, 2, 3, 4), mapOf(
                    "building" to "apartments"
                )),
                way(2L, listOf(1, 3), mapOf(
                    "highway" to "footway",
                    "tunnel" to "building_pasage",
                )),
                way(3L, listOf(3, 30), mapOf(
                    "highway" to "footway",
                )),
            ),
        )
        Assert.assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test
    fun `applicable to plausible entrances`() {
        val mapData = TestMapDataWithGeometry(
            listOf(
                node(1),
                node(2),
                node(3),
                node(4),
                node(30),
                way(1L, listOf(1, 2, 3, 4), mapOf(
                    "building" to "apartments"
                )),
                way(2L, listOf(3, 30), mapOf(
                    "highway" to "footway",
                )),
            ),
        )
        Assert.assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }
}
