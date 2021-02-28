package de.westnordost.streetcomplete.data.osm.edits.delete

import de.westnordost.osmapi.map.data.*
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.edits.upload.ElementConflictException
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DeletePoiNodeActionTest {

    private val e = OsmNode(1L, 2, 0.0, 0.0, mutableMapOf("amenity" to "atm"))

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

    @Test(expected = ElementConflictException::class)
    fun `newer version creates conflict`() {
        DeletePoiNodeAction(1).createUpdates(e, repos, provider)
    }
}
