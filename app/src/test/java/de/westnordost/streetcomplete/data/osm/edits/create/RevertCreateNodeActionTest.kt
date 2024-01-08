package de.westnordost.streetcomplete.data.osm.edits.create

import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.testutils.rel
import de.westnordost.streetcomplete.testutils.way
import de.westnordost.streetcomplete.util.math.translate
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class RevertCreateNodeActionTest {
    private lateinit var repos: MapDataRepository
    private lateinit var provider: ElementIdProvider

    @BeforeTest
    fun setUp() {
        repos = mock()
        provider = mock()
    }

    @Test
    fun `revert add node`() {
        val node = node(123, LatLon(12.0, 34.0), mapOf("amenity" to "atm"), 1)
        on(repos.getNode(node.id)).thenReturn(node)
        val data = RevertCreateNodeAction(node, listOf()).createUpdates(repos, provider)

        assertTrue(data.creations.isEmpty())
        assertTrue(data.modifications.isEmpty())

        val deletedNode = data.deletions.single() as Node
        assertEquals(node, deletedNode)
    }

    @Test
    fun `conflict when node already deleted`() {
        on(repos.getNode(1)).thenReturn(null)

        assertFailsWith<ConflictException> {
            RevertCreateNodeAction(node(1), listOf()).createUpdates(repos, provider)
        }
    }

    @Test
    fun `conflict when node is now member of a relation`() {
        val node = node(1)

        on(repos.getNode(node.id)).thenReturn(node)
        on(repos.getWaysForNode(1)).thenReturn(emptyList())
        on(repos.getRelationsForNode(1)).thenReturn(listOf(rel()))

        assertFailsWith<ConflictException> {
            RevertCreateNodeAction(node, listOf()).createUpdates(repos, provider)
        }
    }

    @Test
    fun `conflict when node is part of more ways than initially`() {
        val node = node(1)

        on(repos.getNode(node.id)).thenReturn(node)
        on(repos.getWaysForNode(1)).thenReturn(listOf(way(1), way(2), way(3)))
        on(repos.getRelationsForNode(1)).thenReturn(emptyList())

        assertFailsWith<ConflictException> {
            RevertCreateNodeAction(node, listOf(1, 2)).createUpdates(repos, provider)
        }
    }

    @Test
    fun `conflict when node was moved at all`() {
        val node = node(1)
        val movedNode = node.copy(position = node.position.translate(10.0, 0.0))

        on(repos.getNode(1)).thenReturn(movedNode)
        on(repos.getWaysForNode(1)).thenReturn(emptyList())
        on(repos.getRelationsForNode(1)).thenReturn(emptyList())

        assertFailsWith<ConflictException> {
            RevertCreateNodeAction(node).createUpdates(repos, provider)
        }
    }

    @Test
    fun `conflict when tags changed on node at all`() {
        val node = node(1)

        on(repos.getNode(1)).thenReturn(node.copy(tags = mapOf("different" to "tags")))
        on(repos.getWaysForNode(1)).thenReturn(emptyList())
        on(repos.getRelationsForNode(1)).thenReturn(emptyList())

        assertFailsWith<ConflictException> {
            RevertCreateNodeAction(node).createUpdates(repos, provider)
        }
    }

    @Test
    fun `no conflict when node is part of less ways than initially`() {
        val node = node(1)

        on(repos.getNode(node.id)).thenReturn(node)
        on(repos.getWaysForNode(1)).thenReturn(listOf(way(1)))
        on(repos.getRelationsForNode(1)).thenReturn(emptyList())

        RevertCreateNodeAction(node, listOf(1, 2)).createUpdates(repos, provider)
    }

    @Test
    fun `removes to be deleted node from ways`() {
        val node = node(1)

        val way1 = way(1, nodes = listOf(1, 2, 3), timestamp = 0)
        val way2 = way(2, nodes = listOf(4, 1, 6), timestamp = 0)

        on(repos.getNode(node.id)).thenReturn(node)
        on(repos.getWaysForNode(1)).thenReturn(listOf(way1, way2))
        on(repos.getRelationsForNode(1)).thenReturn(emptyList())

        val data = RevertCreateNodeAction(node, listOf(1, 2, 3)).createUpdates(repos, provider)

        assertEquals(2, data.modifications.size)
        val updatedWays = data.modifications.toList()

        val updatedWay1 = updatedWays[0] as Way
        assertEquals(way1.id, updatedWay1.id)
        assertNotEquals(0, updatedWay1.timestampEdited)
        assertEquals(listOf<Long>(2, 3), updatedWay1.nodeIds)

        val updatedWay2 = updatedWays[1] as Way
        assertEquals(way2.id, updatedWay2.id)
        assertNotEquals(0, updatedWay2.timestampEdited)
        assertEquals(listOf<Long>(4, 6), updatedWay2.nodeIds)

        val deletedNode = data.deletions.single() as Node
        assertEquals(node, deletedNode)
    }

    @Test fun idsUpdatesApplied() {
        val node = node(id = -1)
        val action = RevertCreateNodeAction(node, listOf(-1, -2, 3))
        val idUpdates = mapOf(
            ElementKey(ElementType.WAY, -1) to 99L,
            ElementKey(ElementType.WAY, -2) to 5L,
            ElementKey(ElementType.NODE, -1) to 999L,
        )

        assertEquals(
            RevertCreateNodeAction(node.copy(id = 999), listOf(99, 5, 3)),
            action.idsUpdatesApplied(idUpdates)
        )
    }

    @Test fun elementKeys() {
        assertEquals(
            listOf(
                ElementKey(ElementType.WAY, -1),
                ElementKey(ElementType.WAY, -2),
                ElementKey(ElementType.WAY, 3),
                ElementKey(ElementType.NODE, -1),
            ),
            RevertCreateNodeAction(node(id = -1), listOf(-1, -2, 3)).elementKeys
        )
    }
}
