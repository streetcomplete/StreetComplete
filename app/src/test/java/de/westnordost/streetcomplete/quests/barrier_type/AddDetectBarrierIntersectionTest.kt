package de.westnordost.streetcomplete.quests.barrier_type

import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.testutils.way
import kotlin.test.Assert
import kotlin.test.Test

class AddDetectBarrierIntersectionTest {
    private val questType = AddBarrierOnRoad()

    @Test fun `free-floating nodes do not count`() {
        val mapData = TestMapDataWithGeometry(listOf(
            node(1)
        ))
        Assert.assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    /*
      ══╪══
    */
    @Test fun `simple crossing counts`() {
        val shared = node(2, p(0.0, 0.0))
        val mapData = TestMapDataWithGeometry(listOf(
            node(1, p(0.0, -1.0)),
            shared,
            node(3, p(0.0, +1.0)),
            node(4, p(-1.0, 0.0)),
            node(5, p(+1.0, 0.0)),
            way(1, nodes = listOf(1, 2, 3), tags = mapOf("highway" to "unclassified")),
            way(2, nodes = listOf(4, 2, 5), tags = mapOf("barrier" to "wall")),
        ))
        Assert.assertEquals(shared, questType.getApplicableElements(mapData).toList().single())
    }

    /*
      ══╪══
    */
    @Test fun `simple crossing with tags on node is skipped`() {
        val shared = node(2, p(0.0, 0.0), tags = mapOf("anything" to "whatever"))
        val mapData = TestMapDataWithGeometry(listOf(
            node(1, p(0.0, -1.0)),
            shared,
            node(3, p(0.0, +1.0)),
            node(4, p(-1.0, 0.0)),
            node(5, p(+1.0, 0.0)),
            way(1, nodes = listOf(1, 2, 3), tags = mapOf("highway" to "unclassified")),
            way(2, nodes = listOf(4, 2, 5), tags = mapOf("barrier" to "wall")),
        ))
        Assert.assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    /*
      ═══╡
    */
    @Test fun `crossing road on end node does not count`() {
        val shared = node(2, p(0.0, 0.0))
        val mapData = TestMapDataWithGeometry(listOf(
            node(1, p(0.0, -1.0)),
            shared,
            node(4, p(-1.0, 0.0)),
            node(5, p(+1.0, 0.0)),
            way(1, nodes = listOf(1, 2), tags = mapOf("highway" to "unclassified")),
            way(2, nodes = listOf(4, 2, 5), tags = mapOf("barrier" to "fence")),
        ))
        Assert.assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    /*
      ══╧══
    */
    @Test fun `crossing barrier on end node does not count`() {
        val shared = node(2, p(0.0, 0.0))
        val mapData = TestMapDataWithGeometry(listOf(
            node(1, p(0.0, -1.0)),
            shared,
            node(3, p(0.0, +1.0)),
            node(4, p(-1.0, 0.0)),
            way(1, nodes = listOf(1, 2, 3), tags = mapOf("highway" to "unclassified")),
            way(2, nodes = listOf(4, 2), tags = mapOf("barrier" to "city_wall")),
        ))
        Assert.assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    /*
      ══╪══ (4 ways)
    */
    @Test fun `crossing with ways split at shared node counts`() {
        val shared = node(2, p(0.0, 0.0))
        val mapData = TestMapDataWithGeometry(listOf(
            node(1, p(0.0, -1.0)),
            shared,
            node(3, p(0.0, +1.0)),
            node(4, p(-1.0, 0.0)),
            node(5, p(+1.0, 0.0)),
            way(1, nodes = listOf(1, 2), tags = mapOf("highway" to "unclassified")),
            way(2, nodes = listOf(3, 2), tags = mapOf("highway" to "unclassified")),
            way(3, nodes = listOf(4, 2), tags = mapOf("barrier" to "guard_rail")),
            way(4, nodes = listOf(2, 5), tags = mapOf("barrier" to "wall"))
        ))
        Assert.assertEquals(shared, questType.getApplicableElements(mapData).toList().single())
    }

    /*
       │ ╱
       │❬
       │ ╲
    */
    @Test fun `touching but not crossing barrier does not count`() {
        val shared = node(2, p(0.0, 0.0))
        val mapData = TestMapDataWithGeometry(listOf(
            node(1, p(0.0, -1.0)),
            shared,
            node(3, p(0.0, +1.0)),
            node(4, p(-1.0, 0.0)),
            node(5, p(-1.0, +1.0)), // <--
            way(1, nodes = listOf(1, 2, 3), tags = mapOf("highway" to "unclassified")),
            way(2, nodes = listOf(4, 2, 5), tags = mapOf("barrier" to "fence")),
        ))
        Assert.assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    /*
       │ ╱
       │❬
       │ ╲
    */
    @Test fun `touching but not crossing road does not count`() {
        val shared = node(2, p(0.0, 0.0))
        val mapData = TestMapDataWithGeometry(listOf(
            node(1, p(0.0, -1.0)),
            shared,
            node(3, p(1.0, -1.0)), // <--
            node(4, p(-1.0, 0.0)),
            node(5, p(+1.0, 0.0)),
            way(1, nodes = listOf(1, 2, 3), tags = mapOf("highway" to "unclassified")),
            way(2, nodes = listOf(4, 2, 5), tags = mapOf("barrier" to "wall")),
        ))
        Assert.assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    /*
       │ ╱
    ───│❬────
       │ ╲
    */
    @Test fun `one of several barriers crosses the road counts`() {
        val shared = node(2, p(0.0, 0.0))
        val mapData = TestMapDataWithGeometry(listOf(
            node(1, p(0.0, -1.0)),
            shared,
            node(3, p(0.0, +1.0)),
            node(4, p(-1.0, 0.0)),
            node(5, p(-1.0, +1.0)),
            node(6, p(-1.0, -1.0)),
            node(7, p(+1.0, 0.0)),
            way(1, nodes = listOf(1, 2, 3), tags = mapOf("highway" to "unclassified")),
            way(2, nodes = listOf(4, 2), tags = mapOf("barrier" to "wall")),
            way(3, nodes = listOf(5, 2), tags = mapOf("barrier" to "fence")),
            way(4, nodes = listOf(6, 2), tags = mapOf("barrier" to "hedge")),
            way(5, nodes = listOf(7, 2), tags = mapOf("barrier" to "guard_rail")),
        ))
        Assert.assertEquals(shared, questType.getApplicableElements(mapData).toList().single())
    }

    /*
        ║
      ══╬══
      ╱ ║ ╲
    */
    @Test fun `one barrier crossing any of the roads count`() {
        val shared = node(2, p(0.0, 0.0))
        val mapData = TestMapDataWithGeometry(listOf(
            node(1, p(0.0, -1.0)),
            shared,
            node(3, p(0.0, +1.0)),
            node(4, p(-1.0, 0.0)),
            node(5, p(+1.0, 0.0)),
            node(6, p(-1.0, -1.0)),
            node(7, p(-1.0, +1.0)),
            way(1, nodes = listOf(1, 2, 3), tags = mapOf("highway" to "unclassified")),
            way(2, nodes = listOf(4, 2, 5), tags = mapOf("highway" to "unclassified")),
            way(3, nodes = listOf(6, 2, 7), tags = mapOf("barrier" to "city_wall")),
        ))
        Assert.assertEquals(shared, questType.getApplicableElements(mapData).toList().single())
    }

    /*
        ║
      ──╫──
        ║
    */
    @Test fun `skip roads going into tunnel`() {
        // some people map the retaining wall as joining with the road, which is not really incorrect
        val shared = node(2, p(0.0, 0.0))
        val mapData = TestMapDataWithGeometry(listOf(
            node(1, p(0.0, -1.0)),
            shared,
            node(3, p(0.0, +1.0)),
            node(4, p(-1.0, 0.0)),
            node(5, p(+1.0, 0.0)),
            way(1, nodes = listOf(1, 2), tags = mapOf("highway" to "unclassified", "tunnel" to "yes")),
            way(2, nodes = listOf(3, 2), tags = mapOf("highway" to "unclassified")),
            way(3, nodes = listOf(4, 2, 5), tags = mapOf("barrier" to "retaining_wall")),
        ))
        Assert.assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }
}
