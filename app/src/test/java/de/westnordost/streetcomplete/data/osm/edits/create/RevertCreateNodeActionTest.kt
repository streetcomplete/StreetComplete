package de.westnordost.streetcomplete.data.osm.edits.create

import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.testutils.rel
import de.westnordost.streetcomplete.testutils.way
import de.westnordost.streetcomplete.util.math.translate
import org.junit.Assert.assertEquals
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
        val data = RevertCreateNodeAction(node).createUpdates(repos, provider)

        assertTrue(data.creations.isEmpty())
        assertTrue(data.modifications.isEmpty())

        val deletedNode = data.deletions.single() as Node
        assertEquals(node, deletedNode)
    }

    @Test(expected = ConflictException::class)
    fun `conflict when node already deleted`() {
        on(repos.getNode(1)).thenReturn(null)
        RevertCreateNodeAction(node(1)).createUpdates(repos, provider)
    }

    @Test(expected = ConflictException::class)
    fun `conflict when node is now member of a relation`() {
        val node = node(1)

        on(repos.getNode(node.id)).thenReturn(node)
        on(repos.getWaysForNode(1)).thenReturn(emptyList())
        on(repos.getRelationsForNode(1)).thenReturn(listOf(rel()))

        RevertCreateNodeAction(node).createUpdates(repos, provider)
    }

    @Test(expected = ConflictException::class)
    fun `conflict when node is now part of a way`() {
        val node = node(1)

        on(repos.getNode(node.id)).thenReturn(node)
        on(repos.getWaysForNode(1)).thenReturn(listOf(way(1)))
        on(repos.getRelationsForNode(1)).thenReturn(emptyList())

        RevertCreateNodeAction(node).createUpdates(repos, provider)
    }


    @Test(expected = ConflictException::class)
    fun `conflict when node was moved at all`() {
        val node = node(1)
        val movedNode = node.copy(position = node.position.translate(10.0, 0.0))

        on(repos.getNode(1)).thenReturn(movedNode)
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
}
