package de.westnordost.streetcomplete.data.osm.edits.create

import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.testutils.any
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.testutils.rel
import de.westnordost.streetcomplete.testutils.way
import de.westnordost.streetcomplete.util.math.translate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RevertCreateNodeActionTest {
    private lateinit var repos: MapDataRepository
    private lateinit var provider: ElementIdProvider

    @Before
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

    @Test(expected = ConflictException::class)
    fun `conflict when node already deleted`() {
        on(repos.getNode(any())).thenReturn(null)
        RevertCreateNodeAction(node(), listOf()).createUpdates(repos, provider)
    }

    @Test(expected = ConflictException::class)
    fun `conflict when node is now member of a relation`() {
        val node = node(1)

        on(repos.getNode(node.id)).thenReturn(node)
        on(repos.getWaysForNode(1)).thenReturn(emptyList())
        on(repos.getRelationsForNode(1)).thenReturn(listOf(rel()))

        RevertCreateNodeAction(node, listOf()).createUpdates(repos, provider)
    }

    @Test(expected = ConflictException::class)
    fun `conflict when node is part of more ways than initially`() {
        val node = node(1)

        on(repos.getNode(node.id)).thenReturn(node)
        on(repos.getWaysForNode(1)).thenReturn(listOf(way(1), way(2), way(3)))
        on(repos.getRelationsForNode(1)).thenReturn(emptyList())

        RevertCreateNodeAction(node, listOf(1,2)).createUpdates(repos, provider)
    }


    @Test(expected = ConflictException::class)
    fun `conflict when node was moved at all`() {
        val node = node(1)

        on(repos.getNode(1)).thenReturn(node.copy(position = node.position.translate(10.0, 0.0)))
        on(repos.getWaysForNode(1)).thenReturn(emptyList())
        on(repos.getRelationsForNode(1)).thenReturn(emptyList())

        RevertCreateNodeAction(node).createUpdates(repos, provider)
    }

    @Test(expected = ConflictException::class)
    fun `conflict when tags changed on node at all`() {
        val node = node(1)

        on(repos.getNode(1)).thenReturn(node.copy(tags = mapOf("different" to "tags")))
        on(repos.getWaysForNode(1)).thenReturn(emptyList())
        on(repos.getRelationsForNode(1)).thenReturn(emptyList())

        RevertCreateNodeAction(node).createUpdates(repos, provider)
    }

    @Test
    fun `no conflict when node is part of less ways than initially`() {
        val node = node(1)

        on(repos.getNode(node.id)).thenReturn(node)
        on(repos.getWaysForNode(1)).thenReturn(listOf(way(1)))
        on(repos.getRelationsForNode(1)).thenReturn(emptyList())

        RevertCreateNodeAction(node, listOf(1,2)).createUpdates(repos, provider)
    }

    @Test
    fun `removes to be deleted node from ways`() {
        val node = node(1)

        val way1 = way(1, nodes = listOf(1,2,3), timestamp = 0)
        val way2 = way(2, nodes = listOf(4,1,6), timestamp = 0)

        on(repos.getNode(node.id)).thenReturn(node)
        on(repos.getWaysForNode(1)).thenReturn(listOf(way1, way2))
        on(repos.getRelationsForNode(1)).thenReturn(emptyList())

        val data = RevertCreateNodeAction(node, listOf(1,2,3)).createUpdates(repos, provider)

        assertEquals(2, data.modifications.size)
        val updatedWays = data.modifications.toList()

        val updatedWay1 = updatedWays[0] as Way
        assertEquals(way1.id, updatedWay1.id)
        assertNotEquals(0, updatedWay1.timestampEdited)
        assertEquals(listOf<Long>(2,3), updatedWay1.nodeIds)

        val updatedWay2 = updatedWays[1] as Way
        assertEquals(way2.id, updatedWay2.id)
        assertNotEquals(0, updatedWay2.timestampEdited)
        assertEquals(listOf<Long>(4,6), updatedWay2.nodeIds)

        val deletedNode = data.deletions.single() as Node
        assertEquals(node, deletedNode)
    }
}
