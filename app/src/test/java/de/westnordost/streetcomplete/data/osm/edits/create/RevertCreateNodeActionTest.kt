package de.westnordost.streetcomplete.data.osm.edits.create

import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
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
        val data = RevertCreateNodeAction.createUpdates(node, node, repos, provider)

        assertTrue(data.creations.isEmpty())
        assertTrue(data.modifications.isEmpty())

        val deletedNode = data.deletions.single() as Node
        assertEquals(node, deletedNode)
    }

    @Test(expected = ConflictException::class)
    fun `conflict revert add node when already deleted`() {
        RevertCreateNodeAction.createUpdates(node(), null, repos, provider)
    }
}
