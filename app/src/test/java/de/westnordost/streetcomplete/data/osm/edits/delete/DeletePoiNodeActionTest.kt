package de.westnordost.streetcomplete.data.osm.edits.delete

import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.on
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DeletePoiNodeActionTest {

    private val e = node(1, tags = mutableMapOf("amenity" to "atm"), version = 2)

    private lateinit var repos: MapDataRepository
    private lateinit var provider: ElementIdProvider

    @Before fun setUp() {
        repos = mock()
        provider = mock()
    }

    @Test fun `delete free-floating node`() {
        on(repos.getWaysForNode(1L)).thenReturn(emptyList())
        on(repos.getRelationsForNode(1L)).thenReturn(emptyList())
        val nd = DeletePoiNodeAction(2).createUpdates(e, repos, provider).single()

        assertTrue(nd.isDeleted)
    }

    @Test fun `'delete' vertex`() {
        on(repos.getWaysForNode(1L)).thenReturn(listOf(mock()))
        on(repos.getRelationsForNode(1L)).thenReturn(emptyList())
        val nd = DeletePoiNodeAction(2).createUpdates(e, repos, provider).single()

        assertFalse(nd.isDeleted)
        assertTrue(nd.tags.isEmpty())
    }

    @Test(expected = ConflictException::class)
    fun `newer version creates conflict`() {
        DeletePoiNodeAction(1).createUpdates(e, repos, provider)
    }
}
