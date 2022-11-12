package de.westnordost.streetcomplete.data.osm.edits.move

import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.util.ktx.copy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RevertMoveNodeActionTest {

    private lateinit var repos: MapDataRepository
    private lateinit var provider: ElementIdProvider

    @Before fun setUp() {
        repos = mock()
        provider = mock()
    }

    @Test fun unmoveIt() {
        val n = node()
        val p = p(0.0, 1.0)
        val movedNode = n.copy(position = p)
        val updates = RevertMoveNodeAction.createUpdates(n, movedNode, repos, provider)
        assertTrue(updates.creations.isEmpty())
        assertTrue(updates.deletions.isEmpty())
        assertEquals(n, updates.modifications.single().copy(timestampEdited = n.timestampEdited))
    }
}
