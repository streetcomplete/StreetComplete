package de.westnordost.streetcomplete.data.osm.edits.create

import de.westnordost.streetcomplete.data.ConflictException
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.mapdata.MutableMapData
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.testutils.way
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class CreateNodeActionTest {
    private lateinit var repos: MapDataRepository
    private lateinit var provider: ElementIdProvider

    @BeforeTest
    fun setUp() {
        repos = mock()
        provider = mock()
    }

    @Test
    fun `create node`() {
        on(provider.nextNodeId()).thenReturn(-123)

        val tags = mapOf("amenity" to "atm")
        val position = LatLon(12.0, 34.0)
        val action = CreateNodeAction(position, tags)

        val data = action.createUpdates(repos, provider)

        assertTrue(data.deletions.isEmpty())
        assertTrue(data.modifications.isEmpty())

        val createdNode = data.creations.single() as Node
        assertEquals(-123, createdNode.id)
        assertEquals(1, createdNode.version)
        assertEquals(tags, createdNode.tags)
        assertEquals(position, createdNode.position)
        assertNotEquals(0, createdNode.timestampEdited)
    }

    @Test
    fun `create node and add to way`() {
        val way = way(id = 1, nodes = listOf(1, 2))
        val pos1 = LatLon(0.0, 1.0)
        val pos2 = LatLon(0.0, 2.0)
        val node1 = node(id = 1, pos = pos1)
        val node2 = node(id = 2, pos = pos2)

        on(provider.nextNodeId()).thenReturn(-123)
        on(repos.getWayComplete(1)).thenReturn(MutableMapData(listOf(way, node1, node2)))

        val tags = mapOf("entrance" to "yes")
        val position = LatLon(0.0, 1.5)
        val action = CreateNodeAction(position, tags, listOf(InsertIntoWayAt(way.id, pos1, pos2)))

        val data = action.createUpdates(repos, provider)

        val createdNode = data.creations.single() as Node
        assertEquals(-123, createdNode.id)

        val updatedWay = data.modifications.single() as Way
        assertEquals(way.id, updatedWay.id)
        assertEquals(updatedWay.tags, updatedWay.tags)
        assertNotEquals(0, updatedWay.timestampEdited)
        assertEquals(listOf<Long>(1, -123, 2), updatedWay.nodeIds)
    }

    @Test
    fun `conflict if way the node should be inserted into does not exist`() {
        val way = way(id = 1, nodes = listOf(1, 2))
        val pos1 = LatLon(0.0, 1.0)
        val pos2 = LatLon(0.0, 2.0)

        on(provider.nextNodeId()).thenReturn(-123)
        on(repos.getWayComplete(1)).thenReturn(null)

        val tags = mapOf("entrance" to "yes")
        val position = LatLon(0.0, 1.5)
        val action = CreateNodeAction(position, tags, listOf(InsertIntoWayAt(way.id, pos1, pos2)))

        assertFailsWith<ConflictException> {
            action.createUpdates(repos, provider)
        }
    }

    @Test
    fun `conflict if node 2 is not successive to node 1`() {
        val way = way(id = 1, nodes = listOf(1, 2, 3))
        val pos1 = LatLon(0.0, 1.0)
        val pos2 = LatLon(0.0, 2.0)
        val pos3 = LatLon(0.0, 3.0)
        val node1 = node(id = 1, pos = pos1)
        val node2 = node(id = 2, pos = pos2)
        val node3 = node(id = 3, pos = pos3)

        on(provider.nextNodeId()).thenReturn(-123)
        on(repos.getWayComplete(1)).thenReturn(MutableMapData(listOf(way, node1, node2, node3)))

        val tags = mapOf("entrance" to "yes")
        val position = LatLon(0.0, 1.5)
        val action = CreateNodeAction(position, tags, listOf(InsertIntoWayAt(way.id, pos1, pos3)))

        assertFailsWith<ConflictException> {
            action.createUpdates(repos, provider)
        }
    }

    @Test
    fun `no conflict if node 2 is first node within closed way`() {
        val way = way(id = 1, nodes = listOf(1, 2, 3, 1))
        val pos1 = LatLon(0.0, 1.0)
        val pos2 = LatLon(0.0, 2.0)
        val pos3 = LatLon(0.0, 3.0)
        val node1 = node(id = 1, pos = pos1)
        val node2 = node(id = 2, pos = pos2)
        val node3 = node(id = 3, pos = pos3)

        on(provider.nextNodeId()).thenReturn(-123)
        on(repos.getWayComplete(1)).thenReturn(MutableMapData(listOf(way, node1, node2, node3)))

        val tags = mapOf("entrance" to "yes")
        val position = LatLon(0.0, 1.5)
        val dummyNode = node(0, position, tags)
        val action = CreateNodeAction(position, tags, listOf(InsertIntoWayAt(way.id, pos3, pos1)))

        action.createUpdates(repos, provider)
    }

    @Test
    fun `conflict if node 1 has been moved`() {
        val way = way(id = 1, nodes = listOf(1, 2))
        val oldPos1 = LatLon(0.0, 0.0)
        val pos1 = LatLon(0.0, 1.0)
        val pos2 = LatLon(0.0, 2.0)
        val node1 = node(id = 1, pos = pos1)
        val node2 = node(id = 2, pos = pos2)

        on(provider.nextNodeId()).thenReturn(-123)
        on(repos.getWayComplete(1)).thenReturn(MutableMapData(listOf(way, node1, node2)))

        val tags = mapOf("entrance" to "yes")
        val position = LatLon(0.0, 1.5)
        val action =
            CreateNodeAction(position, tags, listOf(InsertIntoWayAt(way.id, oldPos1, pos2)))

        assertFailsWith<ConflictException> {
            action.createUpdates(repos, provider)
        }
    }

    @Test
    fun `conflict if node 2 has been moved`() {
        val way = way(id = 1, nodes = listOf(1, 2))
        val oldPos2 = LatLon(0.0, 0.0)
        val pos1 = LatLon(0.0, 1.0)
        val pos2 = LatLon(0.0, 2.0)
        val node1 = node(id = 1, pos = pos1)
        val node2 = node(id = 2, pos = pos2)

        on(provider.nextNodeId()).thenReturn(-123)
        on(repos.getWayComplete(1)).thenReturn(MutableMapData(listOf(way, node1, node2)))

        val tags = mapOf("entrance" to "yes")
        val position = LatLon(0.0, 1.5)
        val action =
            CreateNodeAction(position, tags, listOf(InsertIntoWayAt(way.id, pos1, oldPos2)))

        assertFailsWith<ConflictException> {
            action.createUpdates(repos, provider)
        }
    }

    @Test
    fun `create node and add to ways`() {
        val way1 = way(id = 1, nodes = listOf(1, 2))
        val way2 = way(id = 2, nodes = listOf(3, 4))
        val pos1 = LatLon(0.0, -1.0)
        val pos2 = LatLon(0.0, +1.0)
        val pos3 = LatLon(-1.0, 0.0)
        val pos4 = LatLon(+1.0, 0.0)
        val node1 = node(id = 1, pos = pos1)
        val node2 = node(id = 2, pos = pos2)
        val node3 = node(id = 3, pos = pos3)
        val node4 = node(id = 4, pos = pos4)

        on(provider.nextNodeId()).thenReturn(-123)
        on(repos.getWayComplete(1)).thenReturn(MutableMapData(listOf(way1, way2, node1, node2)))
        on(repos.getWayComplete(2)).thenReturn(MutableMapData(listOf(way2, node3, node4)))

        val tags = mapOf("entrance" to "yes")
        val position = LatLon(0.0, 0.0)
        val action = CreateNodeAction(
            position, tags, listOf(
                InsertIntoWayAt(way1.id, pos1, pos2),
                InsertIntoWayAt(way2.id, pos3, pos4),
            )
        )

        val data = action.createUpdates(repos, provider)

        val createdNode = data.creations.single() as Node
        assertEquals(-123, createdNode.id)

        assertEquals(listOf<Long>(1, 2), data.modifications.map { it.id })
    }

    @Test
    fun `create node on closed way`() {
        val way1 = way(id = 1, nodes = listOf(1, 2, 3, 4, 1))
        val pos1 = LatLon(-1.0, -1.0)
        val pos2 = LatLon(+1.0, -1.0)
        val pos3 = LatLon(+1.0, +1.0)
        val pos4 = LatLon(-1.0, +1.0)
        val node1 = node(id = 1, pos = pos1)
        val node2 = node(id = 2, pos = pos2)
        val node3 = node(id = 3, pos = pos3)
        val node4 = node(id = 4, pos = pos4)

        on(provider.nextNodeId()).thenReturn(-123)
        on(repos.getWayComplete(1)).thenReturn(MutableMapData(listOf(way1, node1, node2, node3, node4)))

        val tags = mapOf("entrance" to "yes")
        val position = LatLon(-1.0, 0.0)
        val action = CreateNodeAction(position, tags, listOf(InsertIntoWayAt(way1.id, pos4, pos1)))

        val data = action.createUpdates(repos, provider)

        val createdNode = data.creations.single() as Node
        assertEquals(-123, createdNode.id)

        assertEquals(listOf<Long>(1, 2, 3, 4, -123, 1), (data.modifications.single() as Way).nodeIds)
    }

    @Test fun idsUpdatesApplied() {
        val action = CreateNodeAction(
            p(),
            mapOf(),
            listOf(InsertIntoWayAt(-1, p(1.0, 1.0), p(0.0, 1.0)))
        )
        val idUpdates = mapOf(ElementKey(ElementType.WAY, -1) to 99L)

        assertEquals(
            CreateNodeAction(
                p(),
                mapOf(),
                listOf(InsertIntoWayAt(99, p(1.0, 1.0), p(0.0, 1.0)))
            ),
            action.idsUpdatesApplied(idUpdates)
        )
    }

    @Test fun elementKeys() {
        assertEquals(
            listOf(ElementKey(ElementType.WAY, -1)),
            CreateNodeAction(
                p(),
                mapOf(),
                listOf(InsertIntoWayAt(-1, p(1.0, 1.0), p(0.0, 1.0)))
            ).elementKeys
        )
    }
}
