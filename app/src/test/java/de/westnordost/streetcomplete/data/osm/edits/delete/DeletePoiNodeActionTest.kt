package de.westnordost.streetcomplete.data.osm.edits.delete

import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.testutils.p
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
        on(repos.getNode(e.id)).thenReturn(e)
        val data = DeletePoiNodeAction(e).createUpdates(repos, provider)
        assertTrue(data.modifications.isEmpty())
        assertTrue(data.creations.isEmpty())
        assertEquals(e, data.deletions.single())
    }

    @Test fun `'delete' vertex`() {
        on(repos.getWaysForNode(1L)).thenReturn(listOf(mock()))
        on(repos.getRelationsForNode(1L)).thenReturn(emptyList())
        on(repos.getNode(e.id)).thenReturn(e)
        val data = DeletePoiNodeAction(e).createUpdates(repos, provider)
        assertTrue(data.deletions.isEmpty())
        assertTrue(data.creations.isEmpty())
        assertTrue(data.modifications.single().tags.isEmpty())
    }

    @Test(expected = ConflictException::class)
    fun `moved element creates conflict`() {
        on(repos.getNode(e.id)).thenReturn(e.copy(position = p(1.0, 1.0)))
        DeletePoiNodeAction(e).createUpdates(repos, provider)
    }
}
