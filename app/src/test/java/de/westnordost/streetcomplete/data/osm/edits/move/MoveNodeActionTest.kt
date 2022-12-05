package de.westnordost.streetcomplete.data.osm.edits.move

import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.testutils.way
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MoveNodeActionTest {

    private lateinit var repos: MapDataRepository
    private lateinit var provider: ElementIdProvider

    @Before fun setUp() {
        repos = mock()
        provider = mock()
    }

    @Test(expected = ConflictException::class)
    fun `conflictException on wrong elementType`() {
        val w = way(1, listOf(1, 2, 3))
        MoveNodeAction(p()).createUpdates(w, w, repos, provider)
    }

    @Test fun moveIt() {
        val n = node()
        val p = p(0.0, 1.0)
        val movedNode = n.copy(position = p)
        val updates = MoveNodeAction(p).createUpdates(n, n, repos, provider)
        assertTrue(updates.creations.isEmpty())
        assertTrue(updates.deletions.isEmpty())
        assertEquals(listOf(movedNode), updates.modifications)
    }
}
